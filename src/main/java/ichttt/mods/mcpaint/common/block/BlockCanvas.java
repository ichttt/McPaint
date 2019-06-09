package ichttt.mods.mcpaint.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCanvas extends Block {
    //change clienteventhandler as well
    public static final BooleanProperty SOLID = BooleanProperty.create("solid");
    public static final BooleanProperty NORMAL_CUBE = BooleanProperty.create("normal_cube");

    //TODO register a block for each common material
    public BlockCanvas(Material material, ResourceLocation regNam) {
        super(Block.Properties.create(material).hardnessAndResistance(1F, 4F));
        setRegistryName(regNam);
        setDefaultState(stateContainer.getBaseState().with(SOLID, true).with(NORMAL_CUBE, true));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntityCanvas();
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    //Delegating methods

    @SuppressWarnings("deprecation")
    @Override
    public float getBlockHardness(BlockState blockState, IBlockReader world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBlockHardness(world, pos);
        }
        return super.getBlockHardness(blockState, world, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBlock().getExplosionResistance(canvas.getContainedState(), world, pos, exploder, explosion);
        }
        return super.getExplosionResistance(state, world, pos, exploder, explosion);
    }

    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBlock().getSoundType(canvas.getContainedState(), world, pos, entity);
        }
        return super.getSoundType(state, world, pos, entity);
    }

    @Override
    public void harvestBlock(@Nonnull World world, PlayerEntity player, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable TileEntity te, ItemStack stack) {
        if (te instanceof TileEntityCanvas && ((TileEntityCanvas) te).getContainedState() != null) {
            TileEntityCanvas canvas = (TileEntityCanvas) te;
            state = canvas.getContainedState();
            state.getBlock().harvestBlock(world, player, pos, state, te, stack);
            return;
        }
        super.harvestBlock(world, player, pos, state, te, stack);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            return state.getBlock().canHarvestBlock(state, world, pos, player);
        }
        return super.canHarvestBlock(state, world, pos, player);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getCollisionShape(world, pos, context);
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getShape(world, pos, context);
        }
        return super.getShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getRenderShape(world, pos);
        }
        return super.getRenderShape(state, world, pos);
    }

    @Override
    public VoxelShape getRaytraceShape(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getRaytraceShape(world, pos);
        }
        return super.getRaytraceShape(state, world, pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public MaterialColor getMaterialColor(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getMaterialColor(world, pos);
        }
        return super.getMaterialColor(state, world, pos);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(SOLID, NORMAL_CUBE);
    }

    @Nonnull
    @Override
    public BlockState getExtendedState(@Nonnull BlockState state, IBlockReader world, BlockPos pos) {
        //Return the contained state, needed for rendering
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState();
        }
        return state;
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull BlockState state, RayTraceResult target, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlayerEntity player) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getTileEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            return state.getBlock().getPickBlock(canvas.getContainedState(), target, world, pos, player);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSolid(BlockState state) {
        return state.get(SOLID);
    }

    @Override
    public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos) {
        return state.get(NORMAL_CUBE);
    }

    public BlockState getStateFrom(IBlockReader world, BlockPos pos, BlockState state) {
        return getDefaultState().with(SOLID, state.isSolid()).with(NORMAL_CUBE, state.isNormalCube(world, pos));
    }
}
