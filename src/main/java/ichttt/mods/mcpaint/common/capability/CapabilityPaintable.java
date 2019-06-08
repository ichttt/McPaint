package ichttt.mods.mcpaint.common.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityPaintable {

    @CapabilityInject(IPaintable.class)
    public static Capability<IPaintable> PAINTABLE;

    public static void register() {
        CapabilityManager.INSTANCE.register(IPaintable.class, new Capability.IStorage<IPaintable>() {
                    @Override
                    public INBT writeNBT(Capability<IPaintable> capability, IPaintable instance, Direction side) {
                        return writeToNBT(instance, new CompoundNBT());
                    }

                    @Override
                    public void readNBT(Capability<IPaintable> capability, IPaintable instance, Direction side, INBT nbt) {
                        readFromNBT(instance, (CompoundNBT) nbt);
                    }
                }, Paint::new);
    }

    public static CompoundNBT writeToNBT(IPaintable instance, CompoundNBT compound) {
        if (instance.hasPaintData()) {
            short pixelCountX = instance.getPixelCountX();
            byte scaleFactor = instance.getScaleFactor();
            int[][] pictureData = instance.getPictureData();

            compound.putShort("pixelX", pixelCountX);
            compound.putByte("scale", scaleFactor);

            CompoundNBT pictureInfo = new CompoundNBT();
            for (int i = 0; i < (pixelCountX / scaleFactor); i++) {
                pictureInfo.putIntArray("" + i, pictureData[i]);
            }

            compound.put("picture", pictureInfo);
            return compound;
        }
        return compound;
    }

    public static void readFromNBT(IPaintable instance, CompoundNBT compound) {
        if (!compound.contains("scale"))
            return;
        short pixelCountX = compound.getShort("pixelX");
        byte scaleFactor = compound.getByte("scale");
        CompoundNBT pictureInfo = compound.getCompound("picture");
        int arraySize = pixelCountX / scaleFactor;
        int[][] pictureData = new int[arraySize][];
        for (int i = 0; i < (arraySize); i++) {
            pictureData[i] = pictureInfo.getIntArray("" + i);
        }
        instance.setData(scaleFactor, pictureData, null, null);
    }
}
