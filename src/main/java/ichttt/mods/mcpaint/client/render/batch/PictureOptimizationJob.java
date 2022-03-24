package ichttt.mods.mcpaint.client.render.batch;

import ichttt.mods.mcpaint.MCPaintConfig;
import ichttt.mods.mcpaint.client.render.buffer.BufferManager;
import ichttt.mods.mcpaint.client.render.TERCanvas;
import ichttt.mods.mcpaint.client.render.OptimizedPictureRenderer;
import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.capability.IPaintable;

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
        int[][] pictureData = paintable.getPictureData(true);
        byte scaleFactor = paintable.getScaleFactor();
        OptimizedPictureRenderer renderer = PictureCacheBuilder.batch(pictureData, scaleFactor, callback, val -> false, 0, 0);
        if (renderer == null)
            return;
        int maxMips = MCPaintConfig.CLIENT.enableMipMaps.get() ? getMaxPass(pictureData.length) : 0;
        BufferManager manager = new BufferManager(renderer, maxMips, pictureData.length);
        callback.provideFinishedBuffer(manager);
        if (maxMips > 0) {
            for (int i = 1; i <= maxMips; i++) {
                int[][] newPicture = LossyCompression.mipMap(pictureData, i);
                byte newScaleFactor = (byte) (scaleFactor * Math.pow(2, i));
                final int currentMip = i;
                renderer = PictureCacheBuilder.batch(newPicture, newScaleFactor, callback, val -> manager.needDiscard(val, currentMip), MCPaintConfig.CLIENT.maxTotalColorDiffPerMip.get() * i, MCPaintConfig.CLIENT.maxSingleColorDiffPerMip.get() * i);
                manager.putMips(renderer, i - 1);
            }
        }
        manager.complete();
    }

    private static int getMaxPass(int res) {
        int maxDist = MCPaintConfig.CLIENT.maxPaintRenderDistance.get();
        int lowestRes = (TERCanvas.getRes(maxDist * maxDist));
        int mips = 0;
        while (lowestRes < res) {
            lowestRes *= 2;
            mips++;
        }
        return mips;
    }
}
