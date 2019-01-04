package ichttt.mods.mcpaint.client.render.batch.pixel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PixelRect implements Iterable<PixelLine> {
    private final List<PixelLine> lines = new ArrayList<>();
    public final int color;

    public PixelRect(PixelLine first) {
        lines.add(first);
        this.color = first.pixelInfos.get(0).color;
    }

    public boolean canAdd(PixelLine line) {
        for (PixelLine line1 : lines) {
            if (line1.isNeighbour(line))
                return true;
        }
        return false;
    }

    public boolean addLine(PixelLine line) {
        if (canAdd(line)) {
            lines.add(line);
            return true;
        }
        return false;
    }

    public List<PixelInfo> getMergedLines() {
        List<PixelInfo> list = new ArrayList<>();
        for (PixelLine line : lines) {
            list.addAll(line.pixelInfos);
        }
        return list;
    }

    @Nonnull
    @Override
    public Iterator<PixelLine> iterator() {
        return this.lines.iterator();
    }


    public boolean couldAdjust(PixelRect rect, int maxSingleDiff, int maxTotalDiff) {
        int otherA = rect.color >> 24 & 255;
        int otherR = (rect.color >> 16 & 255);
        int otherG = (rect.color >> 8 & 255);
        int otherB = (rect.color & 255);
        int a = this.color >> 24 & 255;
        int r = this.color >> 16 & 255;
        int g = this.color >> 8 & 255;
        int b = this.color & 255;
        int aDiff = diff(a, otherA);
        int rDiff = diff(r, otherR);
        int gDiff = diff(g, otherG);
        int bDiff = diff(b, otherB);
        if (aDiff + rDiff + gDiff + bDiff > maxTotalDiff)
            return false;
        return aDiff <= maxSingleDiff && rDiff <= maxSingleDiff && gDiff <= maxSingleDiff && bDiff <= maxSingleDiff;
    }

    public int totalDiffTo(PixelRect rect) {
        int otherA = rect.color >> 24 & 255;
        int otherR = (rect.color >> 16 & 255);
        int otherG = (rect.color >> 8 & 255);
        int otherB = (rect.color & 255);
        int a = this.color >> 24 & 255;
        int r = this.color >> 16 & 255;
        int g = this.color >> 8 & 255;
        int b = this.color & 255;
        int aDiff = diff(a, otherA);
        int rDiff = diff(r, otherR);
        int gDiff = diff(g, otherG);
        int bDiff = diff(b, otherB);
        return aDiff + rDiff + gDiff + bDiff;
    }

    private int diff(int a, int b) {
        return Math.abs(a - b);
    }
}
