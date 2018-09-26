package ichttt.mods.mcpaint.common.item;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.*;

public class ItemBrush extends Item {

    public ItemBrush(ResourceLocation registryName) {
        setCreativeTab(CreativeTabs.DECORATIONS);
        setRegistryName(registryName);
        setTranslationKey(registryName.getNamespace() + "." + registryName.getPath());
        setMaxStackSize(1);
        setMaxDamage(32);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == EventHandler.CANVAS) {
            ItemStack held = player.getHeldItem(hand);
            TileEntityCanvas canvas = (TileEntityCanvas) Objects.requireNonNull(world.getTileEntity(pos));
            //We need to cache getBlockFaceShape as the method takes a world as an argument
            if (canvas.isSideBlockedForPaint(facing)) return EnumActionResult.FAIL;
            startPainting(canvas, world, held, pos, facing.getOpposite(), state);
            held.damageItem(1, player);
            return EnumActionResult.SUCCESS;
        }

        //TODO substates for props... we need 5, we have 4... state.isFullCube = this.getDefaultState().isOpaqueCube
        if (state.getBlockFaceShape(world, pos, facing) == BlockFaceShape.SOLID && state.getMaterial().isOpaque() && state.isFullBlock() == state.isFullCube() &&
                state.isFullCube() == state.isBlockNormalCube() && state.getRenderType() == EnumBlockRenderType.MODEL && !state.getBlock().hasTileEntity(state)) {
            Set<EnumFacing> disallowedFaces = EnumSet.noneOf(EnumFacing.class);
            for (EnumFacing testFacing : EnumFacing.VALUES) {
                if (state.getBlockFaceShape(world, pos, testFacing) != BlockFaceShape.SOLID)
                    disallowedFaces.add(testFacing);
            }
            ItemStack held = player.getHeldItem(hand);
            world.setBlockState(pos, EventHandler.CANVAS.getStateFrom(state));
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
                MCPaint.proxy.showGuiDraw(list, canvas.getPos(), facing, canvas.getContainedState());
            } else {
                MCPaint.proxy.showGuiDraw(pos, facing, canvas.getContainedState());
            }
        }
    }
}
