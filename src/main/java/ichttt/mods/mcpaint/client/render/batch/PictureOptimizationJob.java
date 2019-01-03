package ichttt.mods.mcpaint.client.render.batch;

import ichttt.mods.mcpaint.MCPaintConfig;
import ichttt.mods.mcpaint.client.render.BufferManager;
import ichttt.mods.mcpaint.client.render.CachedBufferBuilder;
import ichttt.mods.mcpaint.client.render.TESRCanvas;
import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import org.apache.commons.lang3.tuple.Pair;

public class PictureOptimizationJob implements Runnable {
    private final IPaintable paintable;
    private final IOptimisationCallback callback;

    public PictureOptimizationJob(IPaintable paintable, IOptimisationCallback callback) {
        this.paintable = paintable;
        this.callback = callback;
    }

    @Override
    public void run() {
        if (callback.isInvalid()) return;
        BufferManager cached = RenderCache.getIfPresent(paintable);
        if (cached != null) {
            callback.provideFinishedBuffer(cached);
            return;
        }
        int[][] pictureData = MCPaintUtil.copyOf(paintable.getPictureData());
        byte scaleFactor = paintable.getScaleFactor();
        Pair<CachedBufferBuilder, Integer> pair = PictureCacheBuilder.batch(pictureData, scaleFactor, callback, val -> false, 0, 0);
        if (pair == null)
            return;
        int maxMips = MCPaintConfig.CLIENT.enableMipMaps ? getMaxPass(pictureData.length) : 0;
        BufferManager manager = new BufferManager(pair.getLeft(), pair.getRight(), maxMips, pictureData.length);
        callback.provideFinishedBuffer(manager);
        if (maxMips > 0) {
            for (int i = 1; i <= maxMips; i++) {
                int[][] newPicture = LossyCompression.mipMap(pictureData, i);
                byte newScaleFactor = (byte) (scaleFactor * Math.pow(2, i));
                final int currentMip = i;
                pair = PictureCacheBuilder.batch(newPicture, newScaleFactor, callback, val -> manager.needDiscard(val, currentMip), MCPaintConfig.CLIENT.maxTotalColorDiffPerMip * i, MCPaintConfig.CLIENT.maxSingleColorDiffPerMip * i);
                if (pair != null)
                    manager.putMips(pair.getLeft(), pair.getRight(), i - 1);
                else
                    manager.putMips(null, -1, i - 1);
            }
        }
        manager.complete();
    }

    private static int getMaxPass(int res) {
        int lowestRes = (TESRCanvas.getRes(MCPaintConfig.CLIENT.maxPaintRenderDistance * MCPaintConfig.CLIENT.maxPaintRenderDistance));
        int mips = 0;
        while (lowestRes < res) {
            lowestRes *= 2;
            mips++;
        }
        return mips;
    }
}
