package ichttt.mods.mcpaint;

import net.minecraftforge.common.config.Config;

@Config(modid = MCPaint.MODID)
public class MCPaintConfig {
    @Config.Comment("Client-Only options")
    public static final Client CLIENT = new Client();

    public static class Client {

        @Config.Comment("True if stamps should set the picture directly instead of opening the GUI")
        public boolean directApplyStamp = false;

        @Config.Comment("True to allow MCPaint to optimize picture draw calls in the background to improve performance in the long run")
        public boolean optimizePictures = true;

        @Config.Comment("Defines how far away the underlying block should be rendered at max")
        @Config.RangeInt(min = 64, max = 512)
        public int maxRenderDistance = 128;

        @Config.Comment("Defines how far away the paint on the block should be rendered at max")
        @Config.RangeInt(min = 64, max = 256)
        public int maxPaintRenderDistance = 96;
    }

    @Config.Comment("Enables additional OneProbe compat if the mod is loaded. If you notice errors or log spam, disable this")
    public static boolean enableOneProbeCompat = true;
}
