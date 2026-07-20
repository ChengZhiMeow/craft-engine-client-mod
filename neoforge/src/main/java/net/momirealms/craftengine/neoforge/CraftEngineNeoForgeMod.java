package net.momirealms.craftengine.neoforge;

import net.minecraft.core.registries.Registries;
import net.momirealms.craftengine.neoforge.block.BlockManager;
import net.momirealms.craftengine.neoforge.commands.CommandManager;
import net.momirealms.craftengine.neoforge.config.ModConfig;
import net.momirealms.craftengine.neoforge.config.ModMenuIntegration;
import net.momirealms.craftengine.neoforge.item.ItemManager;
import net.momirealms.craftengine.neoforge.logger.LoggerFilter;
import net.momirealms.craftengine.neoforge.logger.ModLogger;
import net.momirealms.craftengine.neoforge.logger.Slf4jModLogger;
import net.momirealms.craftengine.neoforge.network.NetworkManager;
import net.momirealms.craftengine.neoforge.util.ConfigUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@Mod(value = CraftEngineNeoForgeMod.MOD_ID, dist = Dist.CLIENT)
public class CraftEngineNeoForgeMod {
    public static final String MOD_ID = "craftengine";
    private static CraftEngineNeoForgeMod instance;
    private Path configPath;
    private ModLogger logger;
    private NetworkManager networkManager;
    private BlockManager blockManager;
    private CommandManager commandManager;
    private ItemManager itemManager;

    public CraftEngineNeoForgeMod(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;
        LoggerFilter.filter();
        ConfigUtils.saveDefaultResource();
        ModConfig.INSTANCE.loadConfig();
        this.networkManager = new NetworkManager(this);
        this.commandManager = new CommandManager(this);
        modEventBus.addListener(NetworkManager::registerPayloads);
        modEventBus.addListener(this::registerDynamicContent);
        modEventBus.addListener(this::clientSetup);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (IConfigScreenFactory) (container, parent) -> ModMenuIntegration.createConfigScreen(parent));
    }

    private void registerDynamicContent(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.BLOCK) {
            this.blockManager = new BlockManager(this);
        } else if (event.getRegistryKey() == Registries.CREATIVE_MODE_TAB) {
            this.itemManager = new ItemManager(this);
        }
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> blockManager().completeRegistration());
    }

    public static CraftEngineNeoForgeMod instance() {
        return instance;
    }

    public ModLogger logger() {
        if (logger == null) {
            logger = new Slf4jModLogger(LoggerFactory.getLogger(MOD_ID));
        }
        return logger;
    }

    public Path dataFolderPath() {
        if (configPath == null) {
            configPath = FMLPaths.CONFIGDIR.get().resolve("craftengine");
        }
        return configPath;
    }

    public NetworkManager networkManager() {
        if (networkManager == null) {
            throw new IllegalStateException("NetworkManager not initialized");
        }
        return networkManager;
    }

    public BlockManager blockManager() {
        if (blockManager == null) {
            throw new IllegalStateException("BlockManager not initialized");
        }
        return blockManager;
    }

    public CommandManager commandManager() {
        if (commandManager == null) {
            throw new IllegalStateException("CommandManager not initialized");
        }
        return commandManager;
    }

    public ItemManager itemManager() {
        if (itemManager == null) {
            throw new IllegalStateException("ItemManager not initialized");
        }
        return itemManager;
    }
}
