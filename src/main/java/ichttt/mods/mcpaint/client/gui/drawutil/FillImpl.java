package ichttt.mods.mcpaint.client.gui.drawutil;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

//Copied from stackoverflow
//Reformatted and rearranged for my use case
//https://stackoverflow.com/questions/2783204/flood-fill-using-a-stack
public class FillImpl {
    public static void floodFillImage(int[][] picture, int x, int y, int color) {
        int srcColor = picture[x][y];
        boolean[][] hits = new boolean[picture.length][picture[0].length];

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x, y));

        while (!queue.isEmpty()) {
            Point p = queue.remove();

            if (floodFillImageDo(picture, hits, p.x, p.y, srcColor, color)) {
                queue.add(new Point(p.x, p.y - 1));
                queue.add(new Point(p.x, p.y + 1));
                queue.add(new Point(p.x - 1, p.y));
                queue.add(new Point(p.x + 1, p.y));
            }
        }
    }

    private static boolean floodFillImageDo(int[][] picture, boolean[][] hits, int x, int y, int srcColor, int tgtColor) {
        if (y < 0) return false;
        if (x < 0) return false;
        if (y > picture[0].length - 1) return false;
        if (x > picture.length - 1) return false;

        if (hits[y][x]) return false;

        if (picture[x][y] != srcColor) return false;

        // valid, paint it

        picture[x][y] = tgtColor;
        hits[y][x] = true;
        return true;
    }

}
