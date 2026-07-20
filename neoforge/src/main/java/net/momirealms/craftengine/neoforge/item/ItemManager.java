package net.momirealms.craftengine.neoforge.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.momirealms.craftengine.neoforge.CraftEngineNeoForgeMod;
import net.momirealms.craftengine.neoforge.jade.BlockItemModelMatcher;
import net.momirealms.craftengine.neoforge.mixin.CreativeModeInventoryScreenAccessor;
import net.momirealms.craftengine.neoforge.mixin.CreativeModeTabAccessor;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class ItemManager {
    private static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ResourceLocation.fromNamespaceAndPath("craftengine", "tab"));
    private static ItemManager instance;
    private final CraftEngineNeoForgeMod mod;
    private final CreativeModeTab tab;
    private List<ItemStack> creativeTabItems = List.of();

    public ItemManager(CraftEngineNeoForgeMod mod) {
        instance = this;
        this.mod = mod;
        this.tab = CreativeModeTab.builder()
                .icon(() -> new ItemStack(Items.NETHER_STAR))
                .title(Component.literal("CraftEngine"))
                .displayItems(($, output) -> {
                    if (creativeTabItems == null || creativeTabItems.isEmpty()) return;
                    List<ItemStack> temp = creativeTabItems;
                    for (ItemStack itemStack : temp) {
                        output.accept(itemStack);
                    }
                })
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KEY, this.tab);
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> clearCreativeTabItems());
    }

    public static ItemManager instance() {
        return instance;
    }

    public void loadFromNetwork(@NotNull List<ItemStack> creativeTabItems) {
        if (creativeTabItems.isEmpty()) {
            CreativeModeInventoryScreenAccessor.setSelectedTab(CreativeModeTabs.getDefaultTab());
        }
        this.creativeTabItems = creativeTabItems;
        BlockItemModelMatcher.invalidate();
        ((CreativeModeTabAccessor) this.tab).ce$displayItems(creativeTabItems);
        ((CreativeModeTabAccessor) this.tab).ce$displayItemsSearchTab(new HashSet<>(creativeTabItems));
    }

    public List<ItemStack> creativeTabItems() {
        return new ArrayList<>(creativeTabItems);
    }

    public void clearCreativeTabItems() {
        this.creativeTabItems = List.of();
        BlockItemModelMatcher.invalidate();
        CreativeModeInventoryScreenAccessor.setSelectedTab(CreativeModeTabs.getDefaultTab());
    }
}
