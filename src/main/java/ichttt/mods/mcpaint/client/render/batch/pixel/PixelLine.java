package ichttt.mods.mcpaint.client.render.batch.pixel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PixelLine implements Iterable<PixelInfo> {
    public final List<PixelInfo> pixelInfos = new ArrayList<>();

    public PixelLine(PixelInfo info) {
        pixelInfos.add(info);
    }

    public boolean canAdd(PixelInfo info) {
        for (PixelInfo info1 : pixelInfos) {
            if (info1.isNeighbourX(info))
                return true;
        }
        return false;
    }

    public void add(PixelInfo info) {
        if (!canAdd(info)) throw new IllegalArgumentException("Cannot add pixel!");
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
        return line.pixelInfos.stream().allMatch(externalPixel -> this.pixelInfos.stream().anyMatch(internalPixel -> internalPixel.isNeighbourY(externalPixel)));
    }

    public void addAll(PixelLine infos) {
        for (PixelInfo info : infos.pixelInfos) {
            add(info);
        }
    }
}
