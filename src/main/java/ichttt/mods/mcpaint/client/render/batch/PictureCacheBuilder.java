package ichttt.mods.mcpaint.client.render.batch;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.render.CachedBufferBuilder;
import ichttt.mods.mcpaint.client.render.PictureRenderer;
import ichttt.mods.mcpaint.client.render.batch.pixel.PixelInfo;
import ichttt.mods.mcpaint.client.render.batch.pixel.PixelLine;
import ichttt.mods.mcpaint.client.render.batch.pixel.PixelRect;
import ichttt.mods.mcpaint.common.block.IOptimisationCallback;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PictureCacheBuilder {

    public static void batch(int[][] picture, byte scaleFactor, IOptimisationCallback callback) {
        if (picture == null) throw new IllegalArgumentException("No paint data");
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
        if (callback.isInvalid()) return;

        List<List<PixelInfo>> finalDrawLists = new ArrayList<>();
        for (Int2ObjectMap.Entry<List<PixelInfo>> entry : colorMap.int2ObjectEntrySet()) {
            List<PixelInfo> pixels = entry.getValue();
            List<PixelLine> lines = new ArrayList<>();
            //Make lines
            for (PixelInfo currentPixel : pixels) {
                List<PixelLine> neighbours = lines.stream().filter(pixelLine -> pixelLine.canAdd(currentPixel)).collect(Collectors.toList());
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

            if (callback.isInvalid()) return;
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
            for (PixelRect rect : rects) {
                List<PixelInfo> mergedInfos = rect.getMergedLines();
                finalDrawLists.add(mergedInfos);
            }
        }
        if (callback.isInvalid()) return;

        //If this is done, we can finally fill the buffer
        int pixelsInFinalList = 0;
        for (List<PixelInfo> infos : finalDrawLists) {
            pixelsInFinalList += infos.size();
        }
        if (pixelsToDraw  != pixelsInFinalList) {
            MCPaint.LOGGER.warn("LOST/DUPLICATE INFORMATION!!!!! {} pixels in array, {} in final list", pixelsToDraw, pixelsInFinalList);
            callback.optimizationFailed();
            return;
        }
        MCPaint.LOGGER.info("Merged {} pixels in picture to {} rectangles to draw", pixelsToDraw, finalDrawLists.size());
        //Start filling a buffer
        CachedBufferBuilder cachedBufferBuilder = new CachedBufferBuilder(finalDrawLists.size() * 16 + 4);
        cachedBufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (List<PixelInfo> infos : finalDrawLists) {
            int left = infos.get(0).x;
            int top = infos.get(0).y;
            int right = infos.get(infos.size() - 1).x;
            int bottom = infos.get(infos.size() - 1).y;
            int color = infos.get(0).color;
            for (PixelInfo info : infos) {
                //validate
                left = Math.min(left, info.x);
                right = Math.max(right, info.x);
                top = Math.min(top, info.y);
                bottom = Math.max(bottom, info.y);
            }
            double leftDraw = 0 - (((left) * scaleFactor) / 128F);
            double topDraw = 1 - (((top) * scaleFactor) / 128F);
            double rightDraw = 0 - (((right + 1) * scaleFactor) / 128F);
            double bottomDraw = 1 - (((bottom + 1) * scaleFactor) / 128F);
            PictureRenderer.drawToBuffer(color, cachedBufferBuilder, leftDraw, topDraw, rightDraw, bottomDraw);
        }
        cachedBufferBuilder.finishBuilding();
        callback.provideFinishedBuffer(cachedBufferBuilder);
        MCPaint.LOGGER.info("Taking {} of memory", cachedBufferBuilder.getSize());
    }

    public static CachedBufferBuilder buildSimple(IPaintable paint) {
        CachedBufferBuilder buffer = new CachedBufferBuilder(262144);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        PictureRenderer.renderInGame(paint.getScaleFactor(), buffer, paint.getPictureData());
        return buffer;
    }
}
