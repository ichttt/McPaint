package ichttt.mods.mcpaint.client.gui.drawutil;

import ichttt.mods.mcpaint.client.gui.GuiDraw;

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
            EnumDrawType.fill(picture, pixelX, pixelY, colorRGB, originalColor);
            return color;
        }
    }, ERASER(true) {
        @Nonnull
        @Override
        public Color draw(int[][] picture, Color color, int pixelX, int pixelY, int size) {
            drawInSize(picture, GuiDraw.ZERO_ALPHA, pixelX, pixelY, size);
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

    private static void fill(int[][] picture, int posX, int posY, int replacement, int current) {
        if (picture[posX][posY] == current) {
            picture[posX][posY] = replacement;
            if (posX > 0)
                fill(picture, posX - 1, posY, replacement, current);
            if (posX + 1 < picture.length)
                fill(picture, posX + 1, posY, replacement, current);
            if (posY > 0)
                fill(picture, posX, posY - 1, replacement, current);
            if (posY + 1 < picture[posX].length)
                fill(picture, posX, posY + 1 , replacement, current);
        }
    }

    public final boolean hasSizeRegulator;

    @Nonnull
    public abstract Color draw(int[][] picture, Color color, int pixelX, int pixelY, int size);

    EnumDrawType(boolean hasSizeRegulator) {
        this.hasSizeRegulator = hasSizeRegulator;
    }
}
