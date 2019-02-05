package ichttt.mods.mcpaint.common.capability;

import ichttt.mods.mcpaint.MCPaint;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProvider implements ICapabilitySerializable<NBTTagCompound> {
    public static final ResourceLocation LOCATION = new ResourceLocation(MCPaint.MODID, "paintable");
    private final IPaintable paint = new Paint();

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (cap == CapabilityPaintable.PAINTABLE)
            return LazyOptional.of(() -> (T) paint);
        return LazyOptional.empty();
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
