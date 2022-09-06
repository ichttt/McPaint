package ichttt.mods.mcpaint.common.item;

import ichttt.mods.mcpaint.client.ClientHooks;
import ichttt.mods.mcpaint.client.render.ISTERStamp;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.RegistryObjects;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class ItemBrush extends Item {

    public ItemBrush() {
        super(new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).stacksTo(1).defaultDurability(32));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ISTERStamp.INSTANCE;
            }
        });
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        HitResult raytraceresult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);
        if (raytraceresult.getType() != HitResult.Type.BLOCK)
            return new InteractionResultHolder<>(processMiss(world, player, hand, held, raytraceresult), held);
        BlockHitResult blockRayTraceResult = (BlockHitResult) raytraceresult;
        BlockPos pos = blockRayTraceResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Direction facing = blockRayTraceResult.getDirection();
        return new InteractionResultHolder<>(processHit(world, player, hand, pos, state, facing), held);
    }

    protected InteractionResult processMiss(Level world, Player player, InteractionHand hand, ItemStack stack, @Nullable HitResult result) {
        return InteractionResult.FAIL;
    }

    protected InteractionResult processHit(Level world, Player player, InteractionHand hand, BlockPos pos, BlockState state, Direction facing) {
        if (state.getBlock() instanceof BlockCanvas) {
            TileEntityCanvas canvas = (TileEntityCanvas) Objects.requireNonNull(world.getBlockEntity(pos));
            //We need to cache getBlockFaceShape as the method takes a world as an argument
            if (canvas.isSideBlockedForPaint(facing)) return InteractionResult.FAIL;
            ItemStack held = player.getItemInHand(hand);
            startPainting(canvas, world, held, pos, facing.getOpposite(), state);
            held.hurtAndBreak(1, player, (p_220282_1_) -> p_220282_1_.broadcastBreakEvent(hand));
            return InteractionResult.SUCCESS;
        }

        if (Block.canSupportCenter(world, pos, facing) && state.getMaterial().isSolidBlocking() /*&& state.isFullBlock() == state.isFullCube()*/ &&
                /*state.isFullCube() == state.isBlockNormalCube() &&*/ state.getRenderShape() == RenderShape.MODEL && !(state.getBlock() instanceof EntityBlock)) {
            Set<Direction> disallowedFaces = EnumSet.noneOf(Direction.class);
            for (Direction testFacing : Direction.values()) {
                if (!Block.canSupportCenter(world, pos, testFacing))
                    disallowedFaces.add(testFacing);
            }
            Block block = null;
            RegistryObject<Block> blockRegistryObject = RegistryObjects.CANVAS_BLOCKS.get(state.getMaterial());
            if (blockRegistryObject != null) {
                block = blockRegistryObject.get();
            }
            if (block == null) {
                if (state.getMaterial().isFlammable())
                    block = RegistryObjects.CANVAS_BLOCKS.get(Material.WOOD).get();
                else if (!state.requiresCorrectToolForDrops())
                    block = RegistryObjects.CANVAS_BLOCKS.get(Material.DIRT).get();
                else
                    block = RegistryObjects.CANVAS_BLOCKS.get(Material.STONE).get();
            }

            world.setBlockAndUpdate(pos, ((BlockCanvas) block).getStateFrom(world, pos, state));
            TileEntityCanvas canvas = (TileEntityCanvas) Objects.requireNonNull(world.getBlockEntity(pos));
            canvas.setInitialData(state, disallowedFaces);
            canvas.setChanged();
            ItemStack held = player.getItemInHand(hand);
            startPainting(canvas, world, held, pos, facing.getOpposite(), state);
            held.hurtAndBreak(1, player, (p_220282_1_) -> p_220282_1_.broadcastBreakEvent(hand));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    protected void startPainting(TileEntityCanvas canvas, Level world, ItemStack heldItem, BlockPos pos, Direction facing, BlockState state) {
        if (world.isClientSide) {
            if (canvas.hasPaintFor(facing)) {
                List<IPaintable> list = new ArrayList<>(1);
                list.add(canvas.getPaintFor(facing));
                DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientHooks.showGuiDraw(list, canvas.getBlockPos(), facing, canvas.getContainedState()));
            } else {
                DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientHooks.showGuiDraw(pos, facing, canvas.getContainedState()));
            }
        }
    }
}
