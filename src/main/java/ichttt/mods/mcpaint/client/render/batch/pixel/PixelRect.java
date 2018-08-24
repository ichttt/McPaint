package ichttt.mods.mcpaint.client.render.batch.pixel;

import ichttt.mods.mcpaint.client.render.batch.pixel.PixelInfo;
import ichttt.mods.mcpaint.client.render.batch.pixel.PixelLine;

import java.util.ArrayList;
import java.util.List;

public class PixelRect {
    private final List<PixelLine> lines = new ArrayList<>();

    public PixelRect(PixelLine first) {
        lines.add(first);
    }

    public boolean addLine(PixelLine line) {
        if (lines.stream().anyMatch(line1 -> line1.isNeighbour(line))) {
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
