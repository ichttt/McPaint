package ichttt.mods.mcpaint.client.render;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.MCPaintConfig;

public class BufferManager {
    private final CachedBufferBuilder[] mips;
    private final CachedBufferBuilder fullSize;
    private final int fullSizeRect;
    private final int maxSize;
    private int[] sizes;

    public BufferManager(CachedBufferBuilder fullSize, int fullSizeRect, int maxMips, int maxSize) {
        this.fullSize = fullSize;
        this.fullSizeRect = fullSizeRect;
        this.maxSize = maxSize;
        this.mips = new CachedBufferBuilder[maxMips];
        this.sizes = new int[maxMips];
    }

    public CachedBufferBuilder get(int resolution) {
        if (resolution >= maxSize)
            return fullSize;
        int mipMap = maxSize / resolution;
        if (mipMap > mips.length)
            mipMap = mips.length;
        while (mipMap > 0) {
            mipMap--;
            CachedBufferBuilder buffer = mips[mipMap];
            if (buffer != null) {
                return buffer;
            }
        }
        return fullSize;
    }

    public void putMips(CachedBufferBuilder buffer, int rects, int index) {
        if (mips[index] != null)
            throw new IllegalStateException("Mip at index " + index + " already present");
        if (sizes == null)
            throw new IllegalStateException("Already complete!");
        if (rects == -1) {
            mips[index] = index == 0 ? fullSize : mips[index - 1];
            sizes[index] = getRects(index - 1);
        } else {
            mips[index] = buffer;
            sizes[index] = rects;
        }
    }

    private int getRects(int index) {
        if (index == -1)
            return fullSizeRect;
        int rects = sizes[index];
        if (rects == 0)
            throw new RuntimeException("Index " + index + " does not have mip info");
        return rects;
    }

    public void complete() {
        sizes = null;
        for (int i = 0; i < mips.length; i++) {
            if (mips[i] == null)
                mips[i] = fullSize;
        }
    }

    public boolean needDiscard(int rects, int mipIndex) {
        boolean result = rects == -1 || !MCPaintConfig.CLIENT.enableMipMaps.get() || (rects / getRects(mipIndex - 2)) > MCPaintConfig.CLIENT.maxMipSize.get();
        if (result)
            MCPaint.LOGGER.debug("Discarding mip level {}, with {} rects", mipIndex, rects);
        return result;
    }
}
