package ichttt.mods.mcpaint;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class MCPaintConfig {
    static final ForgeConfigSpec clientSpec;
    public static final MCPaintConfig.Client CLIENT;
    static {
        final Pair<MCPaintConfig.Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(MCPaintConfig.Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    @SuppressWarnings("CanBeFinal")
    public static class Client {
        Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Client-only settings").push("Client");

            directApplyStamp = builder
                    .comment("True if stamps should set the picture directly instead of opening the GUI")
                    .translation("mcpaint.config.directapplystamp")
                    .define("directApplyStamp", false);

            optimizePictures = builder
                    .comment("True to allow MCPaint to optimize picture draw calls in the background to improve performance in the long run")
                    .translation("mcpaint.config.optimizepictures")
                    .define("optimizePictures", true);

            maxPaintRenderDistance = builder
                    .comment("Defines how far away the paint on the block should be rendered at max")
                    .translation("mcpaint.config.maxpaintrenderdistance")
                    .defineInRange("maxPaintRenderDistance", 128, 64, 256);

            enableMipMaps = builder
                    .comment("If enabled, mipmaps will be used for far away blocks. Can improve speed and image stability, but also could make images more blurry on farther distance or cause micro lags. Somewhat experimental")
                    .translation("mcpaint.config.enablemipmaps")
                    .define("enableMipMaps", false);

            maxPaintBrightness = builder
                    .comment("Defines the maximum brightness that a picture can have. Helps to reduce oversaturation")
                    .translation("mcpaint.config.maxpaintbrightness")
                    .defineInRange("maxPaintBrightness", 220, 180, 240);

            maxMipSize = builder
                    .comment("The factor how many rects the mip is allowed to have so it is allowed to be used. Saves some memory when performance is not better than no-mip version and provides clearer images, but makes image less stable")
                    .translation("mcpaint.config.maxmipsize")
                    .worldRestart()
                    .defineInRange("maxMipSize", 0.8D, 0D, 1D);

            maxTotalColorDiffPerMip = builder
                    .comment("How much all color channels can differ so they are merged as one channel in a mip. Value multiplied by mip level. Higher values improve performance, but reduce color clarity")
                    .translation("mcpaint.config.totalcolordiffpermap")
                    .worldRestart()
                    .defineInRange("maxTotalColorDiffPerMip", 6, 0, 50);

            maxSingleColorDiffPerMip = builder
                    .comment("How much all color channels can differ so they are merged as one channel in a mip. Value multiplied by mip level. Higher values improve performance, but reduce color clarity")
                    .translation("mcpaint.config.maxsinglecolordiffpermip")
                    .worldRestart()
                    .defineInRange("maxSingleColorDiffPerMip", 4, 0, 20);

            builder.pop();
        }

        public final ForgeConfigSpec.BooleanValue directApplyStamp;
        public final ForgeConfigSpec.BooleanValue optimizePictures;
        public final ForgeConfigSpec.IntValue maxPaintRenderDistance;
        public final ForgeConfigSpec.BooleanValue enableMipMaps;
        public final ForgeConfigSpec.IntValue maxPaintBrightness;
        public final ForgeConfigSpec.DoubleValue maxMipSize;
        public final ForgeConfigSpec.IntValue maxTotalColorDiffPerMip;
        public final ForgeConfigSpec.IntValue maxSingleColorDiffPerMip;
    }
}
