package ichttt.mods.mcpaint.client.render;

import net.minecraft.client.renderer.BufferBuilder;

import java.awt.*;

public class PictureRenderer {
    private static final int WHITE = Color.WHITE.getRGB();

    public static void renderInGui(double leftOffset, double topOffset, byte scaleFactor, BufferBuilder builder, int[][] picture) {
        for (int x = 0; x < picture.length; x++) {
            int[] yPos = picture[x];
            for (int y = 0; y < yPos.length; y++) {
                int color = picture[x][y];
                //background is already white, we can skip it
                if (color == WHITE) continue;
                double left = leftOffset + (x * scaleFactor);
                double top = topOffset + (y * scaleFactor);
                double right = left + scaleFactor;
                double bottom = top + scaleFactor;
                //See drawRect(int left, int top, int right, int bottom, int color
                float f3 = (float) (color >> 24 & 255) / 255.0F;
                float f = (float) (color >> 16 & 255) / 255.0F;
                float f1 = (float) (color >> 8 & 255) / 255.0F;
                float f2 = (float) (color & 255) / 255.0F;
                builder.pos(left, bottom, 0D).color(f, f1, f2, f3).endVertex();
                builder.pos(right, bottom, 0D).color(f, f1, f2, f3).endVertex();
                builder.pos(right, top, 0D).color(f, f1, f2, f3).endVertex();
                builder.pos(left, top, 0D).color(f, f1, f2, f3).endVertex();
            }
        }
    }

    public static void renderInGame(double ingameX, double ingameY, double ingameZ, byte scaleFactor, BufferBuilder builder, int[][] picture) {
        for (int x = 0; x < picture.length; x++) {
            int[] yPos = picture[x];
            for (int y = 0; y < yPos.length; y++) {
                int color = picture[x][y];
                double left = 1 + ingameX - (((3 + x) * scaleFactor) / 128F);
                double top = 1 + ingameY - (((3 + y) * scaleFactor) / 128F);
                double right = left + (scaleFactor / 128F);
                double bottom = top + (scaleFactor / 128F);
                //See drawRect(int left, int top, int right, int bottom, int color
                float a = (float) (color >> 24 & 255) / 255.0F;
                float r = (float) (color >> 16 & 255) / 255.0F;
                float g = (float) (color >> 8 & 255) / 255.0F;
                float b = (float) (color & 255) / 255.0F;
                builder.pos(left, top, ingameZ).color(r, g, b, a).endVertex();
                builder.pos(right, top, ingameZ).color(r, g, b, a).endVertex();
                builder.pos(right, bottom, ingameZ).color(r, g, b, a).endVertex();
                builder.pos(left, bottom, ingameZ).color(r, g, b, a).endVertex();
            }
        }
    }
}
