package net.momirealms.craftengine.realblock.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor<T> {
    @Accessor(value = "byLocation", remap = false)
    Map<Identifier, Holder.Reference<T>> craftengine$byLocation();

    @Accessor(value = "byKey", remap = false)
    Map<ResourceKey<T>, Holder.Reference<T>> craftengine$byKey();

    @Accessor(value = "byValue", remap = false)
    Map<T, Holder.Reference<T>> craftengine$byValue();

    @Accessor(value = "registrationInfos", remap = false)
    Map<ResourceKey<T>, RegistrationInfo> craftengine$registrationInfos();
}
