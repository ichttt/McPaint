package ichttt.mods.mcpaint.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import ichttt.mods.mcpaint.MCPaintConfig;
import ichttt.mods.mcpaint.client.render.buffer.BufferManager;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TERCanvas extends TileEntityRenderer<TileEntityCanvas> {
    private static final Direction[] VALUES = Direction.values();

    public TERCanvas(TileEntityRendererDispatcher p_i226006_1_) {
        super(p_i226006_1_);
    }

    private static void renderFace(MatrixStack matrix, IVertexBuilder vertexBuilder, TileEntityCanvas te, Direction facing, int light, double playerDistSq) {
        //Facing setup
        int xOffset = 0;
        int yOffset = 0;
        int zOffset = 0;
        int angle = 0;
        //maybe find a better way to avoid z-fighting, but this hacky shit works somehow
        double translationXOffset = 0D;
        double translationYOffset = 0D;
        double translationZOffset = 0D;
        switch (facing) {
            case NORTH:
                angle = 0;
                zOffset = 1;
                translationZOffset = 0.0015D;
                break;
            case EAST:
                angle = 270;
                translationXOffset = -0.0015D;
                break;
            case SOUTH:
                angle = 180;
                xOffset = 1;
                translationZOffset = -0.0015D;
                break;
            case WEST:
                angle = 90;
                xOffset = 1;
                zOffset = 1;
                translationXOffset = 0.0015D;
                break;
            case UP:
                xOffset = 1;
                zOffset = 1;
                translationYOffset = -0.0015D;
                break;
            case DOWN:
                yOffset = 1;
                zOffset = 1;
                translationYOffset = 0.0015D;
                break;
        }
        if (playerDistSq < 16D) { //4
            translationXOffset /= 2D;
            translationYOffset /= 2D;
            translationZOffset /= 2D;
        }
        if (playerDistSq < 36D) { //6
            translationXOffset /= 4D;
            translationYOffset /= 4D;
            translationZOffset /= 4D;
        }
        if (playerDistSq < 64D) { //8
            translationXOffset /= 2D;
            translationYOffset /= 2D;
            translationZOffset /= 2D;
        }

        if (playerDistSq > 256D) { //16
            translationXOffset *= 2D;
            translationYOffset *= 2D;
            translationZOffset *= 2D;
        }
        if (playerDistSq > 1024D) { //32
            translationXOffset *= 2D;
            translationYOffset *= 2D;
            translationZOffset *= 2D;
        }
        if (playerDistSq > 4096D) { //64
            translationXOffset *= 2D;
            translationYOffset *= 2D;
            translationZOffset *= 2D;
        }
        if (playerDistSq > 9216D) { //96
            translationXOffset *= 2D;
            translationYOffset *= 2D;
            translationZOffset *= 2D;
        }

        //GL setup
        matrix.push();
        matrix.translate(translationXOffset + xOffset, translationYOffset + yOffset, translationZOffset + zOffset);
        int j = light % 65536;
        int k = light / 65536;
        int maxBrightness = MCPaintConfig.CLIENT.maxPaintBrightness.get();
        if (k > maxBrightness)
            k = maxBrightness;
        //lightmap
//        RenderSystem.glMultiTexCoord2f(33986, 0, maxBrightness); TODO???

        if (angle != 0)
            matrix.rotate(Vector3f.YP.rotationDegrees((angle)));
        else if (facing.getAxis().isVertical()) {
            matrix.rotate(Vector3f.XP.rotationDegrees(facing == Direction.DOWN ? -90.0F : 90.0F));
            matrix.rotate(Vector3f.ZP.rotationDegrees(facing == Direction.UP ? 180.0F : 0.0F));
        }

        IPaintable paint = te.getPaintFor(facing);
        Matrix4f matrix4f = matrix.getLast().getPositionMatrix();
        //Render picture
        boolean slow = !MCPaintConfig.CLIENT.optimizePictures.get();
        if (!slow) {
            BufferManager builder = te.getBuffer(facing);
            if (builder == null) {
                int maxDistOffset = MCPaintConfig.CLIENT.maxPaintRenderDistance.get() - 8;
                if (playerDistSq < (maxDistOffset * maxDistOffset))
                    slow = true;
            } else {
                builder.get(getRes(playerDistSq)).renderPicture(matrix4f, vertexBuilder, light);
            }
        }
        if (slow) {
            RenderUtil.renderInGame(matrix4f, paint.getScaleFactor(), vertexBuilder, paint.getPictureData(), light);
        }

        matrix.pop();

        //GL cleanup
    }

    public static int getRes(double playerDistSq) {
        if (playerDistSq < 32 * 32)
            return 128;
        if (playerDistSq < 64 * 64)
            return 64;
        if (playerDistSq < 96 * 96)
            return 32;
        if (playerDistSq < 128 * 128)
            return 16;
        return 8;
    }

    @Override
    public void render(TileEntityCanvas te, float v, @Nonnull MatrixStack matrixStack, @Nonnull IRenderTypeBuffer iRenderTypeBuffer, int light, int otherlight) {
        BlockPos pos = te.getPos();
        double playerDistSq = Minecraft.getInstance().player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ());
        int maxDist = MCPaintConfig.CLIENT.maxPaintRenderDistance.get();
        IVertexBuilder builder = iRenderTypeBuffer.getBuffer(RenderTypeHandler.CANVAS);
        if (playerDistSq < (maxDist * maxDist)) {
            for (Direction facing : VALUES) {
                if (te.hasPaintFor(facing)) {
                    int lightPacked = WorldRenderer.getPackedLightmapCoords(te.getWorld(), te.getWorld().getBlockState(te.getPos()), te.getPos().offset(facing.getOpposite()));
                    renderFace(matrixStack, builder, te, facing, lightPacked, playerDistSq);
                }
            }
        } else {
            te.unbindBuffers(); //We stay in the global cache for a little longer
        }
    }
}
