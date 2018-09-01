package ichttt.mods.mcpaint.client;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.render.batch.RenderCache;
import ichttt.mods.mcpaint.common.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ClientEventHandler {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(EventHandler.BRUSH, 0, new ModelResourceLocation(new ResourceLocation(MCPaint.MODID, "brush"), "inventory"));
        ModelLoader.setCustomModelResourceLocation(EventHandler.STAMP, 0, new ModelResourceLocation(new ResourceLocation(MCPaint.MODID, "stamp"), "inventory"));
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            RenderCache.clear();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && Minecraft.getMinecraft().world != null && Minecraft.getMinecraft().world.getTotalWorldTime() % 200 == 0) {
            RenderCache.scheduleCleanup();
        }
    }
}
