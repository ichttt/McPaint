package ichttt.mods.mcpaint.client.render.batch;

import ichttt.mods.mcpaint.client.render.CachedBufferBuilder;
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
        CachedBufferBuilder cached = RenderCache.getIfPresent(paintable);
        if (cached != null) {
            callback.provideFinishedBuffer(cached);
            return;
        }
        int[][] pictureData = MCPaintUtil.copyOf(paintable.getPictureData());
        byte scaleFactor = paintable.getScaleFactor();
        PictureCacheBuilder.batch(pictureData, scaleFactor, callback);
    }
}
