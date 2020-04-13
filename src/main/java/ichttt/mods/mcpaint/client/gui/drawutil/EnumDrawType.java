package ichttt.mods.mcpaint.client.gui.drawutil;

import ichttt.mods.mcpaint.client.gui.DrawScreen;
import ichttt.mods.mcpaint.client.gui.DrawScreenHelper;

import javax.annotation.Nonnull;
import java.awt.*;

public enum EnumDrawType {
    PENCIL(true) {
        @Nonnull
        @Override
        public Color draw(int[][] picture, Color color, int pixelX, int pixelY, int size) {
            drawInSize(picture, color.getRGB(), pixelX, pixelY, size);
            return color;
        }
    }, FILL(false) {
        @Nonnull
        @Override
        public Color draw(int[][] picture, Color color, int pixelX, int pixelY, int size) {
            int colorRGB = color.getRGB();
            int originalColor = picture[pixelX][pixelY];
            if (originalColor == colorRGB) return color;
            FillImpl.floodFillImage(picture, pixelX, pixelY, colorRGB);
            return color;
        }
    }, ERASER(true) {
        @Nonnull
        @Override
        public Color draw(int[][] picture, Color color, int pixelX, int pixelY, int size) {
            drawInSize(picture, DrawScreenHelper.ZERO_ALPHA, pixelX, pixelY, size);
            return color;
        }
    }, PICK_COLOR(false) {
        @Nonnull
        @Override
        public Color draw(int[][] picture, Color color, int pixelX, int pixelY, int size) {
            return new Color(picture[pixelX][pixelY], true);
        }
    };

    static void drawInSize(int[][] picture, int color, int pixelX, int pixelY, int size) {
        if (size == 1)
            picture[pixelX][pixelY] = color;
        else {
            size--;
            int minPixelX = Math.max(0, pixelX - size);
            int maxPixelX = Math.min(picture.length - 1, pixelX + size);
            int minPixelY = Math.max(0, pixelY - size);
            int maxPixelY = Math.min(picture.length - 1, pixelY + size);
            for (int x = 0; x <= (maxPixelX - minPixelX); x++) {
                for (int y = 0; y <= (maxPixelY - minPixelY); y++) {
                    picture[minPixelX + x][minPixelY + y] = color;
                }
            }
        }
    }

    public final boolean hasSizeRegulator;

    @Nonnull
    public abstract Color draw(int[][] picture, Color color, int pixelX, int pixelY, int size);

    EnumDrawType(boolean hasSizeRegulator) {
        this.hasSizeRegulator = hasSizeRegulator;
    }
}
