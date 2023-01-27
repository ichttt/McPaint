package ichttt.mods.mcpaint.client;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.delegators.BlockColorDelegator;
import ichttt.mods.mcpaint.client.delegators.DelegatingBakedModel;
import ichttt.mods.mcpaint.client.render.ISTERStamp;
import ichttt.mods.mcpaint.client.render.RenderTypeHandler;
import ichttt.mods.mcpaint.client.render.TERCanvas;
import ichttt.mods.mcpaint.client.render.batch.RenderCache;
import ichttt.mods.mcpaint.common.RegistryObjects;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;
import java.util.function.Supplier;

public class ClientEventHandler {

    public static void earlySetup() {
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ClientEventHandler::onModelBake);
        bus.addListener(ClientEventHandler::registerModels);
        bus.addListener(ClientEventHandler::setupClient);
        bus.addListener(ClientEventHandler::onRegisterRenders);
        bus.addListener(ClientEventHandler::onRegisterColorHandlers);
    }

    public static void setupClient(FMLClientSetupEvent event) {
        //Just to classload this ot init to avoid lag spikes
        //noinspection ResultOfMethodCallIgnored,Convert2MethodRef
        event.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RenderTypeHandler.CANVAS.toString()));
    }

    public static void registerModels(RegisterGeometryLoaders event) {
        ItemProperties.register(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(MCPaint.MODID, "stamp")), "Did not find stamp"), new ResourceLocation(MCPaint.MODID, "shift"), ISTERStamp.INSTANCE);
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
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

    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        ResourceLocation[] toReplace = RegistryObjects.CANVAS_BLOCKS.values().stream().map(RegistryObject::getId).toArray(ResourceLocation[]::new);
        String[] variants = new String[] {"normal_cube=false,solid=false", "normal_cube=true,solid=false", "normal_cube=false,solid=true", "normal_cube=true,solid=true"};
        for (ResourceLocation rl : toReplace) {
            for (String variant : variants) {
                ModelResourceLocation mrl = new ModelResourceLocation(rl, variant);
                BakedModel model = event.getModels().get(mrl);
                if (model == null) throw new NullPointerException("Model for " + mrl);
                model = new DelegatingBakedModel(model);
                event.getModels().put(mrl, model);
            }
        }
    }

    public static void onRegisterRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(RegistryObjects.CANVAS_BE.get(), TERCanvas::new);
    }

    public static void onRegisterColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.register(new BlockColorDelegator(), RegistryObjects.CANVAS_BLOCKS.values().stream().map(Supplier::get).toArray(Block[]::new));
    }
}
