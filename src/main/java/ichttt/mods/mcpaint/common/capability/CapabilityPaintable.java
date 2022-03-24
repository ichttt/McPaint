package ichttt.mods.mcpaint.common.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

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

            CompoundTag pictureInfo = new CompoundTag();
            for (int i = 0; i < (pixelCountX / scaleFactor); i++) {
                pictureInfo.putIntArray("" + i, pictureData[i]);
            }

            compound.put("picture", pictureInfo);
            return compound;
        }
        return compound;
    }

    public static void readFromNBT(IPaintable instance, CompoundTag compound) {
        if (!compound.contains("scale"))
            return;
        short pixelCountX = compound.getShort("pixelX");
        byte scaleFactor = compound.getByte("scale");
        CompoundTag pictureInfo = compound.getCompound("picture");
        int arraySize = pixelCountX / scaleFactor;
        int[][] pictureData = new int[arraySize][];
        for (int i = 0; i < (arraySize); i++) {
            pictureData[i] = pictureInfo.getIntArray("" + i);
        }
        instance.setData(scaleFactor, pictureData, null, null);
    }
}
