package ichttt.mods.mcpaint.client.render.batch.pixel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PixelLine implements Iterable<PixelInfo> {
    private static final boolean DEBUG = Boolean.getBoolean("mcpaint.pixelline.debug");
    private final int y;
    public final List<PixelInfo> pixelInfos = new ArrayList<>();

    public PixelLine(PixelInfo info) {
        pixelInfos.add(info);
        y = info.y;
    }

    public boolean canAdd(PixelInfo info) {
        if (info.y != y) return false;
        for (PixelInfo info1 : pixelInfos) {
            if (info1.isNeighbourX(info))
                return true;
        }
        return false;
    }

    public void add(PixelInfo info) {
        if (DEBUG && !canAdd(info)) throw new IllegalArgumentException("Cannot add pixel!");
        pixelInfos.add(info);
    }

    @Nonnull
    @Override
    public Iterator<PixelInfo> iterator() {
        return this.pixelInfos.iterator();
    }

    public boolean isNeighbour(PixelLine line) {
        if (this.pixelInfos.size() != line.pixelInfos.size())
            return false;
        if (!line.isNeighbourY(line))
            return false;
        return line.pixelInfos.stream().allMatch(externalPixel -> this.pixelInfos.stream().anyMatch(internalPixel -> internalPixel.x == externalPixel.x));
    }

    private boolean isNeighbourY(PixelLine line) {
        return Math.abs(line.y - this.y) == 1;
    }

    public void addAll(PixelLine infos) {
        if (DEBUG) {
            for (PixelInfo info : infos.pixelInfos) {
                add(info);
            }
        } else {
            this.pixelInfos.addAll(infos.pixelInfos);
        }
    }

    public int size() {
        return this.pixelInfos.size();
    }
}
