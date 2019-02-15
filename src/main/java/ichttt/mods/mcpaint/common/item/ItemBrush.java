package ichttt.mods.mcpaint.common.item;

import ichttt.mods.mcpaint.client.ClientHooks;
import ichttt.mods.mcpaint.client.render.TEISRStamp;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
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

    public ItemBrush(ResourceLocation registryName) {
        super(new Item.Properties().setTEISR(() -> () -> TEISRStamp.INSTANCE).group(ItemGroup.DECORATIONS).maxStackSize(1).defaultMaxDamage(32));
        setRegistryName(registryName);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        RayTraceResult raytraceresult = this.rayTrace(world, player, false);
        if (raytraceresult == null || raytraceresult.type != RayTraceResult.Type.BLOCK)
            return new ActionResult<>(processMiss(world, player, hand, held, raytraceresult), held);
        BlockPos pos = raytraceresult.getBlockPos();
        IBlockState state = world.getBlockState(pos);
        EnumFacing facing = raytraceresult.sideHit;
        return new ActionResult<>(processHit(world, player, held, pos, state, facing), held);
    }

    protected EnumActionResult processMiss(World world, EntityPlayer player, EnumHand hand, ItemStack stack, @Nullable RayTraceResult result) {
        return EnumActionResult.FAIL;
    }

    protected EnumActionResult processHit(World world, EntityPlayer player, ItemStack held, BlockPos pos, IBlockState state, EnumFacing facing) {
        if (state.getBlock() instanceof BlockCanvas) {
            TileEntityCanvas canvas = (TileEntityCanvas) Objects.requireNonNull(world.getTileEntity(pos));
            //We need to cache getBlockFaceShape as the method takes a world as an argument
            if (canvas.isSideBlockedForPaint(facing)) return EnumActionResult.FAIL;
            startPainting(canvas, world, held, pos, facing.getOpposite(), state);
            held.damageItem(1, player);
            return EnumActionResult.SUCCESS;
        }

        //TODO check isFullBlock
        if (state.getBlockFaceShape(world, pos, facing) == BlockFaceShape.SOLID && state.getMaterial().isOpaque() /*&& state.isFullBlock() == state.isFullCube()*/ &&
                state.isFullCube() == state.isBlockNormalCube() && state.getRenderType() == EnumBlockRenderType.MODEL && !state.getBlock().hasTileEntity(state)) {
            Set<EnumFacing> disallowedFaces = EnumSet.noneOf(EnumFacing.class);
            for (EnumFacing testFacing : EnumFacing.values()) {
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
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    protected void startPainting(TileEntityCanvas canvas, World world, ItemStack heldItem, BlockPos pos, EnumFacing facing, IBlockState state) {
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
