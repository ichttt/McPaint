package ichttt.mods.mcpaint.common.block;

import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.capability.IPaintValidator;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import ichttt.mods.mcpaint.common.capability.Paint;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class TileEntityCanvas extends TileEntity implements IPaintValidator {
    public final IPaintable paint = new Paint(this);
    private IBlockState containedState;

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        NBTUtil.writeBlockState(tag, containedState);
        return CapabilityPaintable.writeToNBT(this.paint, tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.containedState = NBTUtil.readBlockState(tag);
        CapabilityPaintable.readFromNBT(this.paint, tag);
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
            return CapabilityPaintable.PAINTABLE.cast(paint);
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityPaintable.PAINTABLE || super.hasCapability(capability, facing);
    }

    @Override
    public boolean isValidPixelCount(short pixelCountX, short pixelCountY) {
        return pixelCountX == 112 && pixelCountY == 112;
    }

    public EnumFacing getFacing() {
        return this.world.getBlockState(this.pos).getValue(BlockDirectional.FACING);
    }

    public void setContainedBlockstate(IBlockState state) {
        this.containedState = state;
        this.markDirty();
    }

    public IBlockState getContainedState() {
        return this.containedState;
    }
}