package ichttt.mods.mcpaint.common.block;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.render.CachedBufferBuilder;
import ichttt.mods.mcpaint.client.render.batch.RenderCache;
import ichttt.mods.mcpaint.client.render.batch.SimpleCallback;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.capability.IPaintValidator;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import ichttt.mods.mcpaint.common.capability.Paint;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class TileEntityCanvas extends TileEntity implements IPaintValidator {
    private final Map<EnumFacing, IPaintable> facingToPaintMap = new EnumMap<>(EnumFacing.class);
    private IBlockState containedState;
    private final Map<EnumFacing, Object> bufferMap = new EnumMap<>(EnumFacing.class);

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        NBTUtil.writeBlockState(tag, containedState);
        NBTTagCompound faces = new NBTTagCompound();
        for (Map.Entry<EnumFacing, IPaintable> entry : this.facingToPaintMap.entrySet()) {
            faces.setTag(entry.getKey().getName(), CapabilityPaintable.writeToNBT(entry.getValue(), new NBTTagCompound()));
        }
        tag.setTag("faces", faces);
        return tag;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0 || pass == 1;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.containedState = NBTUtil.readBlockState(tag);
        NBTTagCompound faces = tag.getCompoundTag("faces");
        for (String key : faces.getKeySet()) {
            Paint paint = new Paint(this);
            CapabilityPaintable.readFromNBT(paint, faces.getCompoundTag(key));
            this.facingToPaintMap.put(EnumFacing.byName(key), paint);
        }
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
        this.readFromNBT(tag);
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityPaintable.PAINTABLE)
            return CapabilityPaintable.PAINTABLE.cast(getPaintFor(facing));
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityPaintable.PAINTABLE || super.hasCapability(capability, facing);
    }

    @Override
    public boolean isValidPixelCount(short pixelCountX, short pixelCountY) {
        return pixelCountX == 128 && pixelCountY == 128;
    }

    public void setContainedBlockstate(IBlockState state) {
        this.containedState = state;
        this.markDirty();
    }

    public IBlockState getContainedState() {
        return this.containedState;
    }

    public IPaintable getPaintFor(EnumFacing facing) {
        return facingToPaintMap.computeIfAbsent(facing, face -> new Paint(this));
    }

    public boolean hasPaintFor(EnumFacing facing) {
        IPaintable paint = facingToPaintMap.get(facing);
        if (paint == null)
            return false;
        return paint.hasPaintData();
    }

    @SideOnly(Side.CLIENT)
    public CachedBufferBuilder getBuffer(EnumFacing facing) {
        Object obj = bufferMap.get(facing);
        if (obj instanceof CachedBufferBuilder)
            return (CachedBufferBuilder) obj;
        else if (obj instanceof IOptimisationCallback) { //already waiting
            return null;
        } else if (obj != null) {
            MCPaint.LOGGER.error("Unknown object " + obj);
            return null;
        } else {
            SimpleCallback callback = new SimpleCallback() {
                @Override
                public void provideFinishedBuffer(CachedBufferBuilder builder) {
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

    @SideOnly(Side.CLIENT)
    public void invalidateBuffers() {
        for (Map.Entry<EnumFacing, Object> entry : bufferMap.entrySet()) {
            Object obj = entry.getValue();
            if (obj instanceof SimpleCallback) {
                ((SimpleCallback) obj).invalidate();
            } else if (obj instanceof CachedBufferBuilder) {
                RenderCache.cache(getPaintFor(entry.getKey()), (CachedBufferBuilder) obj);
            }
        }
        bufferMap.clear();
    }

    @Override
    public double getMaxRenderDistanceSquared() { //128 for block, paint is limited in TE to 96
        return 128D * 128D;
    }

    public void invalidateBuffer(EnumFacing facing) {
        Object obj = bufferMap.remove(facing);
        if (obj instanceof SimpleCallback) {
            ((SimpleCallback) obj).invalidate();
        } else if (obj instanceof CachedBufferBuilder) {
            RenderCache.uncache(getPaintFor(facing));
        }
    }
}
