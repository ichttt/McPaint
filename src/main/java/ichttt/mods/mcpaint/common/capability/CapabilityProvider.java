package ichttt.mods.mcpaint.common.capability;

import ichttt.mods.mcpaint.MCPaint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation LOCATION = new ResourceLocation(MCPaint.MODID, "paintable");
    private final IPaintable paint = new Paint();
    private final LazyOptional<IPaintable> optional = LazyOptional.of(() -> paint);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CapabilityPaintable.PAINTABLE.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return CapabilityPaintable.writeToNBT(paint, new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CapabilityPaintable.readFromNBT(paint, nbt);
    }
}
