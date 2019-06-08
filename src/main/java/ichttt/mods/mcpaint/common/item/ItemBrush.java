package ichttt.mods.mcpaint.common.item;

import ichttt.mods.mcpaint.client.ClientHooks;
import ichttt.mods.mcpaint.client.render.TEISRStamp;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ItemBrush extends Item {
    public static Item.Properties getProperties() {
        Item.Properties properties = new Item.Properties(); //Workaround for classloading issues
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> properties.setTEISR(TEISRStamp::getInstance));
        properties.group(ItemGroup.DECORATIONS).maxStackSize(1).defaultMaxDamage(32);
        return properties;
    }

    public ItemBrush(ResourceLocation registryName) {
        super(getProperties());
        setRegistryName(registryName);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack held = player.getHeldItem(hand);
        RayTraceResult raytraceresult = this.rayTrace(world, player, false);
        if (raytraceresult == null || raytraceresult.type != RayTraceResult.Type.BLOCK)
            return new ActionResult<>(processMiss(world, player, hand, held, raytraceresult), held);
        BlockPos pos = raytraceresult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Direction facing = raytraceresult.sideHit;
        return new ActionResult<>(processHit(world, player, held, pos, state, facing), held);
    }

    protected ActionResultType processMiss(World world, PlayerEntity player, Hand hand, ItemStack stack, @Nullable RayTraceResult result) {
        return ActionResultType.FAIL;
    }

    protected ActionResultType processHit(World world, PlayerEntity player, ItemStack held, BlockPos pos, BlockState state, Direction facing) {
        if (state.getBlock() instanceof BlockCanvas) {
            TileEntityCanvas canvas = (TileEntityCanvas) Objects.requireNonNull(world.getTileEntity(pos));
            //We need to cache getBlockFaceShape as the method takes a world as an argument
            if (canvas.isSideBlockedForPaint(facing)) return ActionResultType.FAIL;
            startPainting(canvas, world, held, pos, facing.getOpposite(), state);
            held.damageItem(1, player);
            return ActionResultType.SUCCESS;
        }

        //TODO check isFullBlock
        if (state.getBlockFaceShape(world, pos, facing) == BlockFaceShape.SOLID && state.getMaterial().isOpaque() /*&& state.isFullBlock() == state.isFullCube()*/ &&
                state.isFullCube() == state.isBlockNormalCube() && state.getRenderType() == BlockRenderType.MODEL && !state.getBlock().hasTileEntity(state)) {
            Set<Direction> disallowedFaces = EnumSet.noneOf(Direction.class);
            for (Direction testFacing : Direction.values()) {
                if (state.getBlockFaceShape(world, pos, testFacing) != BlockFaceShape.SOLID)
                    disallowedFaces.add(testFacing);
            }
            if (state.getMaterial().isFlammable())
                world.setBlockState(pos, EventHandler.CANVAS_WOOD.getStateFrom(state));
            else if (state.getMaterial().isToolNotRequired())
                world.setBlockState(pos, EventHandler.CANVAS_GROUND.getStateFrom(state));
            else
                world.setBlockState(pos, EventHandler.CANVAS_ROCK.getStateFrom(state));
            TileEntityCanvas canvas = (TileEntityCanvas) Objects.requireNonNull(world.getTileEntity(pos));
            canvas.setInitialData(state, disallowedFaces);
            canvas.markDirty();
            startPainting(canvas, world, held, pos, facing.getOpposite(), state);
            held.damageItem(1, player);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    protected void startPainting(TileEntityCanvas canvas, World world, ItemStack heldItem, BlockPos pos, Direction facing, BlockState state) {
        if (world.isRemote) {
            if (canvas.hasPaintFor(facing)) {
                List<IPaintable> list = new ArrayList<>(1);
                list.add(canvas.getPaintFor(facing));
                DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientHooks.showGuiDraw(list, canvas.getPos(), facing, canvas.getContainedState()));
            } else {
                DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientHooks.showGuiDraw(pos, facing, canvas.getContainedState()));
            }
        }
    }
}
