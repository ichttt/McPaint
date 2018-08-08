package ichttt.mods.mcpaint.common.item;

import ichttt.mods.mcpaint.client.gui.GuiDraw;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ItemBrush extends Item {

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        facing = facing.getOpposite();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (state.getBlock() == EventHandler.CANVAS) {
            TileEntityCanvas canvas = (TileEntityCanvas) Objects.requireNonNull(world.getTileEntity(pos));
            if (canvas.hasPaintFor(facing)) {
                if (world.isRemote)
                    Minecraft.getMinecraft().displayGuiScreen(new GuiDraw(canvas.getPaintFor(facing), canvas.getPos(), facing, canvas.getContainedState()));
            } else {
                if (world.isRemote)
                    Minecraft.getMinecraft().displayGuiScreen(new GuiDraw((byte) 2, pos, facing, state));
            }
            return EnumActionResult.SUCCESS;
        }

        if (state.isFullBlock() && state.isFullCube() && state.isNormalCube() && state.isOpaqueCube() && !state.isTranslucent() && state.isBlockNormalCube() &&
                state.getRenderType() == EnumBlockRenderType.MODEL && !block.hasTileEntity(state)) {
            world.setBlockState(pos, EventHandler.CANVAS.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, 0, player, hand));
            TileEntityCanvas canvas = (TileEntityCanvas) Objects.requireNonNull(world.getTileEntity(pos));
            canvas.setContainedBlockstate(state);
            if (world.isRemote) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiDraw((byte) 2, pos, facing, state));
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }
}
