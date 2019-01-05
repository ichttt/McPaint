package ichttt.mods.mcpaint.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCanvas extends Block implements ITileEntityProvider {
    public static final BooleanProperty IS_FULL_BLOCK = BooleanProperty.create("full_block");
    public static final BooleanProperty IS_NORMAL_CUBE = BooleanProperty.create("normal_cube");

    //TODO register a block for each common material
    public BlockCanvas(Material material, ResourceLocation regNam) {
        super(Block.Builder.create(material).hardnessAndResistance(1F, 4F));
//        useNeighborBrightness = true; TODO
        setRegistryName(regNam);
        setDefaultState(stateContainer.getBaseState().with(IS_FULL_BLOCK, true).with(IS_NORMAL_CUBE, true));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader reader) {
        return new TileEntityCanvas();
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    //Delegating methods

    @SuppressWarnings("deprecation")
    @Override
    public float getBlockHardness(IBlockState blockState, IBlockReader world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBlockHardness(world, pos);
        }
        return super.getBlockHardness(blockState, world, pos);
    }

    @Override
    public float getExplosionResistance(IBlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBlock().getExplosionResistance(canvas.getContainedState(), world, pos, exploder, explosion);
        }
        return super.getExplosionResistance(state, world, pos, exploder, explosion);
    }

    @Override
    public SoundType getSoundType(IBlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
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
    public boolean canHarvestBlock(IBlockState state, IBlockReader world, BlockPos pos, EntityPlayer player) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            return state.getBlock().canHarvestBlock(state, world, pos, player);
        }
        return super.canHarvestBlock(state, world, pos, player);
    }

    @Override
    public VoxelShape getCollisionShape(IBlockState state, IBlockReader world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getCollisionShape(world, pos);
        }
        return super.getCollisionShape(state, world, pos);
    }

    @Override
    public VoxelShape getShape(IBlockState state, IBlockReader world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getShape(world, pos);
        }
        return super.getShape(state, world, pos);
    }

    @Override
    public VoxelShape getRenderShape(IBlockState state, IBlockReader world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getRenderShape(world, pos);
        }
        return super.getRenderShape(state, world, pos);
    }

    @Override
    public VoxelShape getRaytraceShape(IBlockState state, IBlockReader world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getRaytraceShape(world, pos);
        }
        return super.getRaytraceShape(state, world, pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public MapColor getMapColor(IBlockState state, IBlockReader world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getMapColor(world, pos);
        }
        return super.getMapColor(state, world, pos);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(IS_FULL_BLOCK, IS_NORMAL_CUBE);
    }

    @Nonnull
    @Override
    public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockReader world, BlockPos pos) {
        //Return the contained state, needed for rendering
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState();
        }
        return state;
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull IBlockReader world, @Nonnull BlockPos pos, EntityPlayer player) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            return state.getBlock().getPickBlock(canvas.getContainedState(), target, world, pos, player);
        }
        return ItemStack.EMPTY;
    }


    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        return state.get(IS_FULL_BLOCK);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isNormalCube(IBlockState state) {
        return state.get(IS_NORMAL_CUBE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return state.get(IS_FULL_BLOCK);
    }

    public IBlockState getStateFrom(IBlockState state) {
        return getDefaultState().with(IS_NORMAL_CUBE, state.isNormalCube()).with(IS_FULL_BLOCK, state.isBlockNormalCube());
    }
}
