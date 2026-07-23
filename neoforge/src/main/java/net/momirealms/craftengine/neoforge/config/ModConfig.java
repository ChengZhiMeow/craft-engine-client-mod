package net.momirealms.craftengine.neoforge.config;

import net.momirealms.craftengine.neoforge.CraftEngineNeoForgeMod;
import net.momirealms.sparrow.yaml.SparrowYaml;
import net.momirealms.sparrow.yaml.YamlDocument;
import net.momirealms.sparrow.yaml.route.Route;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    public static final ModConfig INSTANCE = new ModConfig();
    public static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("craftengine");
    public static final Path CONFIG_PATH = CONFIG_DIR.resolve("config.yml");
    private static final SparrowYaml YAML = SparrowYaml.builder().build();
    private boolean enableClientCustomBlock = false;
    private boolean enableMinecraft12111ServerCompatibility = false;
    private boolean enableCancelBlockUpdate = false;
    private int serverSideBlocks = 10000;
    private boolean disableResourcePackLoadingScreen = false;
    private boolean forceGhostRecipeShowInputItemStackCount = false;

    private ModConfig() {
    }

    public boolean enableClientCustomBlock() {
        return enableClientCustomBlock;
    }

    public void enableClientCustomBlock(boolean enable) {
        this.enableClientCustomBlock = enable;
    }

    public boolean enableMinecraft12111ServerCompatibility() {
        return enableMinecraft12111ServerCompatibility;
    }

    public void enableMinecraft12111ServerCompatibility(boolean enable) {
        this.enableMinecraft12111ServerCompatibility = enable;
    }

    public boolean enableCancelBlockUpdate() {
        return enableCancelBlockUpdate;
    }

    public void enableCancelBlockUpdate(boolean enable) {
        this.enableCancelBlockUpdate = enable;
    }

    public int serverSideBlocks() {
        return serverSideBlocks;
    }

    public void serverSideBlocks(int size) {
        this.serverSideBlocks = size;
    }

    public boolean disableResourcePackLoadingScreen() {
        return disableResourcePackLoadingScreen;
    }

    public void disableResourcePackLoadingScreen(boolean disable) {
        this.disableResourcePackLoadingScreen = disable;
    }

    public boolean forceGhostRecipeShowInputItemStackCount() {
        return forceGhostRecipeShowInputItemStackCount;
    }

    public void forceGhostRecipeShowInputItemStackCount(boolean enable) {
        this.forceGhostRecipeShowInputItemStackCount = enable;
    }

    public void saveConfig() {
        try {
            YamlDocument document = Files.exists(CONFIG_PATH) ? YAML.load(CONFIG_PATH) : YAML.load("");
            document.set(Route.from("enable-client-custom-block"), enableClientCustomBlock());
            document.set(Route.from("enable-minecraft-1-21-11-server-compatibility"), enableMinecraft12111ServerCompatibility());
            document.set(Route.from("enable-cancel-block-update"), enableCancelBlockUpdate());
            document.set(Route.from("server-side-blocks"), serverSideBlocks());
            document.set(Route.from("disable-resource-pack-loading-screen"), disableResourcePackLoadingScreen());
            document.set(Route.from("force-ghost-recipe-show-input-itemstack-count"), forceGhostRecipeShowInputItemStackCount());
            document.save(CONFIG_PATH);
        } catch (Throwable e) {
            CraftEngineNeoForgeMod.instance().logger().warn("Failed to save config file", e);
        }
    }

    public void loadConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            setDefaultConfig();
            saveConfig();
            return;
        }
        try {
            YamlDocument document = YAML.load(CONFIG_PATH);
            enableClientCustomBlock(document.getOrDefault(Boolean.class, false, "enable-client-custom-block"));
            enableMinecraft12111ServerCompatibility(document.getOrDefault(Boolean.class, false, "enable-minecraft-1-21-11-server-compatibility"));
            enableCancelBlockUpdate(document.getOrDefault(Boolean.class, false, "enable-cancel-block-update"));
            serverSideBlocks(document.getOrDefault(Integer.class, 10000, "server-side-blocks"));
            disableResourcePackLoadingScreen(document.getOrDefault(Boolean.class, false, "disable-resource-pack-loading-screen"));
            forceGhostRecipeShowInputItemStackCount(document.getOrDefault(Boolean.class, false, "force-ghost-recipe-show-input-itemstack-count"));
        } catch (Throwable e) {
            CraftEngineNeoForgeMod.instance().logger().severe("Failed to load config", e);
        }
    }

    private void setDefaultConfig() {
        enableClientCustomBlock(false);
        enableMinecraft12111ServerCompatibility(false);
        enableCancelBlockUpdate(false);
        serverSideBlocks(10000);
        disableResourcePackLoadingScreen(false);
        forceGhostRecipeShowInputItemStackCount(false);
    }
}
