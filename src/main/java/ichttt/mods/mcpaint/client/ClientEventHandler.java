package ichttt.mods.mcpaint.client;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.EventHandler;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientEventHandler {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(EventHandler.BRUSH, 0, new ModelResourceLocation(new ResourceLocation(MCPaint.MODID, "brush"), "inventory"));
    }
}
