package ichttt.mods.mcpaint.client.render;

import net.minecraft.client.renderer.BufferBuilder;

public class CachedBufferBuilder extends BufferBuilder {
    private boolean building = true;
    private int size = -1;
    public CachedBufferBuilder(int bufferSizeIn) {
        super(bufferSizeIn);
    }

    @Override
    public void finishDrawing() {
        throw new UnsupportedOperationException("We do not finish - We just draw the same buffer over and over");
    }

    @Override
    public void setTranslation(double x, double y, double z) {
        throw new UnsupportedOperationException("We don't support translations");
    }

    @Override
    public void endVertex() {
        if (!building) throw new IllegalStateException("Not in building pass!");
        super.endVertex();
    }

    @Override
    public void reset() {
        //We do nothing
    }

    public void finishBuilding() {
        building = false;
//        this.byteBuffer = this.byteBuffer.compact(); TODO AT
        this.size = this.getByteBuffer().position();
    }

    public int getSize() {
        if (building) throw new IllegalStateException("In building pass!");
        return size;
    }
}
