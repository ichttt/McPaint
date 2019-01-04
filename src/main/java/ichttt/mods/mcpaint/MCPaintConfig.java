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

        @Config.Comment("Defines how far away the paint on the block should be rendered at max")
        @Config.RangeInt(min = 64, max = 256)
        public int maxPaintRenderDistance = 128;

        @Config.Comment("If enabled, mipmaps will be used for far away blocks. Can improve speed and image stability, but also could make images more blurry on farther distance")
        public boolean enableMipMaps = false;

        @Config.Comment("")
        @Config.RangeInt(min = 180, max = 240)
        public int maxPaintBrightness = 220;

        @Config.Comment("The factor how many rects the mip is allowed to have so it is allowed to be used. Saves some memory when performance is not better than no-mip version and provides clearer images, but makes image less stable")
        @Config.RangeDouble(min = 0, max = 1)
        @Config.RequiresWorldRestart
        public double maxMipSize = 0.8D;

        @Config.Comment("How much all color channels can differ so they are merged as one channel in a mip. Value multiplied by mip level. Higher values improve performance, but reduce color clarity")
        @Config.RangeInt(min = 0, max = 50)
        @Config.RequiresWorldRestart
        public int maxTotalColorDiffPerMip = 6;

        @Config.Comment("How much a single color channel can differ so it is merged as one channel in a mip. Value multiplied by mip level. Higher values improve performance, but reduce color clarity")
        @Config.RangeInt(min = 0, max = 20)
        @Config.RequiresWorldRestart
        public int maxSingleColorDiffPerMip = 4;
    }

    @Config.Comment("Enables additional OneProbe compat if the mod is loaded. If you notice errors or log spam, disable this")
    public static boolean enableOneProbeCompat = true;
}
