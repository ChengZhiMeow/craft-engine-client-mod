package net.momirealms.craftengine.neoforge.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class BlockRenderUtils {
    private BlockRenderUtils() {}

    public static void registerColor(Block block, Block vanillaBlock) {
        var blockColors = Minecraft.getInstance().getBlockColors();
        int color = blockColors.getColor(vanillaBlock.defaultBlockState(), null, null, 0);
        if (color == -1) return;
        blockColors.register(CustomBlockColor.INSTANCE, block);
    }

    public static final class CustomBlockColor implements BlockColor {
        public static final CustomBlockColor INSTANCE = new CustomBlockColor();

        @Override
        public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int i) {
            if (blockAndTintGetter != null && blockPos != null) {
                return BiomeColors.getAverageFoliageColor(blockAndTintGetter, blockPos);
            }
            return FoliageColor.FOLIAGE_DEFAULT;
        }
    }
}
