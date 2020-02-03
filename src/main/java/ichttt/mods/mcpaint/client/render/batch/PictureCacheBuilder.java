package ichttt.mods.mcpaint.client.render.batch;

import com.google.common.base.Stopwatch;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.render.batch.pixel.PixelInfo;
import ichttt.mods.mcpaint.client.render.batch.pixel.PixelLine;
import ichttt.mods.mcpaint.client.render.batch.pixel.PixelRect;
import ichttt.mods.mcpaint.client.render.OptimizedPictureData;
import ichttt.mods.mcpaint.client.render.OptimizedPictureRenderer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PictureCacheBuilder {

    public static OptimizedPictureRenderer batch(int[][] picture, byte scaleFactor, IOptimisationCallback callback, Function<Integer, Boolean> shouldDiscard, int maxTotalVar, int maxSingleVar) {
        if (picture == null) throw new IllegalArgumentException("No paint data");
        if (callback.isInvalid()) return null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        int pixelsToDraw = 0;

        Int2ObjectMap<List<PixelInfo>> colorMap = new Int2ObjectOpenHashMap<>();
        //Divide picture into colors
        for (int x = 0; x < picture.length; x++) {
            int[] column = picture[x];
            for (int y = 0; y < column.length; y++) {
                int color = column[y];
                int a = (color >> 24 & 255);
                if (a <= 2) continue; //Gets alpha skipped either way, keep it out as early as possible
                pixelsToDraw++;
                colorMap.computeIfAbsent(color, value -> new ArrayList<>()).add(new PixelInfo(x, y, color));
            }
        }
        if (callback.isInvalid()) return null;

        List<PixelRect> allRects = new ArrayList<>();
        for (Int2ObjectMap.Entry<List<PixelInfo>> entry : colorMap.int2ObjectEntrySet()) {
            List<PixelInfo> pixels = entry.getValue();
            List<PixelLine> lines = new ArrayList<>();
            //Make lines
            for (PixelInfo currentPixel : pixels) {
                List<PixelLine> neighbours = new ArrayList<>();
                for (PixelLine line : lines) {
                    if (line.canAdd(currentPixel))
                        neighbours.add(line);
                }
                if (neighbours.isEmpty()) {
                    lines.add(new PixelLine(currentPixel));
                } else if (neighbours.size() == 1) {
                    neighbours.get(0).add(currentPixel);
                } else {
                    PixelLine activeLine = null;
                    for (PixelLine infos : neighbours) {
                        if (activeLine == null) {
                            activeLine = infos;
                            activeLine.add(currentPixel);
                            continue;
                        }
                        neighbours.remove(infos);
                        activeLine.addAll(infos);
                    }
                }
            }

            if (callback.isInvalid()) return null;
            //Now sort into rects
            List<PixelRect> rects = new ArrayList<>();
            for (PixelLine line : lines) {
                boolean added = false;
                for (PixelRect rect : rects) {
                    if (rect.addLine(line)) {
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    rects.add(new PixelRect(line));
                }
            }
            allRects.addAll(rects);
        }
        if (callback.isInvalid()) return null;

        //If this is done, we can finally fill the buffer
        int allPixels = 0;
        for (PixelRect rect : allRects) {
            for (PixelLine line : rect)
                allPixels += line.size();
        }
        if (pixelsToDraw  != allPixels) {
            MCPaint.LOGGER.warn("LOST/DUPLICATE INFORMATION!!!!! {} pixels in array, {} in final list", pixelsToDraw, allPixels);
            callback.optimizationFailed();
            return null;
        }
        stopwatch.stop();
        MCPaint.LOGGER.debug("Merged {} pixels in picture to {} rectangles in {} ms", pixelsToDraw, allRects.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        allRects = LossyCompression.colorCompress(maxTotalVar, maxSingleVar, allRects);
        if (shouldDiscard.apply(allRects.size())) return null;
        List<List<PixelInfo>> mergedDrawList = allRects.stream().map(PixelRect::getMergedLines).collect(Collectors.toList());
        stopwatch.reset();
        stopwatch.start();
        //Create the data
        OptimizedPictureData[] dataArray = new OptimizedPictureData[mergedDrawList.size()];
        for (int i = 0; i < mergedDrawList.size(); i++) {
            List<PixelInfo> infos = mergedDrawList.get(i);
            int left = infos.get(0).x;
            int top = infos.get(0).y;
            int right = infos.get(infos.size() - 1).x;
            int bottom = infos.get(infos.size() - 1).y;
            int color = infos.get(0).drawColor;
            for (PixelInfo info : infos) {
                //find all corners
                left = Math.min(left, info.x);
                right = Math.max(right, info.x);
                top = Math.min(top, info.y);
                bottom = Math.max(bottom, info.y);
            }
            float leftDraw = (((left) * scaleFactor) / 128F);
            float topDraw = 1 - (((top) * scaleFactor) / 128F);
            float rightDraw = (((right + 1) * scaleFactor) / 128F);
            float bottomDraw = 1 - (((bottom + 1) * scaleFactor) / 128F);
            OptimizedPictureData data = new OptimizedPictureData(color, leftDraw, topDraw, rightDraw, bottomDraw);
            dataArray[i] = data;
//            if (PictureRenderer.drawToBuffer(Matrix4f.makeTranslate(0,0,0), color, cachedBufferBuilder, leftDraw, topDraw, rightDraw, bottomDraw))
//                MCPaint.LOGGER.warn("Region left={} right={} top={} bottom={} color{} has not been filtered out from batched picture!", left, right, top, bottom, color);
        }
        stopwatch.stop();
        MCPaint.LOGGER.info("Reduced {} instructions to {} instructions in {} us", picture.length * picture.length, dataArray.length, stopwatch.elapsed(TimeUnit.MICROSECONDS));
        return new OptimizedPictureRenderer(dataArray);
    }
}
