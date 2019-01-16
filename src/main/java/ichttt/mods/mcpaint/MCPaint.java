package ichttt.mods.mcpaint;

import ichttt.mods.mcpaint.client.ClientEventHandler;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.networking.MessageClearSide;
import ichttt.mods.mcpaint.networking.MessageDrawAbort;
import ichttt.mods.mcpaint.networking.MessagePaintData;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MCPaint.MODID)
public class MCPaint {
    public static final String MODID = "mcpaint";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    private static final String NETWORKING_MAJOR = "1.";
    private static final String NETWORKING_MINOR = "0";

    private static final String NETWORKING_VERSION = NETWORKING_MAJOR + NETWORKING_MINOR;
    public static final SimpleChannel NETWORKING = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "channel"),
            () -> NETWORKING_VERSION,
            s -> s.startsWith(NETWORKING_MAJOR),
            s -> s.equals(NETWORKING_VERSION));

    public MCPaint() {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        FMLModLoadingContext.get().getModEventBus().addListener(MCPaint::init);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> FMLModLoadingContext.get().getModEventBus().addListener(ClientEventHandler::setupClient));
    }

    public static void init(FMLCommonSetupEvent event) {
        NETWORKING.registerMessage(1, MessagePaintData.class, MessagePaintData::encode, MessagePaintData::new, MessagePaintData.ServerHandler.INSTANCE::onMessage);
        NETWORKING.registerMessage(2, MessageDrawAbort.class, MessageDrawAbort::encode, MessageDrawAbort::new, MessageDrawAbort.Handler::onMessage);
        NETWORKING.registerMessage(3, MessagePaintData.ClientMessage.class, MessagePaintData.ClientMessage::encode, MessagePaintData.ClientMessage::new, (clientMessage, supplier) -> {
            DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
                throw new RuntimeException("MessagePaintData.ClientMessage cannot be run on server!");
            });
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> MessagePaintData.ClientHandler.INSTANCE.onMessage(clientMessage, supplier));
        });
        NETWORKING.registerMessage(4, MessageClearSide.class, MessageClearSide::encode, MessageClearSide::new, MessageClearSide.Handler::onMessage);
        CapabilityPaintable.register();
    }
}
