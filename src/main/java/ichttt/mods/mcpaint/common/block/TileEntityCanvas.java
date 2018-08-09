package ichttt.mods.mcpaint.common.block;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class TileEntityCanvas extends TileEntity implements IPaintValidator {
    private final Map<EnumFacing, IPaintable> facingToPaintMap = new EnumMap<>(EnumFacing.class);
    private IBlockState containedState;

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

    @Override
    public double getMaxRenderDistanceSquared() { //128 for block, paint is limited in TE to 96
        return 128D * 128D;
    }
}
