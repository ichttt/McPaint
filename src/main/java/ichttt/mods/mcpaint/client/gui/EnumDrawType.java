package ichttt.mods.mcpaint.client.gui;

import java.awt.Color;
import java.util.Arrays;

public enum EnumDrawType {
    PENCIL(true) {
        @Override
        public void draw(GuiDraw gui, int pixelX, int pixelY, int size) {
            drawInSize(gui.picture, gui.color.getRGB(), pixelX, pixelY, size);
        }
    }, FILL(false) {
        @Override
        public void draw(GuiDraw gui, int pixelX, int pixelY, int size) {
            for (int[] subArray : gui.picture) {
                Arrays.fill(subArray, gui.color.getRGB());
            }
        }
    }, ERASER(true) {
        @Override
        public void draw(GuiDraw gui, int pixelX, int pixelY, int size) {
            drawInSize(gui.picture, GuiDraw.ZERO_ALPHA, pixelX, pixelY, size);
        }
    }, PICK_COLOR(false) {
        @Override
        public void draw(GuiDraw gui, int pixelX, int pixelY, int size) {
            gui.color = new Color(gui.picture[pixelX][pixelY]);
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

    public abstract void draw(GuiDraw gui, int pixelX, int pixelY, int size);

    EnumDrawType(boolean hasSizeRegulator) {
        this.hasSizeRegulator = hasSizeRegulator;
    }
}
