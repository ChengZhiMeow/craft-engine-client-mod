package net.momirealms.craftengine.viacompat.realblock;

import com.google.gson.JsonElement;
import net.momirealms.craftengine.bukkit.api.event.AsyncResourcePackGenerateEvent;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import net.momirealms.craftengine.bukkit.block.BukkitCustomBlockStateWrapper;
import net.momirealms.craftengine.bukkit.item.behavior.BukkitItemBehaviors;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.DelegatingBlock;
import net.momirealms.craftengine.core.block.DelegatingBlockState;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.property.Property;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.logger.PluginLogger;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviors;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class RealBlockManager implements Listener {
    private static final Key REAL_BLOCK_ITEM = Key.ce("real_block_item");
    private final JavaPlugin plugin;
    private final BukkitCraftEngine craftEngine;
    private final RealBlockConfigParser parser;
    private final Map<Key, RealBlockDefinition> definitions = new LinkedHashMap<>();
    private final Map<Key, IgniteRealBlockBridge.Installation> installations = new LinkedHashMap<>();
    private final Map<Key, Optional<Map<String, JsonElement>>> previousBlockOverrides = new LinkedHashMap<>();
    private final Map<Key, Map<String, JsonElement>> blockOverrides;

    public RealBlockManager(JavaPlugin plugin, BukkitCraftEngine craftEngine) {
        this.plugin = plugin;
        this.craftEngine = craftEngine;
        this.parser = new RealBlockConfigParser(this, findNativeBlockParser(craftEngine));
        this.blockOverrides = CraftEngineBlockOverrides.mutable(craftEngine.blockManager());
    }

    public void enable() {
        if (!"1.21.11".equals(IgniteRealBlockBridge.runtimeVersion())) {
            throw new IllegalStateException("CraftEngine real-block Ignite mod for Minecraft 1.21.11 is required");
        }
        ClientboundRealBlocksPacket.register();
        ServerboundRealBlocksRequestPacket.register(this);
        registerRealBlockItemBehavior();
        registerConnectableBlockBehavior();
        if (!craftEngine.packManager().registerConfigSectionParser(parser)) {
            throw new IllegalStateException("CraftEngine already has a real_blocks config parser");
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void disable() {
        craftEngine.packManager().unregisterConfigSectionParser(parser);
        ServerboundRealBlocksRequestPacket.unregister();
        restoreModelOverrides();
        definitions.clear();
    }

    void beginLoad() {
        restoreModelOverrides();
        definitions.clear();
    }

    void addDefinition(Path path, Key id, RealBlockShapeResolver shapeResolver) {
        if (definitions.containsKey(id)) {
            throw new IllegalArgumentException("duplicate real block id " + id);
        }
        BlockDefinition block = craftEngine.blockManager().blockById(id)
                .orElseThrow(() -> new IllegalArgumentException("no CraftEngine block exists with id " + id));
        List<ImmutableBlockState> blockStates = block.variantProvider().states();
        if (blockStates.isEmpty()) {
            throw new IllegalArgumentException("CraftEngine block has no states: " + id);
        }
        Set<String> availableProperties = blockStates.getFirst().getProperties().stream()
                .map(Property::name)
                .collect(Collectors.toSet());
        shapeResolver.validateProperties(availableProperties);

        int vanillaStateCount = craftEngine.blockManager().vanillaBlockStateCount();
        List<IgniteRealBlockBridge.StateInput> serverInputs = new ArrayList<>(blockStates.size());
        List<RealBlockDefinition.RealBlockStateDefinition> states = new ArrayList<>(blockStates.size());
        List<RealBlockShape> stateShapes = new ArrayList<>(blockStates.size());
        List<String> visualStates = new ArrayList<>(blockStates.size());
        for (int ordinal = 0; ordinal < blockStates.size(); ordinal++) {
            ImmutableBlockState state = blockStates.get(ordinal);
            if (state.customBlockState() == null) {
                throw new IllegalArgumentException("CraftEngine block state has no server-side custom state: " + state);
            }
            if (state.visualBlockState() == null) {
                throw new IllegalArgumentException("CraftEngine block state has no client visual state: " + state);
            }
            int customIndex = state.customBlockState().registryId() - vanillaStateCount;
            if (customIndex < 0) {
                throw new IllegalArgumentException("CraftEngine custom state id is below the vanilla registry: " + state);
            }
            serverInputs.add(new IgniteRealBlockBridge.StateInput(
                    state.customBlockState().minecraftState(),
                    state.customBlockState().registryId()
            ));
            visualStates.add(state.visualBlockState().getAsString());
            RealBlockShape stateShape = shapeResolver.resolve(propertyValues(state));
            stateShapes.add(stateShape);
            states.add(new RealBlockDefinition.RealBlockStateDefinition(customIndex, stateShape));
        }
        Map<String, JsonElement> realBlockModels = RealBlockModelOverrides.create(
                blockOverrides,
                visualStates
        );

        IgniteRealBlockBridge.Installation installation = IgniteRealBlockBridge.install(
                id.toString(),
                serverInputs
        );
        IgniteRealBlockBridge.persist(
                id.toString(),
                installation.states().stream()
                        .map(IgniteRealBlockBridge.StateMapping::rawStateId)
                        .toList()
        );
        if (installation.states().size() != blockStates.size()) {
            throw new IllegalStateException("Ignite returned a different state count for " + id);
        }
        installations.put(id, installation);
        bindServerRuntime(id, blockStates, installation, stateShapes);

        previousBlockOverrides.put(id, Optional.ofNullable(blockOverrides.put(id, realBlockModels)));
        definitions.put(id, new RealBlockDefinition(id, states));
        logger().info("Loaded real_blocks entry " + id + " with " + states.size() + " state(s) from " + path.getFileName());
    }

    void finishLoad() {
        patchResourcePack(craftEngine.packManager().resourcePackPath(), true);
        plugin.getLogger().info("Loaded " + definitions.size() + " CraftEngine real block registry ID(s)");
    }

    int definitionCount() {
        return definitions.size();
    }

    PluginLogger logger() {
        return craftEngine.logger();
    }

    private void bindServerRuntime(
            Key id,
            List<ImmutableBlockState> blockStates,
            IgniteRealBlockBridge.Installation installation,
            List<RealBlockShape> stateShapes
    ) {
        if (!(installation.realBlock() instanceof DelegatingBlock realBlock)) {
            throw new IllegalStateException("Ignite canonical block does not implement DelegatingBlock: " + id);
        }
        IgniteRealBlockBridge.copyStateSettings(
                java.util.stream.IntStream.range(0, blockStates.size())
                        .mapToObj(index -> new IgniteRealBlockBridge.StateCopy(
                                blockStates.get(index).customBlockState().minecraftState(),
                                installation.states().get(index).realState()
                        ))
                        .toList()
        );
        List<DispatchingBlockBehavior.Delegate> behaviorDelegates = new ArrayList<>(blockStates.size());
        List<DispatchingBlockShape.Delegate> shapeDelegates = new ArrayList<>(blockStates.size());
        for (int index = 0; index < blockStates.size(); index++) {
            ImmutableBlockState state = blockStates.get(index);
            IgniteRealBlockBridge.StateMapping mapping = installation.states().get(index);
            if (!(mapping.realState() instanceof DelegatingBlockState realState)) {
                throw new IllegalStateException("Ignite real state does not implement DelegatingBlockState: " + id);
            }
            realState.setBlockState(state);
            realState.setBlockOwner(realBlock);
            state.setCustomBlockState(new BukkitCustomBlockStateWrapper(
                    mapping.realState(),
                    mapping.rawStateId()
            ));
            behaviorDelegates.add(new DispatchingBlockBehavior.Delegate(
                    mapping.realState(),
                    mapping.originalBlock(),
                    state.behavior()
            ));
            ConfiguredBlockShape configuredShape = new ConfiguredBlockShape(stateShapes.get(index));
            shapeDelegates.add(new DispatchingBlockShape.Delegate(mapping.realState(), configuredShape));
            IgniteRealBlockBridge.configureOcclusion(
                    List.of(mapping.realState()),
                    configuredShape.occlusion()
            );
        }
        realBlock.behaviorDelegate().bindValue(new DispatchingBlockBehavior(behaviorDelegates));
        realBlock.shapeDelegate().bindValue(new DispatchingBlockShape(shapeDelegates));
        IgniteRealBlockBridge.configureCollision(
                installation.realBlock(),
                stateShapes.stream().anyMatch(shape -> !shape.collision().isEmpty())
        );
        IgniteRealBlockBridge.refreshStateCaches(
                installation.states().stream().map(IgniteRealBlockBridge.StateMapping::realState).toList()
        );
    }

    private static Map<String, String> propertyValues(ImmutableBlockState state) {
        Map<String, String> values = new LinkedHashMap<>();
        state.propertyEntries().forEach((property, value) ->
                values.put(property.name(), formatPropertyValue(property, value))
        );
        return values;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String formatPropertyValue(Property property, Comparable value) {
        return property.valueName(value);
    }

    private void restoreModelOverrides() {
        for (Map.Entry<Key, Optional<Map<String, JsonElement>>> entry : previousBlockOverrides.entrySet()) {
            if (entry.getValue().isPresent()) {
                blockOverrides.put(entry.getKey(), entry.getValue().get());
            } else {
                blockOverrides.remove(entry.getKey());
            }
        }
        previousBlockOverrides.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> sendDefinitions(event.getPlayer()), 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCraftEngineReload(CraftEngineReloadEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getOnlinePlayers().forEach(this::sendDefinitions));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onResourcePackGenerated(AsyncResourcePackGenerateEvent event) {
        patchResourcePack(event.zipFilePath(), false);
    }

    public void sendDefinitions(Player player) {
        if (!player.isOnline()) {
            return;
        }
        NetWorkUser user = craftEngine.networkManager().getUser(player);
        if (user == null || !user.hasClientMod()) {
            return;
        }
        user.sendCustomPacket(new ClientboundRealBlocksPacket(List.copyOf(definitions.values())));
    }

    void sendDefinitions(NetWorkUser user) {
        user.sendCustomPacket(new ClientboundRealBlocksPacket(List.copyOf(definitions.values())));
    }

    private void patchResourcePack(Path resourcePackZip, boolean refreshHostedPack) {
        Map<Key, Map<String, JsonElement>> realBlockOverrides = new LinkedHashMap<>();
        java.util.Set<Integer> internalStateIndices = new java.util.LinkedHashSet<>();
        definitions.keySet().forEach(id -> {
            Map<String, JsonElement> variants = blockOverrides.get(id);
            if (variants != null) {
                realBlockOverrides.put(id, variants);
            }
            RealBlockDefinition definition = definitions.get(id);
            definition.states().forEach(state -> internalStateIndices.add(state.customIndex()));
        });
        try {
            boolean canRefreshHostedPack = refreshHostedPack
                    && Config.autoUpload()
                    && craftEngine.packManager().resourcePackHost().canUpload();
            boolean patched;
            if (canRefreshHostedPack) {
                patched = RealBlockResourcePackPublisher.patchAndPublish(
                        resourcePackZip,
                        realBlockOverrides,
                        internalStateIndices,
                        craftEngine.packManager()::uploadResourcePack
                );
            } else {
                RealBlockResourcePackPatcher.patch(
                        resourcePackZip,
                        realBlockOverrides,
                        internalStateIndices
                );
                patched = resourcePackZip != null && java.nio.file.Files.exists(resourcePackZip);
            }
            if (!realBlockOverrides.isEmpty() && patched) {
                plugin.getLogger().info(
                        "Patched " + realBlockOverrides.size()
                                + " real blockstate file(s) into " + resourcePackZip
                );
            }
            if (canRefreshHostedPack && patched) {
                plugin.getLogger().info("Refreshed the hosted resource pack after real blockstate patching");
            }
        } catch (java.io.IOException | RuntimeException exception) {
            logger().warn("Failed to patch real blockstates into " + resourcePackZip, exception);
        }
    }

    private static net.momirealms.craftengine.core.plugin.config.ConfigParser findNativeBlockParser(
            BukkitCraftEngine craftEngine
    ) {
        return NativeBlockParserLookup.find(craftEngine.blockManager().parsers());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerRealBlockItemBehavior() {
        if (BuiltInRegistries.ITEM_BEHAVIOR_TYPE.containsKey(REAL_BLOCK_ITEM)) {
            return;
        }
        ItemBehaviors.register(REAL_BLOCK_ITEM, BukkitItemBehaviors.BLOCK_ITEM.factory());
    }

    private static void registerConnectableBlockBehavior() {
        ConnectableBlockBehavior.register();
    }

}
