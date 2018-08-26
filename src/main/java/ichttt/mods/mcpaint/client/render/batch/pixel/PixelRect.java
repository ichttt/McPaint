package ichttt.mods.mcpaint.client.render.batch.pixel;

import java.util.ArrayList;
import java.util.List;

public class PixelRect {
    private final List<PixelLine> lines = new ArrayList<>();

    public PixelRect(PixelLine first) {
        lines.add(first);
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
}
