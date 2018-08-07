package ichttt.mods.mcpaint.common.block;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCanvas extends BlockDirectional {

    public BlockCanvas() {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockDirectional.FACING, EnumFacing.NORTH));
        setHardness(1F);
        setCreativeTab(CreativeTabs.DECORATIONS);
        setResistance(5F);
        useNeighborBrightness = true;
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

    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, EnumHand hand)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockDirectional.FACING);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(BlockDirectional.FACING, EnumFacing.byIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockDirectional.FACING).getIndex();
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
}
