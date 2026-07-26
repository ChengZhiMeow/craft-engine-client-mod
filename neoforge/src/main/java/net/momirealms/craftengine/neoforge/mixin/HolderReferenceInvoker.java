package net.momirealms.craftengine.neoforge.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(Holder.Reference.class)
public interface HolderReferenceInvoker<T> {

    @Invoker("bindValue")
    void ce$callBindValue(T object);

    @Mutable
    @Accessor("key")
    void ce$key(ResourceKey<T> key);

    @Accessor("tags")
    Set<TagKey<T>> ce$tags();

    @Accessor("tags")
    void ce$tags(Set<TagKey<T>> tags);
}
