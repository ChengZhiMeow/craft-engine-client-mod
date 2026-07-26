package net.momirealms.craftengine.neoforge.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor<T> {

    @Accessor("byLocation")
    Map<ResourceLocation, Holder.Reference<T>> ce$byLocation();

    @Accessor("byKey")
    Map<ResourceKey<T>, Holder.Reference<T>> ce$byKey();

    @Accessor("registrationInfos")
    Map<ResourceKey<T>, RegistrationInfo> ce$registrationInfos();
}
