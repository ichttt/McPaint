package ichttt.mods.mcpaint.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;

public class RenderUtil {
    public static void renderInGui(float leftOffset, float topOffset, byte scaleFactor, BufferBuilder builder, int[][] picture) {
        for (int x = 0; x < picture.length; x++) {
            int[] yPos = picture[x];
            for (int y = 0; y < yPos.length; y++) {
                int color = picture[x][y];
                float left = leftOffset + (x * scaleFactor);
                float top = topOffset + (y * scaleFactor);
                float right = left + scaleFactor;
                float bottom = top + scaleFactor;
                drawToBuffer(Matrix4f.makeTranslate(0,0,0), color, builder, left, top, right, bottom);
            }
        }
    }

    public static void renderInGame(Matrix4f matrix4f, byte scaleFactor, IVertexBuilder builder, int[][] picture) {
        for (int x = 0; x < picture.length; x++) {
            int[] yPos = picture[x];
            for (int y = 0; y < yPos.length; y++) {
                int color = picture[x][y];
                float left = ((x * scaleFactor) / 128F) + scaleFactor / 128F;
                float top = 1 - ((y * scaleFactor) / 128F) - scaleFactor / 128F;
                float right = left - (scaleFactor / 128F);
                float bottom = top + (scaleFactor / 128F);
                drawToBuffer(matrix4f, color, builder, left, top, right, bottom);
            }
        }
    }

    public static boolean drawToBuffer(Matrix4f matrix4f, int color, IVertexBuilder builder, float left, float top, float right, float bottom) {
        //See drawRect(int left, int top, int right, int bottom, int color
        float a = (float) (color >> 24 & 255) / 255.0F;
        if (a <= 0.01F) return true;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        builder.pos(matrix4f, left, bottom, 0).color(r, g, b, a).endVertex();
        builder.pos(matrix4f, right, bottom, 0).color(r, g, b, a).endVertex();
        builder.pos(matrix4f, right, top, 0).color(r, g, b, a).endVertex();
        builder.pos(matrix4f, left, top, 0).color(r, g, b, a).endVertex();
        return false;
    }
}
