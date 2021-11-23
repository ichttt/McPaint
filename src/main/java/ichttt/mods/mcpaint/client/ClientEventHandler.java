package ichttt.mods.mcpaint.client;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.delegators.BlockColorDelegator;
import ichttt.mods.mcpaint.client.delegators.DelegatingBakedModel;
import ichttt.mods.mcpaint.client.render.ISTERStamp;
import ichttt.mods.mcpaint.client.render.RenderTypeHandler;
import ichttt.mods.mcpaint.client.render.TERCanvas;
import ichttt.mods.mcpaint.client.render.batch.RenderCache;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class ClientEventHandler {

    public static void earlySetup() {
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ClientEventHandler::onModelBake);
        bus.addListener(ClientEventHandler::registerModels);
        bus.addListener(ClientEventHandler::setupClient);
        bus.addListener(ClientEventHandler::lateSetup);
    }

    public static void setupClient(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(EventHandler.CANVAS_TE, TERCanvas::new);
        //Just to classload this ot init to avoid lag spikes
        //noinspection ResultOfMethodCallIgnored,Convert2MethodRef
        event.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RenderTypeHandler.CANVAS.toString()));
    }

    public static void registerModels(ModelRegistryEvent event) {
        ItemProperties.register(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(MCPaint.MODID, "stamp")), "Did not find stamp"), new ResourceLocation(MCPaint.MODID, "shift"), ISTERStamp.INSTANCE);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isClientSide()) {
            RenderCache.clear();
        }
    }

    @SubscribeEvent
    public static void onConfigChange(ModConfigEvent.Reloading event) {
        ClientHooks.onConfigReload();
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        ClientHooks.onConfigReload();
    }

    public static void onModelBake(ModelBakeEvent event) {
        String[] toReplace = new String[] {"canvas_ground", "canvas_rock", "canvas_wood"};
        String[] variants = new String[] {"normal_cube=false,solid=false", "normal_cube=true,solid=false", "normal_cube=false,solid=true", "normal_cube=true,solid=true"};
        for (String s : toReplace) {
            for (String variant : variants) {
                ModelResourceLocation mrl = new ModelResourceLocation(new ResourceLocation(MCPaint.MODID, s), variant);
                BakedModel model = event.getModelRegistry().get(mrl);
                if (model == null) throw new NullPointerException("Model for " + mrl);
                model = new DelegatingBakedModel(model);
                event.getModelRegistry().put(mrl, model);
            }
        }
    }

    public static void lateSetup(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> Minecraft.getInstance().getBlockColors().register(new BlockColorDelegator(), EventHandler.CANVAS_GROUND, EventHandler.CANVAS_ROCK, EventHandler.CANVAS_WOOD));
    }
}
