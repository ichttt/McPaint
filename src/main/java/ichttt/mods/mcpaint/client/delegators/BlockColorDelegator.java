package ichttt.mods.mcpaint.client.delegators;

import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockColorDelegator implements IBlockColor {

    @Override
    public int getColor(BlockState blockState, @Nullable IEnviromentBlockReader world, @Nullable BlockPos pos, int tintIndex) {
        if (world == null || pos == null) return -1;
        TileEntity entity = world.getTileEntity(pos);
        if (entity instanceof TileEntityCanvas) {
            TileEntityCanvas canvas = (TileEntityCanvas) entity;
            BlockState contained = canvas.getContainedState();
            if (contained != null) {
                return Minecraft.getInstance().getBlockColors().func_216860_a(contained, world, pos, tintIndex);
            }
        }
        return -1;
    }
}