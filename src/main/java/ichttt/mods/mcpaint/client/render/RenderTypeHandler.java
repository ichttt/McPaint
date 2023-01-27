package ichttt.mods.mcpaint.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class RenderTypeHandler extends RenderStateShard { //Extend render state to have access to static protected fields
    public static final RenderType CANVAS = RenderType.create("mcpaint_canvas",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            65536, //quite large, as picture can easily take up this amount
            false,
            false,
            RenderType.CompositeState.builder()
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setShaderState(ShaderStateShard.POSITION_COLOR_LIGHTMAP_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .createCompositeState(false));

    public RenderTypeHandler(String nameIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, setupTaskIn, clearTaskIn);
        throw new UnsupportedOperationException();
    }

}