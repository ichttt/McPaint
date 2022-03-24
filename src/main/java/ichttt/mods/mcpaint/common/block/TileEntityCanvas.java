package ichttt.mods.mcpaint.common.block;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.MCPaintConfig;
import ichttt.mods.mcpaint.client.render.buffer.BufferManager;
import ichttt.mods.mcpaint.client.render.batch.IOptimisationCallback;
import ichttt.mods.mcpaint.client.render.batch.RenderCache;
import ichttt.mods.mcpaint.client.render.batch.SimpleCallback;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.capability.IPaintValidator;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import ichttt.mods.mcpaint.common.capability.Paint;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.EffectiveSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class TileEntityCanvas extends BlockEntity implements IPaintValidator {
    public static final ModelProperty<BlockState> BLOCK_STATE_PROPERTY = new ModelProperty<>();
    private final Map<Direction, IPaintable> facingToPaintMap = new EnumMap<>(Direction.class);
    private BlockState containedState;
    private final Map<Direction, Object> bufferMap = new EnumMap<>(Direction.class);
    private final Set<Direction> disallowedFaces = EnumSet.noneOf(Direction.class);

    public TileEntityCanvas(BlockPos pos, BlockState state) {
        super(EventHandler.CANVAS_TE, pos, state);
    }

    @Nonnull
    @Override
    public void saveAdditional(CompoundTag tag) {
        if (this.containedState != null)
            tag.put("blockState", NbtUtils.writeBlockState(this.containedState));
        CompoundTag faces = new CompoundTag();
        for (Map.Entry<Direction, IPaintable> entry : this.facingToPaintMap.entrySet()) {
            faces.put(entry.getKey().getName(), CapabilityPaintable.writeToNBT(entry.getValue(), new CompoundTag()));
        }
        tag.put("faces", faces);
        if (!disallowedFaces.isEmpty()) {
            CompoundTag blockedFaces = new CompoundTag();
            for (Direction facing : Direction.values())
                blockedFaces.putBoolean(facing.getName(), disallowedFaces.contains(facing));
            tag.put("blocked", blockedFaces);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.containedState = tag.contains("blockState", Tag.TAG_COMPOUND) ? NbtUtils.readBlockState(tag.getCompound("blockState")) : null;
        CompoundTag faces = tag.getCompound("faces");
        for (String key : faces.getAllKeys()) {
            Paint paint = new Paint(this);
            CapabilityPaintable.readFromNBT(paint, faces.getCompound(key));
            this.facingToPaintMap.put(Direction.byName(key), paint);
        }
        disallowedFaces.clear();
        if (tag.contains("blocked")) {
            CompoundTag blockedFaces = tag.getCompound("blocked");
            for (String key : blockedFaces.getAllKeys()) {
                if (blockedFaces.getBoolean(key))
                    disallowedFaces.add(Direction.byName(key));
            }
        }
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        this.load(tag);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction facing) {
        if (cap == CapabilityPaintable.PAINTABLE)
            return LazyOptional.of(() -> (T) getPaintFor(facing));
        return super.getCapability(cap, facing);
    }

    @Override
    public boolean isValidPixelCount(short pixelCountX, short pixelCountY) {
        return (pixelCountX == 0 && pixelCountY == 0) || (pixelCountX == 128 && pixelCountY == 128);
    }

    public void setInitialData(BlockState state, Set<Direction> disallowedFaces) {
        this.containedState = state;
        this.disallowedFaces.addAll(disallowedFaces);
        this.setChanged();
        if (this.level.isClientSide)
            ModelDataManager.requestModelDataRefresh(this);
    }

    public BlockState getContainedState() {
        return this.containedState;
    }

    public IPaintable getPaintFor(Direction facing) {
        return facingToPaintMap.computeIfAbsent(facing, face -> new Paint(this));
    }

    public boolean hasPaintFor(Direction facing) {
        IPaintable paint = facingToPaintMap.get(facing);
        if (paint == null)
            return false;
        return paint.hasPaintData();
    }

    @OnlyIn(Dist.CLIENT)
    public BufferManager getBuffer(Direction facing) {
        Object obj = bufferMap.get(facing);
        if (obj instanceof BufferManager)
            return (BufferManager) obj;
        else if (obj instanceof IOptimisationCallback) { //already waiting
            return null;
        } else if (obj != null) {
            MCPaint.LOGGER.error("Unknown object " + obj);
            return null;
        } else {
            SimpleCallback callback = new SimpleCallback() {
                @Override
                public void provideFinishedBuffer(BufferManager builder) {
                    if (this.isInvalid()) return;
                    RenderCache.cache(getPaintFor(facing), builder);
                    bufferMap.put(facing, builder);
                }
            };
            bufferMap.put(facing, callback);
            RenderCache.getOrRequest(facingToPaintMap.get(facing), callback);
            return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void unbindBuffers() {
        for (Map.Entry<Direction, Object> entry : bufferMap.entrySet()) {
            Object obj = entry.getValue();
            if (obj instanceof SimpleCallback) {
                ((SimpleCallback) obj).invalidate();
            } else if (obj instanceof BufferManager) {
                RenderCache.cache(getPaintFor(entry.getKey()), (BufferManager) obj);
            }
        }
        bufferMap.clear();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
        unbindBuffers();
    }

    public void invalidateBuffer(Direction facing) {
        Object obj = bufferMap.remove(facing);
        if (obj instanceof SimpleCallback) {
            ((SimpleCallback) obj).invalidate();
        } else if (obj instanceof BufferManager) {
            RenderCache.uncache(getPaintFor(facing));
        } else if (obj != null){
            MCPaint.LOGGER.warn("Unknown obj " + obj);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            unbindBuffers();
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder().withInitial(BLOCK_STATE_PROPERTY, this.containedState).build();
    }

    public boolean isSideBlockedForPaint(Direction facing) {
        return disallowedFaces.contains(facing);
    }

    public void removePaint(Direction facing) {
        facingToPaintMap.remove(facing);
    }
}
