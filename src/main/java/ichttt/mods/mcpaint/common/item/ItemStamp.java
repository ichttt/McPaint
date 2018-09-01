package ichttt.mods.mcpaint.common.item;

import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ItemStamp extends ItemBrush {

    public ItemStamp(ResourceLocation registryName) {
        super(registryName);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack hold = player.getHeldItem(hand);
        IPaintable paint = Objects.requireNonNull(hold.getCapability(CapabilityPaintable.PAINTABLE, null));
        if (paint.hasPaintData()) {
            return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
        } else {
            facing = facing.getOpposite();
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() == EventHandler.CANVAS) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof TileEntityCanvas) {
                    TileEntityCanvas canvas = (TileEntityCanvas) te;
                    if (canvas.hasPaintFor(facing)) {
                        paint.copyFrom(canvas.getPaintFor(facing));
                        System.out.println("COPY");
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
        }
        return EnumActionResult.FAIL;
    }

    @Override
    protected void startPainting(TileEntityCanvas canvas, World world, ItemStack heldItem, BlockPos pos, EnumFacing facing, IBlockState state) {
        canvas.getPaintFor(facing).copyFrom(heldItem.getCapability(CapabilityPaintable.PAINTABLE, null));
    }
}
