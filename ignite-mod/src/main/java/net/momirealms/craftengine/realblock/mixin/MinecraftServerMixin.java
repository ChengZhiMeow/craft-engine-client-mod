package net.momirealms.craftengine.realblock.mixin;

import net.minecraft.server.MinecraftServer;
import net.momirealms.craftengine.realblock.RealBlockRegistryBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "loadLevel", at = @At("HEAD"), remap = false)
    private void craftengine$installPersistedRealBlocks(String levelName, CallbackInfo callback) {
        RealBlockRegistryBridge.installPersisted();
    }
}
