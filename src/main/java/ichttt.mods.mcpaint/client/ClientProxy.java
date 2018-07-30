package ichttt.mods.mcpaint.client;

import ichttt.mods.mcpaint.IProxy;
import ichttt.mods.mcpaint.MCPaint;

public class ClientProxy implements IProxy {
    @Override
    public void preInit() {
        MCPaint.LOGGER.debug("Loading ClientProxy");
    }
}
