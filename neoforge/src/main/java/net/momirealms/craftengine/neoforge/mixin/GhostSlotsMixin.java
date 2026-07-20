package net.momirealms.craftengine.neoforge.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.momirealms.craftengine.neoforge.config.ModConfig;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GhostSlots.class)
public class GhostSlotsMixin {

    @ModifyExpressionValue(
            method = "lambda$render$0",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/screens/recipebook/GhostSlots$GhostSlot;isResultSlot:Z",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 1
            ),
            remap = false
    )
    private boolean ce$modifyIsResultSlot(boolean original) {
        return ModConfig.INSTANCE.forceGhostRecipeShowInputItemStackCount() || original;
    }
}
