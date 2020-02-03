package ichttt.mods.mcpaint.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderTypeHandler extends RenderState { //Extend render state to have access to static protected fields
    public static final RenderType CANVAS = RenderType.get("mcpaint_canvas",
            DefaultVertexFormats.POSITION_COLOR_LIGHTMAP,
            GL11.GL_QUADS,
            65536, //quite large, as picture can easily take up this amount
            false,
            false,
            RenderType.State.builder()
                    .alpha(RenderState.DEFAULT_ALPHA)
                    .lightmap(RenderState.LIGHTMAP_ENABLED)
                    .transparency(RenderState.TRANSLUCENT_TRANSPARENCY)
                    .layer(RenderState.NO_LAYERING)
                    .build(false));

    public RenderTypeHandler(String nameIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, setupTaskIn, clearTaskIn);
        throw new UnsupportedOperationException();
    }
}
