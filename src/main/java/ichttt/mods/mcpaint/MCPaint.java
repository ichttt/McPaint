package ichttt.mods.mcpaint;

import ichttt.mods.mcpaint.client.ClientEventHandler;
import ichttt.mods.mcpaint.client.render.TESRCanvas;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.networking.MessageDrawAbort;
import ichttt.mods.mcpaint.networking.MessagePaintData;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.item.Item;
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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.filter.MarkerFilter;

import java.util.Objects;

@Mod(MCPaint.MODID)
public class MCPaint {
    public static final String MODID = "mcpaint";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final SimpleChannel NETWORKING = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, "channel"), () -> "1", Objects::nonNull, Objects::nonNull);

    static {
        Configurator.setRootLevel(Level.DEBUG);
        final MarkerFilter classloadingFilter = MarkerFilter.createFilter("CLASSLOADING", Filter.Result.DENY, Filter.Result.NEUTRAL);
        final MarkerFilter launchpluginFilter = MarkerFilter.createFilter("LAUNCHPLUGIN", Filter.Result.DENY, Filter.Result.NEUTRAL);
        final MarkerFilter axformFilter= MarkerFilter.createFilter("AXFORM", Filter.Result.DENY, Filter.Result.NEUTRAL);
        final MarkerFilter eventbusFilter = MarkerFilter.createFilter("EVENTBUS", Filter.Result.DENY, Filter.Result.NEUTRAL);
        final MarkerFilter distxformFilter = MarkerFilter.createFilter("DISTXFORM", Filter.Result.DENY, Filter.Result.NEUTRAL);
        final LoggerContext logcontext = LoggerContext.getContext(false);
        logcontext.getConfiguration().addFilter(classloadingFilter);
        logcontext.getConfiguration().addFilter(launchpluginFilter);
        logcontext.getConfiguration().addFilter(axformFilter);
        logcontext.getConfiguration().addFilter(eventbusFilter);
        logcontext.getConfiguration().addFilter(distxformFilter);
        logcontext.updateLoggers();
    }

    public MCPaint() {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        FMLModLoadingContext.get().getModEventBus().register(MCPaint.class);
    }

    @SubscribeEvent
    public static void preInit(FMLPreInitializationEvent event) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(ClientEventHandler.class));
        LOGGER.info("MCPaint preinit");
        LOGGER.info("Registry clazz: " + Block.REGISTRY.toString() + " Forge registry clazz: " + ForgeRegistries.BLOCKS.toString());
        DeferredWorkQueue.enqueueWork(() -> {
            try {
                //TODO remove once registry events are fired
                EventHandler.registerBlocks(new RegistryEvent.Register<>(null, ForgeRegistries.BLOCKS));
                EventHandler.registerItems(new RegistryEvent.Register<>(null, ForgeRegistries.ITEMS));
                EventHandler.registerTileEntity(new RegistryEvent.Register<>(null, ForgeRegistries.TILE_ENTITIES));
                LOGGER.info("Brush: " + ForgeRegistries.ITEMS.getValue(new ResourceLocation(MODID, "brush")) + " or " + Item.REGISTRY.get(new ResourceLocation(MODID, "brush")));
                DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCanvas.class, new TESRCanvas()));
            } catch (Throwable e) {
                e.printStackTrace();
                Minecraft.getInstance().displayCrashReport(new CrashReport("Parallel Work", e));
            }
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
