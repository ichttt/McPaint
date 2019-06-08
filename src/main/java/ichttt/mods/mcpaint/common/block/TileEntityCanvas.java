package ichttt.mods.mcpaint.common.block;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.MCPaintConfig;
import ichttt.mods.mcpaint.client.render.BufferManager;
import ichttt.mods.mcpaint.client.render.CachedBufferBuilder;
import ichttt.mods.mcpaint.client.render.batch.IOptimisationCallback;
import ichttt.mods.mcpaint.client.render.batch.RenderCache;
import ichttt.mods.mcpaint.client.render.batch.SimpleCallback;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.capability.IPaintValidator;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import ichttt.mods.mcpaint.common.capability.Paint;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class TileEntityCanvas extends TileEntity implements IPaintValidator {
    public static final ModelProperty<BlockState> BLOCK_STATE_PROPERTY = new ModelProperty<>();
    private final Map<Direction, IPaintable> facingToPaintMap = new EnumMap<>(Direction.class);
    private BlockState containedState;
    private final Map<Direction, Object> bufferMap = new EnumMap<>(Direction.class);
    private final Set<Direction> disallowedFaces = EnumSet.noneOf(Direction.class);

    public TileEntityCanvas() {
        super(EventHandler.CANVAS_TE);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag = super.write(tag);
        tag.put("blockState", NBTUtil.writeBlockState(this.containedState));
        CompoundNBT faces = new CompoundNBT();
        for (Map.Entry<Direction, IPaintable> entry : this.facingToPaintMap.entrySet()) {
            faces.put(entry.getKey().getName(), CapabilityPaintable.writeToNBT(entry.getValue(), new CompoundNBT()));
        }
        tag.put("faces", faces);
        if (!disallowedFaces.isEmpty()) {
            CompoundNBT blockedFaces = new CompoundNBT();
            for (Direction facing : Direction.values())
                blockedFaces.putBoolean(facing.getName(), disallowedFaces.contains(facing));
            tag.put("blocked", blockedFaces);
        }
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        this.containedState = NBTUtil.readBlockState(tag.getCompound("blockState"));
        CompoundNBT faces = tag.getCompound("faces");
        for (String key : faces.keySet()) {
            Paint paint = new Paint(this);
            CapabilityPaintable.readFromNBT(paint, faces.getCompound(key));
            this.facingToPaintMap.put(Direction.byName(key), paint);
        }
        disallowedFaces.clear();
        if (tag.contains("blocked")) {
            CompoundNBT blockedFaces = tag.getCompound("blocked");
            for (String key : blockedFaces.keySet()) {
                if (blockedFaces.getBoolean(key))
                    disallowedFaces.add(Direction.byName(key));
            }
        }
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        this.read(tag);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
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
        this.markDirty();
        if (this.world.isRemote)
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
    public double getMaxRenderDistanceSquared() { //add 8 so we catch when were are no longer rendered
        int distOffset = MCPaintConfig.CLIENT.maxPaintRenderDistance.get() + 8;
        return distOffset * distOffset;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(pkt.getNbtCompound());
        unbindBuffers();
    }

    public void invalidateBuffer(Direction facing) {
        Object obj = bufferMap.remove(facing);
        if (obj instanceof SimpleCallback) {
            ((SimpleCallback) obj).invalidate();
        } else if (obj instanceof CachedBufferBuilder) {
            RenderCache.uncache(getPaintFor(facing));
        }
    }

    @Override
    public void onChunkUnloaded() {
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
