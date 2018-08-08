package ichttt.mods.mcpaint;

import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public interface IProxy {

    void preInit();

    default void showGuiDraw(IPaintable canvas, BlockPos pos, EnumFacing facing, IBlockState state) {}

    default void showGuiDraw(BlockPos pos, EnumFacing facing, IBlockState state) {}
}
