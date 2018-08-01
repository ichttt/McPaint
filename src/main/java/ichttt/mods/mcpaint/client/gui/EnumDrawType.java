package ichttt.mods.mcpaint.client.gui;

import java.util.Arrays;

public enum EnumDrawType {
    PENCIL {
        @Override
        public void draw(int[][] picture, int color, int pixelX, int pixelY) {
            picture[pixelX][pixelY] = color;
        }
    }, FILL {
        @Override
        public void draw(int[][] picture, int color, int pixelX, int pixelY) {
            for (int[] subArray : picture) {
                Arrays.fill(subArray, color);
            }
        }
    };

    public abstract void draw(int[][] picture, int color, int pixelX, int pixelY);
}
