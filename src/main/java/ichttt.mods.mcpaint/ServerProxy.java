package ichttt.mods.mcpaint;

public class ServerProxy implements IProxy {
    @Override
    public void preInit() {
        MCPaint.LOGGER.info("Loading ServerProxy");
    }
}
