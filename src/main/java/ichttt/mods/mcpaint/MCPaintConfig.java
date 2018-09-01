package ichttt.mods.mcpaint;

import net.minecraftforge.common.config.Config;

@Config(modid = MCPaint.MODID)
public class MCPaintConfig {

    @Config.Comment("True to allow MCPaint to optimize picture draw calls in the background to improve performance in the long run")
    public static boolean optimizePictures = true;
}
