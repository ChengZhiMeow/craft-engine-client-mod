package net.momirealms.craftengine.realblock.mixin;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.IdMapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(IdMapper.class)
public interface IdMapperAccessor<T> {
    @Accessor(value = "tToId", remap = false)
    Reference2IntMap<T> craftengine$toId();

    @Accessor(value = "idToT", remap = false)
    List<T> craftengine$byId();
}
