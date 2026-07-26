package net.momirealms.craftengine.neoforge.jade;

import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.momirealms.craftengine.neoforge.CraftEngineNeoForgeMod;
import net.momirealms.craftengine.neoforge.block.BlockManager;
import net.momirealms.craftengine.neoforge.block.CraftEngineBlock;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@WailaPlugin("jade")
public final class CraftEngineJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CustomBlockProvider.INSTANCE, Block.class);
        registration.registerBlockIcon(CustomBlockProvider.INSTANCE, Block.class);
        registration.addRayTraceCallback((hitResult, accessor, originalAccessor) ->
                filterRayTraceTarget(registration, accessor));
    }

    private static @Nullable Accessor<?> filterRayTraceTarget(
            IWailaClientRegistration registration,
            @Nullable Accessor<?> accessor
    ) {
        if (accessor instanceof EntityAccessor entityAccessor) {
            boolean itemDisplay = entityAccessor.getEntity() instanceof ItemDisplay;
            String customName = entityAccessor.getEntity().getCustomName() == null
                    ? null
                    : entityAccessor.getEntity().getCustomName().getString();
            if (JadeBlockPresentation.ignoreDirectItemDisplay(itemDisplay, customName)) {
                return null;
            }
        }
        return redirectToItemDisplay(registration, accessor);
    }

    private static @Nullable Accessor<?> redirectToItemDisplay(
            IWailaClientRegistration registration,
            @Nullable Accessor<?> accessor
    ) {
        if (!(accessor instanceof BlockAccessor blockAccessor)
                || !(blockAccessor.getBlock() instanceof CraftEngineBlock)) {
            return accessor;
        }
        boolean realBlock = realBlockId(blockAccessor.getBlock()).isPresent();
        if (!JadeBlockPresentation.redirectToItemDisplay(realBlock)) {
            return accessor;
        }

        Vec3 hitLocation = blockAccessor.getHitResult().getLocation();
        AABB blockBounds = new AABB(blockAccessor.getPosition());
        ItemDisplay itemDisplay = blockAccessor.getLevel()
                .getEntitiesOfClass(
                        ItemDisplay.class,
                        blockBounds,
                        display -> JadeBlockPresentation.isUsableItemDisplay(
                                display.getSlot(0).get().isEmpty(),
                                display.getCustomName() == null ? null : display.getCustomName().getString()
                        )
                )
                .stream()
                .min(Comparator.comparingDouble(display -> display.position().distanceToSqr(hitLocation)))
                .orElse(null);
        if (itemDisplay == null) {
            return accessor;
        }

        return registration.entityAccessor()
                .serverConnected(false)
                .hit(new EntityHitResult(itemDisplay, hitLocation))
                .entity(itemDisplay)
                .build();
    }

    private enum CustomBlockProvider implements IBlockComponentProvider {
        INSTANCE;

        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(CraftEngineNeoForgeMod.MOD_ID, "jade_custom_block");

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            Optional<ResourceLocation> realBlockId = realBlockId(accessor.getBlock());
            if (realBlockId.isPresent()) {
                boolean hasRegistryTranslation = Language.getInstance().has(accessor.getBlock().getDescriptionId());
                if (JadeBlockPresentation.useRegistryPresentation(true, hasRegistryTranslation)) {
                    return;
                }
                replaceWithCraftEngineItem(tooltip, BlockItemModelMatcher.findById(realBlockId.get()));
                return;
            }

            Optional<ItemStack> item = BlockItemModelMatcher.find(accessor.getBlockState());
            replaceWithCraftEngineItem(tooltip, item);
        }

        @Override
        public @Nullable Element getIcon(BlockAccessor accessor, IPluginConfig config, Element currentIcon) {
            Optional<ResourceLocation> realBlockId = realBlockId(accessor.getBlock());
            if (realBlockId.isPresent()) {
                return BlockItemModelMatcher.findById(realBlockId.get())
                        .or(() -> BlockItemModelMatcher.find(accessor.getBlockState()))
                        .map(JadeUI::item)
                        .orElse(currentIcon);
            }
            return BlockItemModelMatcher.find(accessor.getBlockState())
                    .map(JadeUI::item)
                    .orElse(currentIcon);
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public int getDefaultPriority() {
            return TooltipPosition.TAIL;
        }

        @Override
        public boolean isRequired() {
            return true;
        }

        private static void replaceWithCraftEngineItem(ITooltip tooltip, Optional<ItemStack> item) {
            if (item.isEmpty()) {
                return;
            }
            tooltip.replace(JadeIds.CORE_OBJECT_NAME, IThemeHelper.get().title(item.get().getHoverName()));
            tooltip.replace(
                    JadeIds.CORE_MOD_NAME,
                    ignored -> List.of(List.of(IThemeHelper.get().modName("CraftEngine")))
            );
        }
    }

    private static Optional<ResourceLocation> realBlockId(Block block) {
        BlockManager blockManager = BlockManager.instance();
        return blockManager == null ? Optional.empty() : blockManager.realBlockId(block);
    }
}
