package ichttt.mods.mcpaint.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderTypeHandler extends RenderState { //Extend render state to have access to static protected fields
    public static final RenderType CANVAS = RenderType.create("mcpaint_canvas",
            DefaultVertexFormats.POSITION_COLOR_LIGHTMAP,
            GL11.GL_QUADS,
            65536, //quite large, as picture can easily take up this amount
            false,
            false,
            RenderType.State.builder()
                    .setAlphaState(RenderState.DEFAULT_ALPHA)
                    .setLightmapState(RenderState.LIGHTMAP)
                    .setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
                    .setLayeringState(RenderState.NO_LAYERING)
                    .createCompositeState(false));

    public RenderTypeHandler(String nameIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, setupTaskIn, clearTaskIn);
        throw new UnsupportedOperationException();
    }
}
