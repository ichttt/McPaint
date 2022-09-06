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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ItemStamp extends ItemBrush {

    public ItemStamp() {}

    @Override
    protected InteractionResult processMiss(Level world, Player player, InteractionHand hand, ItemStack stack, @Nullable HitResult result) {
        if ((result == null || result.getType() == HitResult.Type.MISS) && player.getPose() == Pose.CROUCHING) {
            IPaintable paint = stack.getCapability(CapabilityPaintable.PAINTABLE, null).orElseThrow(() -> new RuntimeException("Paintable cap needs to be present!"));
            if (!paint.hasPaintData())
                return InteractionResult.PASS;
            paint.clear(null, null);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult processHit(Level world, Player player, InteractionHand hand, BlockPos pos, BlockState state, Direction facing) {
        ItemStack held = player == null ? ItemStack.EMPTY : player.getItemInHand(hand);
        IPaintable paint = Objects.requireNonNull(held.getCapability(CapabilityPaintable.PAINTABLE, null).orElseThrow(() -> new RuntimeException("Missing paint on brush!")));
        if (paint.hasPaintData()) {
            return super.processHit(world, player, hand, pos, state, facing);
        } else if (player != null && player.getPose() == Pose.CROUCHING) {
            facing = facing.getOpposite();
            if (state.getBlock() instanceof BlockCanvas) {
                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof TileEntityCanvas) {
                    TileEntityCanvas canvas = (TileEntityCanvas) te;
                    if (canvas.hasPaintFor(facing)) {
                        paint.copyFrom(canvas.getPaintFor(facing), canvas, facing);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    protected void startPainting(TileEntityCanvas canvas, Level world, ItemStack heldItem, BlockPos pos, Direction facing, BlockState state) {
        if (world.isClientSide) {
            IPaintable heldPaint = Objects.requireNonNull(heldItem.getCapability(CapabilityPaintable.PAINTABLE, null).orElseThrow(() -> new RuntimeException("No paint in stamp")));
            if (MCPaintConfig.CLIENT.directApplyStamp.get()) {
                canvas.getPaintFor(facing).copyFrom(heldPaint, canvas, facing);
                MCPaintUtil.uploadPictureToServer(canvas, facing, heldPaint.getScaleFactor(), heldPaint.getPictureData(true), false);
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
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        IPaintable paintable = stack.getCapability(CapabilityPaintable.PAINTABLE, null).orElse(null);
        if (paintable != null && paintable.hasPaintData()) {
            tooltip.add(Component.translatable("mcpaint.tooltip.stamp.paint"));
        } else {
            tooltip.add(Component.translatable("mcpaint.tooltip.stamp.nopaint"));
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt != null) {
            IPaintable paint = stack.getCapability(CapabilityPaintable.PAINTABLE).orElseThrow(() -> new IllegalArgumentException("Missing paintable on brush!"));
            CapabilityPaintable.readFromNBT(paint, nbt);
        }
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        IPaintable paint = stack.getCapability(CapabilityPaintable.PAINTABLE).orElseThrow(() -> new IllegalArgumentException("Missing paintable on brush!"));
        return CapabilityPaintable.writeToNBT(paint, new CompoundTag());
    }
}
