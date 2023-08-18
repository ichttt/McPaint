package ichttt.mods.mcpaint.client.delegators;

import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockColorDelegator implements BlockColor {

    @Override
    public int getColor(@Nonnull BlockState blockState, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex) {
        if (world == null || pos == null) return -1;
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof TileEntityCanvas canvas) {
            BlockState contained = canvas.getContainedState();
            if (contained != null) {
                return Minecraft.getInstance().getBlockColors().getColor(contained, world, pos, tintIndex);
            }
        }
        return -1;
    }
}
