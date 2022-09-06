package ichttt.mods.mcpaint.common.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCanvas extends Block implements EntityBlock {
    //change clienteventhandler as well
    public static final BooleanProperty SOLID = BooleanProperty.create("solid");
    public static final BooleanProperty NORMAL_CUBE = BooleanProperty.create("normal_cube");

    public BlockCanvas(Material material) {
        super(Block.Properties.of(material).strength(1F, 4F).isRedstoneConductor((state, world, pos) -> state.getValue(NORMAL_CUBE)));
        registerDefaultState(stateDefinition.any().setValue(SOLID, true).setValue(NORMAL_CUBE, true));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityCanvas(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    //Delegating methods

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getDestroyProgress(player, world, pos);
        }
        return super.getDestroyProgress(state, player, world, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBlock().getExplosionResistance(canvas.getContainedState(), world, pos, explosion);
        }
        return super.getExplosionResistance(state, world, pos, explosion);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBlock().getSoundType(canvas.getContainedState(), world, pos, entity);
        }
        return super.getSoundType(state, world, pos, entity);
    }

    @Override
    public void playerDestroy(@Nonnull Level world, Player player, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        if (te instanceof TileEntityCanvas && ((TileEntityCanvas) te).getContainedState() != null) {
            TileEntityCanvas canvas = (TileEntityCanvas) te;
            state = canvas.getContainedState();
            state.getBlock().playerDestroy(world, player, pos, state, te, stack);
            return;
        }
        super.playerDestroy(world, player, pos, state, te, stack);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            return state.getBlock().canHarvestBlock(state, world, pos, player);
        }
        return super.canHarvestBlock(state, world, pos, player);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getCollisionShape(world, pos, context);
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getShape(world, pos, context);
        }
        return super.getShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            return canvas.getContainedState().getBlockSupportShape(world, pos);
        }
        return super.getOcclusionShape(state, world, pos);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TileEntityCanvas) { // Got some crashes because decocraft seems to wrap tileentites
            TileEntityCanvas canvas = (TileEntityCanvas) te;
            if (canvas.getContainedState() != null) {
                return canvas.getContainedState().getVisualShape(world, pos, context);
            }
        }
        return super.getVisualShape(state, world, pos, context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOLID, NORMAL_CUBE);
    }

    @Nonnull
    @Override
    public ItemStack getCloneItemStack(@Nonnull BlockState state, HitResult target, @Nonnull BlockGetter world, @Nonnull BlockPos pos, Player player) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            return state.getBlock().getCloneItemStack(canvas.getContainedState(), target, world, pos, player);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            return state.getBlock().propagatesSkylightDown(canvas.getContainedState(), world, pos);
        }
        return super.propagatesSkylightDown(state, world, pos);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            state.getBlock().animateTick(canvas.getContainedState(), world, pos, random);
            return;
        }
        super.animateTick(state, world, pos, random);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            return state.getBlock().getLightEmission(canvas.getContainedState(), world, pos);
        }
        return super.getLightEmission(state, world, pos);
    }

    @Override
    public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity, float pFallDistance) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            state.getBlock().fallOn(world, canvas.getContainedState(), pos, entity, pFallDistance);
            return;
        }
        super.fallOn(world, state, pos, entity, pFallDistance);
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            return state.getBlock().isCollisionShapeFullBlock(canvas.getContainedState(), world, pos);
        }
        return super.isCollisionShapeFullBlock(state, world, pos);
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            return state.getBlock().getBlockSupportShape(canvas.getContainedState(), world, pos);
        }
        return super.getBlockSupportShape(state, world, pos);
    }

    @Override
    public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            return state.getBlock().isOcclusionShapeFullBlock(canvas.getContainedState(), world, pos);
        }
        return super.isOcclusionShapeFullBlock(state, world, pos);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        TileEntityCanvas canvas = (TileEntityCanvas) world.getBlockEntity(pos);
        if (canvas != null && canvas.getContainedState() != null) {
            state = canvas.getContainedState();
            return state.getBlock().getInteractionShape(canvas.getContainedState(), world, pos);
        }
        return super.getInteractionShape(state, world, pos);
    }



    public BlockState getStateFrom(BlockGetter world, BlockPos pos, BlockState state) {
        return defaultBlockState().setValue(SOLID, state.canOcclude()).setValue(NORMAL_CUBE, state.isRedstoneConductor(world, pos));
    }
}
