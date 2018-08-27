package ichttt.mods.mcpaint.client.render.batch;

import ichttt.mods.mcpaint.client.ClientProxy;
import ichttt.mods.mcpaint.common.block.IOptimisationCallback;
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
        int[][] orig = paintable.getPictureData();
        int[][] pictureData = ClientProxy.copyOf(orig);
        byte scaleFactor = paintable.getScaleFactor();
        PictureCacheBuilder.batch(pictureData, scaleFactor, callback);
    }
}
