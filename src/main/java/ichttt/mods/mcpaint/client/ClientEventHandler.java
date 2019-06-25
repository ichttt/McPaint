package ichttt.mods.mcpaint.client;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.delegators.BlockColorDelegator;
import ichttt.mods.mcpaint.client.delegators.DelegatingBakedModel;
import ichttt.mods.mcpaint.client.render.TEISRStamp;
import ichttt.mods.mcpaint.client.render.TESRCanvas;
import ichttt.mods.mcpaint.client.render.batch.RenderCache;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.stream.Collectors;

public class ClientEventHandler {

    public static void setupClient(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientEventHandler::onModelBake);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCanvas.class, new TESRCanvas());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(MCPaint.MODID, "stamp")), "Did not find stamp").addPropertyOverride(new ResourceLocation(MCPaint.MODID, "shift"), TEISRStamp.INSTANCE);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote()) {
            RenderCache.clear();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().world != null && Minecraft.getInstance().world.getGameTime() % 200 == 0) {
            RenderCache.scheduleCleanup();
        }
    }

    @SubscribeEvent
    public static void onConfigChange(ConfigChangedEvent event) {
        ClientHooks.onConfigReload();
    }

    public static void onModelBake(ModelBakeEvent event) {
        String[] toReplace = new String[] {"canvas_ground", "canvas_rock", "canvas_wood"};
        String[] variants = new String[] {"normal_cube=false,solid=false", "normal_cube=true,solid=false", "normal_cube=false,solid=true", "normal_cube=true,solid=true"};
        for (String s : toReplace) {
            for (String variant : variants) {
                ModelResourceLocation mrl = new ModelResourceLocation(new ResourceLocation(MCPaint.MODID, s), variant);
                IBakedModel model = event.getModelRegistry().get(mrl);
                if (model == null) throw new NullPointerException("Model for " + mrl);
                model = new DelegatingBakedModel(model);
                event.getModelRegistry().put(mrl, model);
            }
        }
    }

    public static void lateSetup(FMLLoadCompleteEvent event) {
        DeferredWorkQueue.runLater(() -> Minecraft.getInstance().getBlockColors().register(new BlockColorDelegator(), EventHandler.CANVAS_GROUND, EventHandler.CANVAS_ROCK, EventHandler.CANVAS_WOOD));
    }
}
