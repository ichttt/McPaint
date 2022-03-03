package ichttt.mods.mcpaint.common.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

public class CapabilityPaintable {


<<<<<<< Updated upstream
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
=======
    public static void register(){
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(CapabilityPaintable::onRegisterCapabilities);
    }

    //@CapabilityInject(IPaintable.class)
    public static Capability<IPaintable> PAINTABLE = getCapability(new CapabilityToken<>() {
    });

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IPaintable.class); //TODO move to event
>>>>>>> Stashed changes
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
    @NotNull
    protected static <T> Capability<T> getCapability(CapabilityToken<T> type) {
        return CapabilityManager.get(type);
    }
}
