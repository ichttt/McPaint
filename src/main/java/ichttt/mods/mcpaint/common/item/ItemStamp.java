package ichttt.mods.mcpaint.common.item;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.MCPaintConfig;
import ichttt.mods.mcpaint.client.ClientHooks;
import ichttt.mods.mcpaint.client.render.TEISRStamp;
import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ItemStamp extends ItemBrush {

    public ItemStamp(ResourceLocation registryName) {
        super(registryName);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> addPropertyOverride(new ResourceLocation(MCPaint.MODID, "shift"), TEISRStamp.INSTANCE));
    }

    @Override
    protected EnumActionResult processMiss(World world, EntityPlayer player, EnumHand hand, ItemStack stack, @Nullable RayTraceResult result) {
        if (result == null && player.isSneaking()) {
            stack.getCapability(CapabilityPaintable.PAINTABLE, null).orElseThrow(() -> new RuntimeException("Paintable cap needs to be present!")).clear(null, null);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    @Override
    protected EnumActionResult processHit(World world, EntityPlayer player, ItemStack held, BlockPos pos, IBlockState state, EnumFacing facing) {
        IPaintable paint = Objects.requireNonNull(held.getCapability(CapabilityPaintable.PAINTABLE, null).orElseThrow(() -> new RuntimeException("Missing paint on brush!")));
        if (paint.hasPaintData()) {
            return super.processHit(world, player, held, pos, state, facing);
        } else if (player != null && player.isSneaking()) {
            facing = facing.getOpposite();
            if (state.getBlock() instanceof BlockCanvas) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof TileEntityCanvas) {
                    TileEntityCanvas canvas = (TileEntityCanvas) te;
                    if (canvas.hasPaintFor(facing)) {
                        paint.copyFrom(canvas.getPaintFor(facing), canvas, facing);
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
        }
        return EnumActionResult.FAIL;
    }

    @Override
    protected void startPainting(TileEntityCanvas canvas, World world, ItemStack heldItem, BlockPos pos, EnumFacing facing, IBlockState state) {
        if (world.isRemote) {
            IPaintable heldPaint = Objects.requireNonNull(heldItem.getCapability(CapabilityPaintable.PAINTABLE, null).orElseThrow(() -> new RuntimeException("No paint in stamp")));
            if (MCPaintConfig.CLIENT.directApplyStamp.get()) {
                canvas.getPaintFor(facing).copyFrom(heldPaint, canvas, facing);
                MCPaintUtil.uploadPictureToServer(canvas, facing, heldPaint.getScaleFactor(), heldPaint.getPictureData(), false);
            } else {
                List<IPaintable> paintList = new LinkedList<>();
                if (canvas.hasPaintFor(facing)) {
                    paintList.add(canvas.getPaintFor(facing));
                }
                paintList.add(heldPaint);
                DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientHooks.showGuiDraw(paintList, pos, facing, canvas.getContainedState()));
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        IPaintable paintable = stack.getCapability(CapabilityPaintable.PAINTABLE, null).orElse(null);
        if (paintable != null && paintable.hasPaintData()) {
            tooltip.add(new TextComponentTranslation("mcpaint.tooltip.stamp.paint"));
        } else {
            tooltip.add(new TextComponentTranslation("mcpaint.tooltip.stamp.nopaint"));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
