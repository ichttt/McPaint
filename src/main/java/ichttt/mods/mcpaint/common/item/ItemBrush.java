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
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nonnull;
import java.util.*;

public class ItemBrush extends Item {

    public ItemBrush(ResourceLocation registryName) {
        super(new Item.Builder().setTEISR(() -> () -> TEISRStamp.INSTANCE).group(ItemGroup.DECORATIONS).maxStackSize(1).defaultMaxDamage(32));
        setRegistryName(registryName);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        EntityPlayer player = context.getPlayer();
        BlockPos pos = context.getPos();
        ItemStack held = context.getItem();
        EnumFacing facing = context.getFace();
        IBlockState state = world.getBlockState(pos);
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
