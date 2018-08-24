package ichttt.mods.mcpaint.client.render.batch.pixel;

public class PixelInfo {
    public final int x;
    public final int y;
    public final int color;

    public PixelInfo(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

//    public boolean isNeighbour(PixelInfo info) {
//        return isNeighbourX(info) || isNeighbourY(info);
//    }

    public boolean isNeighbourX(PixelInfo info) {
        return Math.abs(info.x - this.x) == 1 && info.y == this.y;
    }

    public boolean isNeighbourY(PixelInfo info) {
        return Math.abs(info.y - this.y) == 1 && info.x == this.x;
    }
}
