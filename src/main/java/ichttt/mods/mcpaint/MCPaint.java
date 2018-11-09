package ichttt.mods.mcpaint;

import ichttt.mods.mcpaint.client.ClientHooks;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.javafmlmod.FMLModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

@Mod(MCPaint.MODID)
public class MCPaint {
    public static final String MODID = "mcpaint";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final SimpleChannel NETWORKING = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, "channel"), () -> "1", Objects::nonNull, Objects::nonNull);

    public MCPaint() {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        FMLModLoadingContext.get().getModEventBus().register(MCPaint.class);
    }

    @SubscribeEvent
    public static void preInit(FMLPreInitializationEvent event) {
        System.out.println("INIT");
        DeferredWorkQueue.enqueueWork(() -> {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> ClientHooks::preInit);
            EventHandler.registerBlocks(new RegistryEvent.Register<>(null, ForgeRegistries.BLOCKS));
            EventHandler.registerItems(new RegistryEvent.Register<>(null, ForgeRegistries.ITEMS));
            EventHandler.registerTileEntity(new RegistryEvent.Register<>(null, ForgeRegistries.TILE_ENTITIES));
            return null;
        });
    }

    @SubscribeEvent
    public static void postInit(FMLPostInitializationEvent event) {
        System.out.println("POST");
        DeferredWorkQueue.enqueueWork(() -> {
            EventHandler.update();
            return null;
        });
    }

    @SubscribeEvent
    public static void init(FMLInitializationEvent event) {
        //TODO networking
//        NETWORKING.registerMessage(MessagePaintData.ServerHandler.class, MessagePaintData.class, 1, Side.SERVER);
//        NETWORKING.registerMessage(MessageDrawAbort.Handler.class, MessageDrawAbort.class, 2, Side.SERVER);
//        NETWORKING.registerMessage(MessagePaintData.ClientHandler.class, MessagePaintData.class, 3, Side.CLIENT);
//        NETWORKING.registerMessage(MessageClearSide.Handler.class, MessageClearSide.class, 4, Side.SERVER);
        CapabilityPaintable.register();
//        if (MCPaintConfig.enableOneProbeCompat)
//            FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "ichttt.mods.mcpaint.common.OneProbeCompat");
    }

}
