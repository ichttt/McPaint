package ichttt.mods.mcpaint.client.render.pixelbatch;

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
        int[][] pictureData = new int[orig.length][];
        for (int i = 0; i < orig.length; i++)
            pictureData[i] = orig[i].clone();
        byte scaleFactor = paintable.getScaleFactor();
        PictureCacheBuilder.batch(pictureData, scaleFactor, callback);
    }
}
