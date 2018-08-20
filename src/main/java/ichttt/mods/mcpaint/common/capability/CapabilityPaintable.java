package ichttt.mods.mcpaint.common.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;

public class CapabilityPaintable {

    @CapabilityInject(IPaintable.class)
    public static Capability<IPaintable> PAINTABLE;

    public static void register() {
        CapabilityManager.INSTANCE.register(IPaintable.class, new Capability.IStorage<IPaintable>() {
                    @Override
                    public NBTBase writeNBT(Capability<IPaintable> capability, IPaintable instance, EnumFacing side) {
                        return writeToNBT(instance, new NBTTagCompound());
                    }

                    @Override
                    public void readNBT(Capability<IPaintable> capability, IPaintable instance, EnumFacing side, NBTBase nbt) {
                        readFromNBT(instance, (NBTTagCompound) nbt);
                    }
                }, Paint::new);
    }

    public static NBTTagCompound writeToNBT(IPaintable instance, NBTTagCompound compound) {
        if (instance.hasPaintData()) {
            short pixelCountX = instance.getPixelCountX();
            short pixelCountY = instance.getPixelCountY();
            byte scaleFactor = instance.getScaleFactor();
            int[][] pictureData = instance.getPictureData();

            compound.setShort("pixelX", pixelCountX);
            compound.setShort("pixelY", pixelCountY);
            compound.setByte("scale", scaleFactor);

            NBTTagCompound pictureInfo = new NBTTagCompound();
            for (int i = 0; i < (pixelCountX / scaleFactor); i++) {
                pictureInfo.setIntArray("" + i, pictureData[i]);
            }

            compound.setTag("picture", pictureInfo);
            return compound;
        }
        return compound;
    }

    public static void readFromNBT(IPaintable instance, NBTTagCompound compound) {
        if (!compound.hasKey("scale", Constants.NBT.TAG_BYTE))
            return;
        short pixelCountX = compound.getShort("pixelX");
        short pixelCountY = compound.getShort("pixelY");
        byte scaleFactor = compound.getByte("scale");
        NBTTagCompound pictureInfo = compound.getCompoundTag("picture");
        int arraySize = pixelCountX / scaleFactor;
        int[][] pictureData = new int[arraySize][];
        for (int i = 0; i < (arraySize); i++) {
            pictureData[i] = pictureInfo.getIntArray("" + i);
        }
        instance.setData(scaleFactor, pictureData, false);
    }
}
