package ichttt.mods.mcpaint.server;

import ichttt.mods.mcpaint.IProxy;
import ichttt.mods.mcpaint.MCPaint;

public class ServerProxy implements IProxy {
    @Override
    public void preInit() {
        MCPaint.LOGGER.info("Loading ServerProxy");
    }
}
