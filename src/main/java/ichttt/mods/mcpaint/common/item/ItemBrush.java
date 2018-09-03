package ichttt.mods.mcpaint.common.item;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        facing = facing.getOpposite();
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == EventHandler.CANVAS) {
            ItemStack held = player.getHeldItem(hand);
            TileEntityCanvas canvas = (TileEntityCanvas) Objects.requireNonNull(world.getTileEntity(pos));
            startPainting(canvas, world, held, pos, facing, state);
            held.damageItem(1, player);
            return EnumActionResult.SUCCESS;
        }

        if (state.isFullBlock() && state.isFullCube() && state.isNormalCube() && state.isOpaqueCube() && state.isBlockNormalCube() &&
                state.getRenderType() == EnumBlockRenderType.MODEL && !state.getBlock().hasTileEntity(state)) {
            ItemStack held = player.getHeldItem(hand);
            world.setBlockState(pos, EventHandler.CANVAS.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, 0, player, hand));
            TileEntityCanvas canvas = (TileEntityCanvas) Objects.requireNonNull(world.getTileEntity(pos));
            canvas.setContainedBlockstate(state);
            canvas.markDirty();
            startPainting(canvas, world, held, pos, facing, state);
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

    @Override
    public void setTileEntityItemStackRenderer(@Nullable TileEntityItemStackRenderer teisr) {
        super.setTileEntityItemStackRenderer(teisr);
    }
}
