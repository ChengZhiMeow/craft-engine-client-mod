package net.momirealms.craftengine.neoforge.util;

import net.momirealms.craftengine.neoforge.CraftEngineNeoForgeMod;
import net.momirealms.craftengine.neoforge.config.ModConfig;

import java.io.IOException;
import java.nio.file.Files;

public final class ConfigUtils {

    public static void saveDefaultResource() {
        if (!Files.exists(ModConfig.CONFIG_DIR)) {
            try {
                Files.createDirectories(ModConfig.CONFIG_DIR);
            } catch (IOException e) {
                CraftEngineNeoForgeMod.instance().logger().warn("Failed to create config directory", e);
            }
        }
        if (!Files.exists(ModConfig.CONFIG_PATH)) {
            ModConfig.INSTANCE.saveConfig();
        }
    }

}
