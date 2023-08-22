package ichttt.mods.mcpaint;

import ichttt.mods.mcpaint.client.ClientEventHandler;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.RegistryObjects;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.networking.MessageClearSide;
import ichttt.mods.mcpaint.networking.MessageDrawAbort;
import ichttt.mods.mcpaint.networking.MessagePaintData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MCPaint.MODID)
public class MCPaint {
    public static final String MODID = "mcpaint";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    private static final String NETWORKING_VERSION = "6";
    public static SimpleChannel NETWORKING;

    public MCPaint() {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(MCPaint.class);

        //noinspection Convert2MethodRef classloading...
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientEventHandler.earlySetup());
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, MCPaintConfig.clientSpec);

        RegistryObjects.registerToBus(modEventBus);
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        NETWORKING = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(MODID, "channel"))
                .networkProtocolVersion(() -> NETWORKING_VERSION)
                .clientAcceptedVersions(s -> s.equals(NETWORKING_VERSION))
                .serverAcceptedVersions(s -> s.equals(NETWORKING_VERSION))
                .simpleChannel();
        NETWORKING.registerMessage(1, MessagePaintData.class, MessagePaintData::encode, MessagePaintData::new, MessagePaintData.ServerHandler.INSTANCE::onMessage);
        NETWORKING.registerMessage(2, MessageDrawAbort.class, MessageDrawAbort::encode, MessageDrawAbort::new, MessageDrawAbort.Handler::onMessage);
        NETWORKING.registerMessage(3, MessagePaintData.ClientMessage.class, MessagePaintData.ClientMessage::encode, MessagePaintData.ClientMessage::new, MessagePaintData.ClientHandler.INSTANCE::onMessage);
        NETWORKING.registerMessage(4, MessageClearSide.class, MessageClearSide::encode, MessageClearSide::new, MessageClearSide.ServerHandler::onMessage);
        NETWORKING.registerMessage(5, MessageClearSide.ClientMessage.class, MessageClearSide.ClientMessage::encode, MessageClearSide.ClientMessage::new, MessageClearSide.ClientHandler::onMessage);
        LOGGER.debug("Registered networking");
        CapabilityPaintable.register();
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        // Add to ingredients tab
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(RegistryObjects.BRUSH);
            event.accept(RegistryObjects.STAMP);
        }
    }
}
