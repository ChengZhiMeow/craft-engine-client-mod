package net.momirealms.craftengine.neoforge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.momirealms.craftengine.neoforge.config.ModConfig;

public class ReloadCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("craftengine-client").then(Commands.literal("reload").executes(ReloadCommands::reloadConfig)));
        dispatcher.register(Commands.literal("cec").then(Commands.literal("reload").executes(ReloadCommands::reloadConfig)));
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        ModConfig.INSTANCE.loadConfig();
        context.getSource().sendSuccess(() -> Component.translatable("craftengine.reload.success").withStyle(ChatFormatting.GREEN), false);
        return 0;
    }
}
