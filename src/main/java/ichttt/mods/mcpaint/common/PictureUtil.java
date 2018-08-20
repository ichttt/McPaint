package ichttt.mods.mcpaint.common;

public class PictureUtil {

    public static boolean hasAlpha(int[][] picture) {
        for (int[] subarray : picture) {
            for (int pixel : subarray) {
                int alpha = (pixel >> 24 & 255);
                if (alpha > 3 && alpha < 255) {
                    return true;
                }
            }
        }
        return false;
    }
}
