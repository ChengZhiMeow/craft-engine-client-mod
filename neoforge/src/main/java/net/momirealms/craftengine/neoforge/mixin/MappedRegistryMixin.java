package net.momirealms.craftengine.neoforge.mixin;

import net.minecraft.core.MappedRegistry;
import net.minecraft.world.level.block.Block;
import net.momirealms.craftengine.neoforge.block.BlockManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> {

    @Shadow @Nullable public abstract T byId(int i);

    @Inject(method = "refreshTagsInHolders", at = @At("TAIL"))
    public void ce$afterHandle(CallbackInfo ci) {
        if (byId(0) instanceof Block) {
            BlockManager manager = BlockManager.instance();
            if (manager != null) {
                manager.handleTags();
            }
        }
    }
}
