package net.momirealms.craftengine.realblock.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Holder.Reference.class)
public interface HolderReferenceAccessor<T> {
    @Accessor(value = "key", remap = false)
    @Mutable
    void craftengine$setKey(ResourceKey<T> key);
}
