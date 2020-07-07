package ichttt.mods.mcpaint.common.item;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.MCPaintConfig;
import ichttt.mods.mcpaint.client.ClientHooks;
import ichttt.mods.mcpaint.client.render.ISTERStamp;
import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
//        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> addPropertyOverride(new ResourceLocation(MCPaint.MODID, "shift"), ISTERStamp.INSTANCE)); TODO validate
    }

    @Override
    protected ActionResultType processMiss(World world, PlayerEntity player, Hand hand, ItemStack stack, @Nullable RayTraceResult result) {
        if ((result == null || result.getType() == RayTraceResult.Type.MISS) && player.getPose() == Pose.CROUCHING) {
            IPaintable paint = stack.getCapability(CapabilityPaintable.PAINTABLE, null).orElseThrow(() -> new RuntimeException("Paintable cap needs to be present!"));
            if (paint.getPictureData() == null)
                return ActionResultType.PASS;
            paint.clear(null, null);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    protected ActionResultType processHit(World world, PlayerEntity player, Hand hand, BlockPos pos, BlockState state, Direction facing) {
        ItemStack held = player == null ? ItemStack.EMPTY : player.getHeldItem(hand);
        IPaintable paint = Objects.requireNonNull(held.getCapability(CapabilityPaintable.PAINTABLE, null).orElseThrow(() -> new RuntimeException("Missing paint on brush!")));
        if (paint.hasPaintData()) {
            return super.processHit(world, player, hand, pos, state, facing);
        } else if (player != null && player.getPose() == Pose.CROUCHING) {
            facing = facing.getOpposite();
            if (state.getBlock() instanceof BlockCanvas) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof TileEntityCanvas) {
                    TileEntityCanvas canvas = (TileEntityCanvas) te;
                    if (canvas.hasPaintFor(facing)) {
                        paint.copyFrom(canvas.getPaintFor(facing), canvas, facing);
                        return ActionResultType.SUCCESS;
                    }
                }
            }
        }
        return ActionResultType.FAIL;
    }

    @Override
    protected void startPainting(TileEntityCanvas canvas, World world, ItemStack heldItem, BlockPos pos, Direction facing, BlockState state) {
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
            tooltip.add(new TranslationTextComponent("mcpaint.tooltip.stamp.paint"));
        } else {
            tooltip.add(new TranslationTextComponent("mcpaint.tooltip.stamp.nopaint"));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt != null) {
            IPaintable paint = stack.getCapability(CapabilityPaintable.PAINTABLE).orElseThrow(() -> new IllegalArgumentException("Missing paintable on brush!"));
            CapabilityPaintable.readFromNBT(paint, nbt);
        }
    }

    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        IPaintable paint = stack.getCapability(CapabilityPaintable.PAINTABLE).orElseThrow(() -> new IllegalArgumentException("Missing paintable on brush!"));
        return CapabilityPaintable.writeToNBT(paint, new CompoundNBT());
    }
}
