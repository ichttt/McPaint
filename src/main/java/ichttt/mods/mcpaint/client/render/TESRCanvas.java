package ichttt.mods.mcpaint.client.render;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import ichttt.mods.mcpaint.MCPaintConfig;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

public class TESRCanvas extends TileEntityRenderer<TileEntityCanvas> {
    private static final Direction[] VALUES = Direction.values();
    private static final WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();

    @Override
    public void render(TileEntityCanvas te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (destroyStage != -1) return;
        BlockPos pos = te.getPos();
        double playerDistSq = Minecraft.getInstance().player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ());
        int maxDist = MCPaintConfig.CLIENT.maxPaintRenderDistance.get();
        if (playerDistSq < (maxDist * maxDist)) {
            int light = Objects.requireNonNull(te.getWorld()).getCombinedLight(te.getPos(), 0);
            for (Direction facing : VALUES) {
                if (te.hasPaintFor(facing)) renderFace(x, y, z, te, facing, light, playerDistSq);
            }
        } else {
            te.unbindBuffers(); //We stay in the global cache for a little longer
        }
    }

    private static void renderFace(double x, double y, double z, TileEntityCanvas te, Direction facing, int light, double playerDistSq) {
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
        PictureRenderer.setWorldGLState();
        GlStateManager.translated(x + translationXOffset + xOffset, y + translationYOffset + yOffset, z + translationZOffset + zOffset);
        int j = light % 65536;
        int k = light / 65536;
        int maxBrightness = MCPaintConfig.CLIENT.maxPaintBrightness.get();
        if (k > maxBrightness)
            k = maxBrightness;
        //lightmap
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, j, k);

        if (angle != 0)
            GlStateManager.rotatef(angle, 0, 1, 0);
        else if (facing.getAxis().isVertical()) {
            GlStateManager.rotatef(facing == Direction.DOWN ? -90.0F : 90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(facing == Direction.UP ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
        }

        IPaintable paint = te.getPaintFor(facing);
        //Render picture
        boolean slow = !MCPaintConfig.CLIENT.optimizePictures.get();
        if (!slow) {
            BufferManager builder = te.getBuffer(facing);
            if (builder == null) {
                int maxDistOffset = MCPaintConfig.CLIENT.maxPaintRenderDistance.get() - 8;
                if (playerDistSq < (maxDistOffset * maxDistOffset))
                    slow = true;
            } else {
                vboUploader.draw(builder.get(getRes(playerDistSq)));
            }
        }
        if (slow) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder builder = tessellator.getBuffer();
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            PictureRenderer.renderInGame(paint.getScaleFactor(), builder, paint.getPictureData());
            tessellator.draw();
        }

        PictureRenderer.resetWorldGLState();

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
}
