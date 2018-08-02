package ichttt.mods.mcpaint.client.gui;

import java.util.Arrays;

public enum EnumDrawType {
    PENCIL(true) {
        @Override
        public void draw(int[][] picture, int color, int pixelX, int pixelY, int size) {
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
    }, FILL(false) {
        @Override
        public void draw(int[][] picture, int color, int pixelX, int pixelY, int size) {
            for (int[] subArray : picture) {
                Arrays.fill(subArray, color);
            }
        }
    };

    public final boolean hasSizeRegulator;

    public abstract void draw(int[][] picture, int color, int pixelX, int pixelY, int size);

    EnumDrawType(boolean hasSizeRegulator) {
        this.hasSizeRegulator = hasSizeRegulator;
    }
}
