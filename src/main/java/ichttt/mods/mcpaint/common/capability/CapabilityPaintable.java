package ichttt.mods.mcpaint.common.capability;

import ichttt.mods.mcpaint.common.MCPaintUtil;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CapabilityPaintable {

    public static Capability<IPaintable> PAINTABLE = CapabilityManager.get(new CapabilityToken<>() {});;

    public static void register() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(CapabilityPaintable::onRegisterCapabilities);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        // Registers the capability classes
        event.register(Paint.class);
    }

    public static CompoundTag writeToNBT(IPaintable instance, CompoundTag compound) {
        if (instance.hasPaintData()) {
            short pixelCountX = instance.getPixelCountX();
            byte scaleFactor = instance.getScaleFactor();
            int[][] pictureData = instance.getPictureData(true);

            compound.putShort("pixelX", pixelCountX);
            compound.putByte("scale", scaleFactor);

            int[] palette = instance.getPalette();
            Int2ByteMap reversePalette = null;
            if (palette != null) {
                compound.putIntArray("palette", palette);
                reversePalette = MCPaintUtil.buildReversePalette(palette);
            }

            ListTag listTag = new ListTag();
            for (int i = 0; i < (pixelCountX / scaleFactor); i++) {
                int[] row = pictureData[i];
                if (palette == null) {
                    listTag.add(new IntArrayTag(row));
                } else {
                    byte[] asByteDat = new byte[row.length];
                    for (int j = 0; j < row.length; j++) {
                        asByteDat[j] = reversePalette.get(row[j]);
                    }
                    listTag.add(new ByteArrayTag(asByteDat));
                }
            }

            compound.put("picture", listTag);
            return compound;
        }
        return compound;
    }

    public static void readFromNBT(IPaintable instance, CompoundTag compound) {
        if (!compound.contains("scale"))
            return;
        short pixelCountX = compound.getShort("pixelX");
        byte scaleFactor = compound.getByte("scale");
        if (compound.contains("picture", Tag.TAG_COMPOUND)) {
            // legacy support
            CompoundTag pictureInfo = compound.getCompound("picture");
            int arraySize = pixelCountX / scaleFactor;
            int[][] pictureData = new int[arraySize][];
            for (int i = 0; i < arraySize; i++) {
                pictureData[i] = pictureInfo.getIntArray("" + i);
            }
            instance.setData(scaleFactor, pictureData, null, null);
        } else {
            int[] palette = compound.contains("palette", Tag.TAG_INT_ARRAY) ? compound.getIntArray("palette") : null;
            ListTag pictureInfo = compound.getList("picture", palette == null ? Tag.TAG_INT_ARRAY : Tag.TAG_BYTE_ARRAY);
            int arraySize = pixelCountX / scaleFactor;
            int[][] pictureData = new int[arraySize][];
            for (int i = 0; i < arraySize; i++) {
                if (palette == null) {
                    pictureData[i] = pictureInfo.getIntArray(i);
                } else {
                    byte[] data = ((ByteArrayTag) pictureInfo.get(i)).getAsByteArray();
                    int[] row = new int[data.length];
                    for (int j = 0; j < data.length; j++) {
                        row[j] = palette[data[j]];
                    }
                    pictureData[i] = row;
                }
            }
            instance.setDataWithPalette(scaleFactor, pictureData, palette, null, null);
        }
    }
}
