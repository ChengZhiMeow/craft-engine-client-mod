package net.momirealms.craftengine.neoforge.commands;

import net.momirealms.craftengine.neoforge.CraftEngineNeoForgeMod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

public class CommandManager {
    private static CommandManager instance;
    private final CraftEngineNeoForgeMod mod;

    public CommandManager(CraftEngineNeoForgeMod mod) {
        instance = this;
        this.mod = mod;
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    public static CommandManager instance() {
        return instance;
    }

    private void registerCommands(RegisterClientCommandsEvent event) {
        ReloadCommands.register(event.getDispatcher());
    }
}
