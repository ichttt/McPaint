package ichttt.mods.mcpaint.client.render.batch.pixel;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PixelInfo {
    public final int x;
    public final int y;
    public final int color;

    public PixelInfo(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public boolean isNeighbourX(PixelInfo info) {
        return Math.abs(info.x - this.x) == 1 && info.y == this.y;
    }

    public boolean isNeighbourY(PixelInfo info) {
        return Math.abs(info.y - this.y) == 1 && info.x == this.x;
    }

    @Override
    public int hashCode() {
        int hash = x;
        hash = hash *31 + y;
        hash = hash * 31 + color;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof PixelInfo) {
            PixelInfo other = (PixelInfo) obj;
            return this.color == other.color && this.x == other.x && this.y == other.y;
        }
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("x", x)
                .append("y", y)
                .append("color", color)
                .toString();
    }
}
