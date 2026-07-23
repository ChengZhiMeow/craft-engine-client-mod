package net.momirealms.craftengine.viacompat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public final class CraftEngineViaCompatibilityPlugin extends JavaPlugin {
    private static final int MINECRAFT_1_21_11_BLOCK_STATE_COUNT = 29_671;
    private static final int MAX_PATCH_ATTEMPTS = 200;
    private static final List<String> PROTOCOLS = List.of(
            "com.viaversion.viabackwards.protocol.v1_21_11to1_21_9.Protocol1_21_11To1_21_9"
    );

    private ViaBlockStateMappingPatch mappingPatch;
    private Plugin viaVersion;
    private Plugin viaBackwards;
    private BukkitTask retryTask;
    private int attempts;

    @Override
    public void onEnable() {
        Plugin craftEngine = requirePlugin("CraftEngine", true);
        this.viaVersion = requirePlugin("ViaVersion", false);
        this.viaBackwards = requirePlugin("ViaBackwards", false);

        int serverSideBlocks = readServerSideBlockCount(craftEngine);
        int extendedRegistrySize = Math.addExact(MINECRAFT_1_21_11_BLOCK_STATE_COUNT, serverSideBlocks);
        verifyMinecraftBlockStateRegistrySize(extendedRegistrySize);

        this.mappingPatch = new ViaBlockStateMappingPatch(
                this.viaBackwards.getClass().getClassLoader(),
                PROTOCOLS,
                MINECRAFT_1_21_11_BLOCK_STATE_COUNT,
                extendedRegistrySize
        );
        this.retryTask = Bukkit.getScheduler().runTaskTimer(this, this::tryApplyPatch, 0L, 1L);
    }

    @Override
    public void onDisable() {
        if (this.retryTask != null) {
            this.retryTask.cancel();
            this.retryTask = null;
        }
        if (this.mappingPatch != null) {
            this.mappingPatch.restore();
            this.mappingPatch = null;
        }
    }

    private void tryApplyPatch() {
        this.attempts++;
        try {
            if (!this.viaVersion.isEnabled() || !this.viaBackwards.isEnabled()) {
                throw new ViaBlockStateMappingPatch.MappingsNotReadyException();
            }
            int patchedMappings = this.mappingPatch.apply();
            this.retryTask.cancel();
            this.retryTask = null;
            getLogger().info(
                    "CraftEngine block state IDs [" + this.mappingPatch.firstExtendedId() + ", "
                            + this.mappingPatch.extendedRegistrySize() + ") now pass through "
                            + patchedMappings + " ViaBackwards mappings unchanged"
            );
        } catch (ViaBlockStateMappingPatch.MappingsNotReadyException ignored) {
            if (this.attempts < MAX_PATCH_ATTEMPTS) {
                return;
            }
            getLogger().severe("ViaBackwards mappings were not ready after " + MAX_PATCH_ATTEMPTS + " ticks");
            Bukkit.getPluginManager().disablePlugin(this);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            getLogger().severe("Unable to patch ViaBackwards block state mappings: " + exception);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private Plugin requirePlugin(String name, boolean mustBeEnabled) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        if (plugin == null) {
            throw new IllegalStateException(name + " must be installed");
        }
        if (mustBeEnabled && !plugin.isEnabled()) {
            throw new IllegalStateException(name + " must be enabled");
        }
        return plugin;
    }

    private int readServerSideBlockCount(Plugin craftEngine) {
        File configFile = new File(craftEngine.getDataFolder(), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        int count = config.getInt("block.serverside-blocks", -1);
        if (count <= 0) {
            throw new IllegalStateException(
                    "CraftEngine block.serverside-blocks must be a positive integer, got " + count
            );
        }
        return count;
    }

    private void verifyMinecraftBlockStateRegistrySize(int expectedSize) {
        try {
            Class<?> blockClass = Class.forName("net.minecraft.world.level.block.Block");
            Field registryField = blockClass.getField("BLOCK_STATE_REGISTRY");
            Object registry = registryField.get(null);
            Method sizeMethod = registry.getClass().getMethod("size");
            int actualSize = (int) sizeMethod.invoke(registry);
            if (actualSize != expectedSize) {
                throw new IllegalStateException(
                        "Minecraft block state registry size mismatch: expected " + expectedSize
                                + " from Minecraft 1.21.11 + CraftEngine config, got " + actualSize
                );
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to verify Minecraft block state registry size", exception);
        }
    }
}
