package net.momirealms.craftengine.neoforge.mixin;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(Holder.Reference.class)
public interface HolderReferenceInvoker<T> {

    @Invoker("bindValue")
    void ce$callBindValue(T object);

    @Accessor("tags")
    Set<TagKey<T>> ce$tags();

    @Accessor("tags")
    void ce$tags(Set<TagKey<T>> tags);
}
