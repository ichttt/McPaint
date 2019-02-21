package ichttt.mods.mcpaint;

import ichttt.mods.mcpaint.client.ClientEventHandler;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.item.ItemBrush;
import ichttt.mods.mcpaint.common.item.ItemStamp;
import ichttt.mods.mcpaint.networking.MessageClearSide;
import ichttt.mods.mcpaint.networking.MessageDrawAbort;
import ichttt.mods.mcpaint.networking.MessagePaintData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MCPaint.MODID)
public class MCPaint {
    public static final String MODID = "mcpaint";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    private static final String NETWORKING_MAJOR = "2.";
    private static final String NETWORKING_MINOR = "0";

    private static final String NETWORKING_VERSION = NETWORKING_MAJOR + NETWORKING_MINOR;
    public static SimpleChannel NETWORKING;

    public MCPaint() {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        FMLJavaModLoadingContext.get().getModEventBus().register(MCPaint.class);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientEventHandler::setupClient));
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        NETWORKING = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MODID, "channel"),
                () -> NETWORKING_VERSION,
                s -> s.startsWith(NETWORKING_MAJOR),
                s -> s.equals(NETWORKING_VERSION));
        NETWORKING.registerMessage(1, MessagePaintData.class, MessagePaintData::encode, MessagePaintData::new, MessagePaintData.ServerHandler.INSTANCE::onMessage);
        NETWORKING.registerMessage(2, MessageDrawAbort.class, MessageDrawAbort::encode, MessageDrawAbort::new, MessageDrawAbort.Handler::onMessage);
        NETWORKING.registerMessage(3, MessagePaintData.ClientMessage.class, MessagePaintData.ClientMessage::encode, MessagePaintData.ClientMessage::new, MessagePaintData.ClientHandler.INSTANCE::onMessage);
        NETWORKING.registerMessage(4, MessageClearSide.class, MessageClearSide::encode, MessageClearSide::new, MessageClearSide.ServerHandler::onMessage);
        NETWORKING.registerMessage(5, MessageClearSide.ClientMessage.class, MessageClearSide.ClientMessage::encode, MessageClearSide.ClientMessage::new, MessageClearSide.ClientHandler::onMessage);
        CapabilityPaintable.register();
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(new ItemBrush(new ResourceLocation(MCPaint.MODID, "brush")));
        registry.register(new ItemStamp(new ResourceLocation(MCPaint.MODID, "stamp")));
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockCanvas(Material.WOOD, new ResourceLocation(MCPaint.MODID, "canvas_wood")));
        event.getRegistry().register(new BlockCanvas(Material.ROCK, new ResourceLocation(MCPaint.MODID, "canvas_rock")));
        event.getRegistry().register(new BlockCanvas(Material.GROUND, new ResourceLocation(MCPaint.MODID, "canvas_ground")));
    }

    @SubscribeEvent
    @SuppressWarnings("ConstantConditions")
    public static void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
        TileEntityType<?> type = (TileEntityType.Builder.create(TileEntityCanvas::new).build(null).setRegistryName(MCPaint.MODID, "canvas_te"));
        event.getRegistry().register(type);
    }
}
