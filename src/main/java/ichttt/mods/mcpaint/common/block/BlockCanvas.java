package ichttt.mods.mcpaint.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCanvas extends Block {
    public BlockCanvas(ResourceLocation regNam) {
        super(Material.WOOD);
        setHardness(1F);
        setCreativeTab(CreativeTabs.DECORATIONS);
        setResistance(5F);
        useNeighborBrightness = true;
        this.setRegistryName(regNam);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityCanvas();
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    //Delegating methods

    @SuppressWarnings("deprecation")
    @Override
    public float getBlockHardness(IBlockState blockState, World world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBlockHardness(world, pos);
        }
        return super.getBlockHardness(blockState, world, pos);
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBlock().getExplosionResistance(world, pos, exploder, explosion);
        }
        return super.getExplosionResistance(world, pos, exploder, explosion);
    }

    @Nonnull
    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBlock().getSoundType(state, world, pos, entity);
        }
        return super.getSoundType(state, world, pos, entity);
    }

    @Override
    public void harvestBlock(@Nonnull World world, EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        if (te instanceof TileEntityCanvas && ((TileEntityCanvas) te).getContainedState() != null) {
            state = ((TileEntityCanvas) te).getContainedState();
            state.getBlock().harvestBlock(world, player, pos, state, te, stack);
            return;
        }
        super.harvestBlock(world, player, pos, state, te, stack);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasCustomBreakingProgress(IBlockState state) {
        return true;
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBlock().canHarvestBlock(world, pos, player);
        }
        return super.canHarvestBlock(world, pos, player);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        //HARD AND REALLY DIRTY HACK: BE WARNED IF YOU CONTINUE READING
        //See if we are called from ForgeHooks#canHarvestBlock
        //if yes, return the contained state
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace.length > 2 && stackTrace[2].toString().contains("net.minecraftforge.common.ForgeHooks.canHarvestBlock")) {
                return canvas.getContainedState();
            }
        }
        return super.getActualState(state, world, pos);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (state.getBlock() != this) //Possible when harvesting (as we send the technically wrong blockstate)
            return state.getBlock().getMetaFromState(state);
        return super.getMetaFromState(state);
    }
}
