package net.momirealms.craftengine.neoforge.mixin;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Collection;
import java.util.Set;

@Mixin(CreativeModeTab.class)
public interface CreativeModeTabAccessor {

    @Accessor("displayItems")
    void ce$displayItems(Collection<ItemStack> displayItems);

    @Accessor("displayItemsSearchTab")
    void ce$displayItemsSearchTab(Set<ItemStack> displayItems);
}
