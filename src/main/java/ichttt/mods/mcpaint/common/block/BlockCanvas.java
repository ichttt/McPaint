package ichttt.mods.mcpaint.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockCanvas extends Block {
    public static final PropertyBool IS_FULL_BLOCK = PropertyBool.create("full_block");
    public static final PropertyBool IS_NORMAL_CUBE = PropertyBool.create("normal_cube");
    public static final PropertyBool IS_OPAQUE_CUBE = PropertyBool.create("opaque_cube");

    public BlockCanvas(Material material, ResourceLocation regNam) {
        super(material);
        setHardness(1F);
        setCreativeTab(CreativeTabs.DECORATIONS);
        setResistance(5F);
        useNeighborBrightness = true;
        setRegistryName(regNam);
        setTranslationKey(regNam.getNamespace() + "." + regNam.getPath());
        setDefaultState(blockState.getBaseState().withProperty(IS_FULL_BLOCK, true).withProperty(IS_NORMAL_CUBE, true).withProperty(IS_OPAQUE_CUBE, true));
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

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
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
            return canvas.getContainedState().getBlock().getSoundType(canvas.getContainedState(), world, pos, entity);
        }
        return super.getSoundType(state, world, pos, entity);
    }

    @Override
    public void harvestBlock(@Nonnull World world, EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        if (te instanceof TileEntityCanvas && ((TileEntityCanvas) te).getContainedState() != null) {
            TileEntityCanvas canvas = (TileEntityCanvas) te;
            state = canvas.getContainedState();
            state.getBlock().harvestBlock(world, player, pos, state, te, stack);
            return;
        }
        super.harvestBlock(world, player, pos, state, te, stack);
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBlock().canHarvestBlock(world, pos, player);
        }
        return super.canHarvestBlock(world, pos, player);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addCollisionBoxToList(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            canvas.getContainedState().getBlock().addCollisionBoxToList(canvas.getContainedState(), world, pos, entityBox, collidingBoxes, entityIn, isActualState);
            return;
        }
        super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entityIn, isActualState);
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getCollisionBoundingBox(world, pos);
        }
        return super.getCollisionBoundingBox(blockState, world, pos);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) source.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBoundingBox(source, pos);
        }
        return super.getBoundingBox(state, source, pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getMapColor(world, pos);
        }
        return super.getMapColor(state, world, pos);
    }


    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        //Return the contained state, needed for rendering
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState();
        }
        return super.getActualState(state, world, pos);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (state.getBlock() != this) //Possible when harvesting (as we send the technically wrong blockstate)
            return state.getBlock().getMetaFromState(state);
        //We got one bool rest...
        int meta = 0;
        if (!state.getValue(IS_OPAQUE_CUBE))
            meta = meta | 4;
        if (!state.getValue(IS_NORMAL_CUBE))
            meta = meta | 2;
        if (!state.getValue(IS_FULL_BLOCK))
            meta = meta | 1;
        return meta;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IS_FULL_BLOCK, IS_NORMAL_CUBE, IS_OPAQUE_CUBE);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        if ((meta & 4) > 0)
            state = state.withProperty(IS_OPAQUE_CUBE, false);
        if ((meta & 2) > 0)
            state = state.withProperty(IS_NORMAL_CUBE, false);
        if ((meta & 1) > 0)
            state = state.withProperty(IS_FULL_BLOCK, false);
        return state;
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            return state.getBlock().getPickBlock(canvas.getContainedState(), target, world, pos, player);
        }
        return ItemStack.EMPTY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullBlock(IBlockState state) {
        return state.getValue(IS_FULL_BLOCK);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        return state.getValue(IS_FULL_BLOCK);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isNormalCube(IBlockState state) {
        return state.getValue(IS_NORMAL_CUBE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return state.getValue(IS_OPAQUE_CUBE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return state.getValue(IS_FULL_BLOCK);
    }

    public IBlockState getStateFrom(IBlockState state) {
        return getDefaultState().withProperty(IS_OPAQUE_CUBE, state.isOpaqueCube()).withProperty(IS_NORMAL_CUBE, state.isNormalCube()).withProperty(IS_FULL_BLOCK, state.isFullBlock());
    }
}
