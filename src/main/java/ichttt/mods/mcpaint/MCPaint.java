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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MCPaint.MODID)
public class MCPaint {
    public static final String MODID = "mcpaint";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    private static final String NETWORKING_VERSION = "5";
    public static SimpleChannel NETWORKING;

    public MCPaint() {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        FMLJavaModLoadingContext.get().getModEventBus().register(MCPaint.class);
        //noinspection Convert2MethodRef classloading...
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientEventHandler.earlySetup());
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, MCPaintConfig.clientSpec);
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
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(new ItemBrush(new ResourceLocation(MCPaint.MODID, "brush")));
        registry.register(new ItemStamp(new ResourceLocation(MCPaint.MODID, "stamp")));
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockCanvas(Material.WOOD, new ResourceLocation(MCPaint.MODID, "canvas_wood")));
        event.getRegistry().register(new BlockCanvas(Material.STONE, new ResourceLocation(MCPaint.MODID, "canvas_rock")));
        event.getRegistry().register(new BlockCanvas(Material.DIRT, new ResourceLocation(MCPaint.MODID, "canvas_ground")));
    }

    @SubscribeEvent
    @SuppressWarnings("ConstantConditions")
    public static void registerTileEntity(RegistryEvent.Register<BlockEntityType<?>> event) {
        BlockEntityType<?> type = (BlockEntityType.Builder.of(TileEntityCanvas::new, EventHandler.CANVAS_GROUND, EventHandler.CANVAS_ROCK, EventHandler.CANVAS_WOOD).build(null).setRegistryName(MCPaint.MODID, "canvas_te"));
        event.getRegistry().register(type);
    }
}
