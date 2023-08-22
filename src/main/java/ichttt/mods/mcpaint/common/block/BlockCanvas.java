package ichttt.mods.mcpaint.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCanvas extends Block implements EntityBlock {
    //change clienteventhandler as well
    public static final BooleanProperty SOLID = BooleanProperty.create("solid");
    public static final BooleanProperty NORMAL_CUBE = BooleanProperty.create("normal_cube");

    private static BlockState getContainedState(BlockGetter levelAccessor, BlockPos pos) {
        // This check guards access to the ServerLevel from offthread. This can happen during world upgrades and
        // causes a stall because the main thread waits for spawn chunks while this thread which is supposed to load
        // spawn chunks is waiting on the main thread to access the chunk cache
        // The status check ensures this early exit is only taken on startup
        // It is a giant hack but oh well
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null && !currentServer.isSameThread() && levelAccessor instanceof ServerLevel && currentServer.getStatus() == null) {
            return null;
        }
        BlockEntity blockEntity = levelAccessor.getExistingBlockEntity(pos);
        if (blockEntity instanceof TileEntityCanvas canvas) {
            return canvas.getContainedState();
        }
        return null;
    }

    private static boolean canRenderEmissiveProp(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.emissiveRendering(level, pos);
        }
        // default
        return false;
    }

    private static boolean isValidSpawnProp(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> entityType) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.isValidSpawn(level, pos, entityType);
        }
        // default
        return state.isFaceSturdy(level, pos, Direction.UP) && state.getLightEmission(level, pos) < 14;
    }

    private static boolean isSuffocatingProp(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.isSuffocating(level, pos);
        }
        // default
        return state.blocksMotion() && state.isCollisionShapeFullBlock(level, pos);
    }

    private static boolean isRedstoneConductorProp(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.isRedstoneConductor(level, pos);
        }
        // default
        return state.isCollisionShapeFullBlock(level, pos);
    }

    public BlockCanvas() {
        // TODO check for noCollision and pushReaction
        super(Block.Properties.of()
                .strength(1F, 4F)
                .isRedstoneConductor(BlockCanvas::isRedstoneConductorProp)
                .emissiveRendering(BlockCanvas::canRenderEmissiveProp)
                .isValidSpawn(BlockCanvas::isValidSpawnProp)
                .isSuffocating(BlockCanvas::isSuffocatingProp)
                .dynamicShape()
        );
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
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getDestroyProgress(player, level, pos);
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getExplosionResistance(level, pos, explosion);
        }
        return super.getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getSoundType(level, pos, entity);
        }
        return super.getSoundType(state, level, pos, entity);
    }

    @Override
    public void playerDestroy(@Nonnull Level level, Player player, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        // Use provided BlockEntity
        if (te instanceof TileEntityCanvas canvas) {
            BlockState containedState = canvas.getContainedState();
            if (containedState != null) {
                containedState.getBlock().playerDestroy(level, player, pos, containedState, te, stack);
                return;
            }
        }
        super.playerDestroy(level, player, pos, state, te, stack);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.canHarvestBlock(level, pos, player);
        }
        return super.canHarvestBlock(state, level, pos, player);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getCollisionShape(level, pos, context);
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getShape(level, pos, context);
        }
        return super.getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getBlockSupportShape(level, pos);
        }
        return super.getOcclusionShape(state, level, pos);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getVisualShape(level, pos, context);
        }
        return super.getVisualShape(state, level, pos, context);
    }

    @Nonnull
    @Override
    public ItemStack getCloneItemStack(@Nonnull BlockState state, HitResult target, @Nonnull BlockGetter level, @Nonnull BlockPos pos, Player player) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getCloneItemStack(target, level, pos, player);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.propagatesSkylightDown(level, pos);
        }
        return super.propagatesSkylightDown(state, level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            containedState.getBlock().animateTick(containedState, level, pos, random);
            return;
        }
        super.animateTick(state, level, pos, random);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getLightEmission(level, pos);
        }
        return super.getLightEmission(state, level, pos);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float pFallDistance) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            containedState.getBlock().fallOn(level, containedState, pos, entity, pFallDistance);
            return;
        }
        super.fallOn(level, state, pos, entity, pFallDistance);
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.isCollisionShapeFullBlock(level, pos);
        }
        return super.isCollisionShapeFullBlock(state, level, pos);
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getBlockSupportShape(level, pos);
        }
        return super.getBlockSupportShape(state, level, pos);
    }

    @Override
    public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getBlock().isOcclusionShapeFullBlock(containedState, level, pos);
        }
        return super.isOcclusionShapeFullBlock(state, level, pos);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getInteractionShape(level, pos);
        }
        return super.getInteractionShape(state, level, pos);
    }


    @Override
    public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getBlock().hidesNeighborFace(level, pos, containedState, neighborState, dir);
        }
        return super.hidesNeighborFace(level, pos, state, neighborState, dir);
    }

    @Override
    public MapColor getMapColor(BlockState state, BlockGetter level, BlockPos pos, MapColor defaultColor) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getMapColor(level, pos);
        }
        return super.getMapColor(state, level, pos, defaultColor);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState containedState = getContainedState(level, pos);
        if (containedState != null) {
            return containedState.getLightBlock(level, pos);
        }
        return super.getLightBlock(state, level, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOLID, NORMAL_CUBE);
    }

    public BlockState getStateFrom(BlockGetter world, BlockPos pos, BlockState state) {
        return defaultBlockState().setValue(SOLID, state.canOcclude()).setValue(NORMAL_CUBE, state.isRedstoneConductor(world, pos));
    }
}
