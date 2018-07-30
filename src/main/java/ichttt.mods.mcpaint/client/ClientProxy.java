package ichttt.mods.mcpaint.client;

import ichttt.mods.mcpaint.IProxy;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.render.TESRCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy implements IProxy {
    @Override
    public void preInit() {
        MCPaint.LOGGER.debug("Loading ClientProxy");
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCanvas.class, new TESRCanvas());
    }
}
