package ichttt.mods.mcpaint.client.render.buffer;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.nio.ByteBuffer;
import java.util.Objects;

public class GatheringBufferBuilder extends BufferBuilder {
    private boolean building = true;
    private int size = -1;
    public GatheringBufferBuilder(int bufferSizeIn) {
        super(bufferSizeIn);
    }

    @Override
    public void finishDrawing() {
        throw new UnsupportedOperationException("We do not finish - We just draw the same buffer over and over");
    }

    @Override
    public void endVertex() {
        if (!building) throw new IllegalStateException("Not in building pass!");
        super.endVertex();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Pair<DrawState, ByteBuffer> getAndResetData() {
        throw new UnsupportedOperationException();
    }

    public ByteBuffer finishBuilding() {
        building = false;
        ByteBuffer buffer = ObfuscationReflectionHelper.getPrivateValue(BufferBuilder.class, this, "byteBuffer");
        buffer = Objects.requireNonNull(buffer).compact();
        this.size = buffer.position();
        return buffer;
    }

    public int getSize() {
        if (building) throw new IllegalStateException("In building pass!");
        return size;
    }
}
