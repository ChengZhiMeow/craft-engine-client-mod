package net.momirealms.craftengine.neoforge.jade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.momirealms.craftengine.neoforge.CraftEngineNeoForgeMod;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
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

import java.util.List;
import java.util.Optional;

@WailaPlugin("jade")
public final class CraftEngineJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CustomBlockProvider.INSTANCE, Block.class);
        registration.registerBlockIcon(CustomBlockProvider.INSTANCE, Block.class);
    }

    private enum CustomBlockProvider implements IBlockComponentProvider {
        INSTANCE;

        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(CraftEngineNeoForgeMod.MOD_ID, "jade_custom_block");

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            Optional<ItemStack> item = BlockItemModelMatcher.find(accessor.getBlockState());
            if (item.isEmpty()) {
                return;
            }

            tooltip.replace(JadeIds.CORE_OBJECT_NAME, IThemeHelper.get().title(item.get().getHoverName()));
            tooltip.replace(
                    JadeIds.CORE_MOD_NAME,
                    ignored -> List.of(List.of(IThemeHelper.get().modName("CraftEngine")))
            );
        }

        @Override
        public @Nullable Element getIcon(BlockAccessor accessor, IPluginConfig config, Element currentIcon) {
            return BlockItemModelMatcher.find(accessor.getBlockState())
                    .map(JadeUI::item)
                    .orElse(null);
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
    }
}
