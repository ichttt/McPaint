package ichttt.mods.mcpaint.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public class OptimizedPictureRenderer {
    private final OptimizedPictureData[] dataArray;

    public OptimizedPictureRenderer(OptimizedPictureData[] dataArray) {
        this.dataArray = dataArray;
    }

    public void renderPicture(Matrix4f matrix4f, VertexConsumer builder, int light) {
        for (OptimizedPictureData data : dataArray) {
            if (RenderUtil.drawToBuffer(matrix4f, data.color, builder, data.left, data.top, data.right, data.bottom, light))
                throw new RuntimeException("Not filtered out a pixel!");
        }
    }

    public void renderShard(Matrix4f matrix4f, VertexConsumer builder, int light, int idx) {
        OptimizedPictureData data = dataArray[idx];
        if (RenderUtil.drawToBuffer(matrix4f, data.color, builder, data.left, data.top, data.right, data.bottom, light))
            throw new RuntimeException("Not filtered out a pixel!");
    }

    public int getInstructions() {
        return dataArray.length;
    }
}