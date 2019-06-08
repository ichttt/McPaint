package ichttt.mods.mcpaint.common.capability;

import ichttt.mods.mcpaint.MCPaint;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProvider implements ICapabilitySerializable<CompoundNBT> {
    public static final ResourceLocation LOCATION = new ResourceLocation(MCPaint.MODID, "paintable");
    private final IPaintable paint = new Paint();

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityPaintable.PAINTABLE)
            return LazyOptional.of(() -> (T) paint);
        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        return CapabilityPaintable.writeToNBT(paint, new CompoundNBT());
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        CapabilityPaintable.readFromNBT(paint, nbt);
    }
}
