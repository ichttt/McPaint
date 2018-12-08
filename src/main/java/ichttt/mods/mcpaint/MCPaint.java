package ichttt.mods.mcpaint;

import ichttt.mods.mcpaint.client.ClientEventHandler;
import ichttt.mods.mcpaint.client.render.TESRCanvas;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.networking.MessageDrawAbort;
import ichttt.mods.mcpaint.networking.MessagePaintData;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
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
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(ClientEventHandler.class));
        DeferredWorkQueue.enqueueWork(() -> {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCanvas.class, new TESRCanvas()));
            //TODO remove once registry events are fired
            EventHandler.registerBlocks(new RegistryEvent.Register<>(null, ForgeRegistries.BLOCKS));
            EventHandler.registerItems(new RegistryEvent.Register<>(null, ForgeRegistries.ITEMS));
            EventHandler.registerTileEntity(new RegistryEvent.Register<>(null, ForgeRegistries.TILE_ENTITIES));
            return null;
        });
    }

    @SubscribeEvent
    public static void init(FMLInitializationEvent event) {
        NETWORKING.registerMessage(1, MessagePaintData.class, MessagePaintData::encode, MessagePaintData::new, MessagePaintData.ServerHandler.INSTANCE::onMessage);
        NETWORKING.registerMessage(2, MessageDrawAbort.class, MessageDrawAbort::encode, MessageDrawAbort::new, MessageDrawAbort.Handler::onMessage);
        NETWORKING.registerMessage(3, MessagePaintData.ClientMessage.class, MessagePaintData.ClientMessage::encode, MessagePaintData.ClientMessage::new, (clientMessage, supplier) -> {
            DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
                throw new RuntimeException("MessagePaintData.ClientMessage cannot be run on server!");
            });
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> MessagePaintData.ClientHandler.INSTANCE.onMessage(clientMessage, supplier));
        });
        NETWORKING.registerMessage(4, MessageDrawAbort.class, MessageDrawAbort::encode, MessageDrawAbort::new, MessageDrawAbort.Handler::onMessage);
        CapabilityPaintable.register();
        DeferredWorkQueue.enqueueWork(() -> {
            EventHandler.update();
            return null;
        });
    }

}
