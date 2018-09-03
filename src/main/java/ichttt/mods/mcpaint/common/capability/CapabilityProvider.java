package ichttt.mods.mcpaint.common.capability;

import ichttt.mods.mcpaint.MCPaint;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProvider implements ICapabilitySerializable<NBTTagCompound> {
    public static final ResourceLocation LOCATION = new ResourceLocation(MCPaint.MODID, "paintable");
    private final IPaintable paint = new Paint();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityPaintable.PAINTABLE;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityPaintable.PAINTABLE)
            return (T) paint;
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return CapabilityPaintable.writeToNBT(paint, new NBTTagCompound());
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        CapabilityPaintable.readFromNBT(paint, nbt);
    }
}
