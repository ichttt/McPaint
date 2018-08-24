package ichttt.mods.mcpaint.client.render.pixelbatch;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.render.CachedBufferBuilder;
import ichttt.mods.mcpaint.client.render.PictureRenderer;
import ichttt.mods.mcpaint.client.render.PixelInfo;
import ichttt.mods.mcpaint.common.block.IOptimisationCallback;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PictureCacheBuilder {

    //This is awful
    //It's probably way too complicated and slow, but better than no optimisation
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
        int pixelsAfterRegions = 0;
        for (Int2ObjectMap.Entry<List<PixelInfo>> entry : colorMap.int2ObjectEntrySet()) {
            //Sort into regions
            List<List<PixelInfo>> regions = new ArrayList<>();
            for (PixelInfo info : entry.getValue()) {
                List<List<PixelInfo>> lists = regions.stream().filter(pixelInfos -> pixelInfos.stream().anyMatch(pixelInfo -> pixelInfo.isNeighbour(info))).collect(Collectors.toList());
                if (lists.size() > 1) {
                    List<PixelInfo> mergedList = lists.get(0);
                    mergedList.add(info);
                    for (List<PixelInfo> deletedList : lists) {
                        if (deletedList == mergedList) continue;
                        mergedList.addAll(deletedList);
                        regions.remove(deletedList);
                    }
                } else if (lists.size() == 1) {
                    lists.get(0).add(info);
                } else {
                    List<PixelInfo> newNeighborList = new ArrayList<>();
                    newNeighborList.add(info);
                    regions.add(newNeighborList);
                }
            }
            if (callback.isInvalid()) return;

            //Count to provide more debug info in case anything goes wrong
            for (List<PixelInfo> infos : regions) {
                pixelsAfterRegions += infos.size();
            }

            //Region map for color is complete, split out into rectangles
            for (List<PixelInfo> region : regions) {
                Int2ObjectLinkedOpenHashMap<List<List<PixelInfo>>> xRowList = new Int2ObjectLinkedOpenHashMap<>();
                for (PixelInfo info : region) {
                    List<List<PixelInfo>> infos = xRowList.computeIfAbsent(info.x, value -> new ArrayList<>());
                    List<List<PixelInfo>> validLists = infos.stream().filter(pixelInfos -> pixelInfos.stream().anyMatch(pixelInfo -> pixelInfo.isNeighbourY(info))).collect(Collectors.toList());
                    if (validLists.isEmpty()) {
                        List<PixelInfo> list = new ArrayList<>();
                        list.add(info);
                        infos.add(list);
                    } else if (validLists.size() == 1) {
                        validLists.get(0).add(info);
                    } else {
                        List<PixelInfo> mergedList = validLists.get(0);
                        mergedList.add(info);
                        for (List<PixelInfo> deletedList : validLists) {
                            if (deletedList == mergedList) continue;
                            mergedList.addAll(deletedList);
                            infos.remove(deletedList);
                        }
                    }
                }
                if (callback.isInvalid()) return;

                Map<List<PixelInfo>, Integer> pixelListToInitalSize = new IdentityHashMap<>();
                for (List<List<PixelInfo>> infosList : xRowList.values()) {
                    for (List<PixelInfo> infos : infosList)
                        pixelListToInitalSize.put(infos, infos.size());
                }

                //we now have them in Y-Rows. If two neighboured rows are the same start and end point, we can merge them.
                for (int i = 0; i < picture.length; i++) {
                    List<List<PixelInfo>> currentLists = xRowList.get(i);
                    List<List<PixelInfo>> prevLists = xRowList.get(i - 1);
                    if (currentLists != null) {
                        if (prevLists != null) {
                            for (List<PixelInfo> currentList : currentLists) {
                                int size = pixelListToInitalSize.get(currentList);
                                List<List<PixelInfo>> possibleLists = prevLists.stream()
                                        .filter(pixelInfos -> pixelListToInitalSize.get(pixelInfos) == size)
                                        .filter(pixelInfos -> pixelInfos.stream()
                                                .allMatch(pixelInfo -> currentList.stream()
                                                        .anyMatch(currentPixelInfo -> currentPixelInfo.isNeighbourY(pixelInfo))))
                                        .collect(Collectors.toList());
                                if (possibleLists.size() > 1) {
                                    MCPaint.LOGGER.error("{} lists were found while mapping rows that have neighbours, it should only be max 1!", possibleLists.size());
                                    callback.optimizationFailed();
                                    return;
                                } else if (possibleLists.isEmpty()) {
                                    if (!finalDrawLists.contains(currentList))
                                        finalDrawLists.add(currentList);
                                } else {
                                    List<PixelInfo> prevList = possibleLists.get(0);
                                    currentList.addAll(prevList);
                                    finalDrawLists.remove(prevList);
                                    if (!finalDrawLists.contains(currentList))
                                        finalDrawLists.add(currentList);
                                }
                            }
                        } else {
                            finalDrawLists.addAll(currentLists);
                        }
                    }
                }
            }
        }
        if (callback.isInvalid()) return;

        //If this is done, we can finally fill the buffer
        int pixelsInFinalList = 0;
        for (List<PixelInfo> infos : finalDrawLists) {
            pixelsInFinalList += infos.size();
        }
        if (pixelsToDraw != pixelsAfterRegions || pixelsAfterRegions != pixelsInFinalList) {
            MCPaint.LOGGER.warn("LOST/DUPLICATE INFORMATION!!!!! {} pixels in array, {} in region list, {} in final list", pixelsToDraw, pixelsAfterRegions, pixelsInFinalList);
            callback.optimizationFailed();
            return;
        }
        MCPaint.LOGGER.info("Merged {} pixels in picture to {} rectangles to draw", pixelsToDraw, finalDrawLists.size());
        //Start filling a buffer
        CachedBufferBuilder cachedBufferBuilder = new CachedBufferBuilder(262144);
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
    }

    public static CachedBufferBuilder buildSimple(IPaintable paint) {
        CachedBufferBuilder buffer = new CachedBufferBuilder(262144);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        PictureRenderer.renderInGame(paint.getScaleFactor(), buffer, paint.getPictureData());
        return buffer;
    }
}
