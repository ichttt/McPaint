package ichttt.mods.mcpaint.client.render.batch;

import com.google.common.base.Stopwatch;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.render.batch.pixel.PixelInfo;
import ichttt.mods.mcpaint.client.render.batch.pixel.PixelLine;
import ichttt.mods.mcpaint.client.render.batch.pixel.PixelRect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

public class LossyCompression {

    public static int[][] mipMap(int[][] original, int oldLevel) {
        if (oldLevel > 4 || oldLevel < 0)
            throw new IllegalArgumentException("Level " + oldLevel);
        int level = oldLevel;
        while (original.length % Math.pow(2, level) != 0) {
            level--;
            if (level < 0)
                throw new IllegalArgumentException("Weird size " + original.length);
        }
        if (level != oldLevel)
            System.out.println("WANTED " + oldLevel + " POSSIBLE " + level);
        if (original.length % Math.pow(2, level) != 0)
            throw new IllegalArgumentException("NonConform " + original.length);
        if (original[0].length != original.length)
            throw new IllegalArgumentException("DiffLen " + original.length + "/" + original[0].length);
        int increment = (int) Math.pow(2, level);
        float incSq = increment * increment;
        int posX = 0;
        int posY = 0;
        int[][] newPic = new int[original.length / increment][original.length / increment];
        for (int x = 0; x < original.length; x += increment) {
            for (int y = 0; y < original[x].length; y += increment) {
                int r = 0;
                int g = 0;
                int b = 0;
                int a = 0;
                for (int subX = x; subX < x + increment; subX++) {
                    for (int subY = y; subY < y + increment; subY++) {
                        int color = original[subX][subY];
                        a += color >> 24 & 255;
                        r += (color >> 16 & 255);
                        g += (color >> 8 & 255);
                        b += (color & 255);
                    }
                }
                r = Math.round(r / incSq);
                g = Math.round(g / incSq);
                b = Math.round(b / incSq);
                a = Math.round(a / incSq);
                int argb = a;
                argb = (argb << 8) + r;
                argb = (argb << 8) + g;
                argb = (argb << 8) + b;
                newPic[posX][posY] = argb;
                posY++;
            }
            posY = 0;
            posX++;
        }
        return newPic;
    }

    public static List<PixelRect> colorCompress(int maxTotalVar, int maxSingleVar, List<PixelRect> allRects) {
        if (maxSingleVar > 0 && maxTotalVar > 0) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            List<PixelRect> checkedOut = new ArrayList<>();
            for (PixelRect rect : allRects) {
                Optional<PixelRect> found = checkedOut.stream().filter(pixelLines -> StreamSupport.stream(rect.spliterator(), false).allMatch(pixelLines::canAdd)).filter(pixelLines -> pixelLines.couldAdjust(rect, maxSingleVar, maxTotalVar)).min(Comparator.comparingInt(o -> o.totalDiffTo(rect)));
                if (found.isPresent()) {
                    //TODO merge colors
                    PixelRect toMerge = found.get();
                    for (PixelLine line : rect) {
                        for (PixelInfo info : line)
                            info.drawColor = toMerge.color;
                        if (!toMerge.addLine(line))
                            throw new RuntimeException("You said we could be together :(");
                    }
                } else {
                    checkedOut.add(rect);
                }
            }
            stopwatch.stop();
            MCPaint.LOGGER.debug("Reduced {} rectangles to {} rectangles in {} ms", allRects.size(), checkedOut.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return checkedOut;
        }
        return allRects;
    }
}
