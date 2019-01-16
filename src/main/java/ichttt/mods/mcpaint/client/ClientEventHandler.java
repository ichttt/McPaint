package ichttt.mods.mcpaint.client;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.render.TEISRStamp;
import ichttt.mods.mcpaint.client.render.TESRCanvas;
import ichttt.mods.mcpaint.client.render.batch.RenderCache;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class ClientEventHandler {

    public static void setupClient(FMLClientSetupEvent event) {
        DeferredWorkQueue.enqueueWork(() -> {MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);return null;});
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCanvas.class, new TESRCanvas());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
//        ModelLoader.setCustomModelResourceLocation(EventHandler.BRUSH, 0, new ModelResourceLocation(new ResourceLocation(MCPaint.MODID, "brush"), "inventory"));
//        ModelLoader.setCustomModelResourceLocation(EventHandler.STAMP, 0, new ModelResourceLocation(new ResourceLocation(MCPaint.MODID, "stamp"), "inventory"));
//        EventHandler.STAMP.setTileEntityItemStackRenderer(TEISRStamp.INSTANCE);
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
}
