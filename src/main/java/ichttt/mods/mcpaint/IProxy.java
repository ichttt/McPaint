package ichttt.mods.mcpaint;

import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface IProxy {

    void preInit();

    default void showGuiDraw(List<IPaintable> canvasList, BlockPos pos, EnumFacing facing, IBlockState state) {}

    default void showGuiDraw(BlockPos pos, EnumFacing facing, IBlockState state) {}

    default void onConfigReload() {}

    default void invalidateCache(IPaintable paint, TileEntityCanvas canvas, EnumFacing facing) {}
}
