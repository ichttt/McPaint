package ichttt.mods.mcpaint.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;

public class PictureRenderer {
    public static void renderInGui(double leftOffset, double topOffset, byte scaleFactor, BufferBuilder builder, int[][] picture) {
        for (int x = 0; x < picture.length; x++) {
            int[] yPos = picture[x];
            for (int y = 0; y < yPos.length; y++) {
                int color = picture[x][y];
                double left = leftOffset + (x * scaleFactor);
                double top = topOffset + (y * scaleFactor);
                double right = left + scaleFactor;
                double bottom = top + scaleFactor;
                drawToBuffer(color, builder, left, top, right, bottom);
            }
        }
    }

    public static void renderInGame(byte scaleFactor, BufferBuilder builder, int[][] picture) {
        for (int x = 0; x < picture.length; x++) {
            int[] yPos = picture[x];
            for (int y = 0; y < yPos.length; y++) {
                int color = picture[x][y];
                double left = ((x * scaleFactor) / 128F) + scaleFactor / 128F;
                double top = 1 - ((y * scaleFactor) / 128F) - scaleFactor / 128F;
                double right = left - (scaleFactor / 128F);
                double bottom = top + (scaleFactor / 128F);
                drawToBuffer(color, builder, left, top, right, bottom);
            }
        }
    }

    public static void setWorldGLState() {
        GlStateManager.pushMatrix();
        GlStateManager.enableDepthTest(); //Should be true
        GlStateManager.disableTexture();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void resetWorldGLState() {
        GlStateManager.disableBlend();
        GlStateManager.enableTexture();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    public static boolean drawToBuffer(int color, BufferBuilder builder, double left, double top, double right, double bottom) {
        //See drawRect(int left, int top, int right, int bottom, int color
        float a = (float) (color >> 24 & 255) / 255.0F;
        if (a <= 0.01F) return true;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        builder.pos(left, bottom, 0).color(r, g, b, a).endVertex();
        builder.pos(right, bottom, 0).color(r, g, b, a).endVertex();
        builder.pos(right, top, 0).color(r, g, b, a).endVertex();
        builder.pos(left, top, 0).color(r, g, b, a).endVertex();
        return false;
    }
}
