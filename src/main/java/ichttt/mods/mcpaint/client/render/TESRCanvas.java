package ichttt.mods.mcpaint.client.render;

import ichttt.mods.mcpaint.MCPaintConfig;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class TESRCanvas extends TileEntityRenderer<TileEntityCanvas> {
    private static final WorldVertexBufferUploader UPLOADER = new WorldVertexBufferUploader();

    @Override
    public void render(TileEntityCanvas te, double x, double y, double z, float partialTicks, int destroyStage) {
        /*if (destroyStage >= 0 && MinecraftForgeClient.getRenderPass() == 0)*/ {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder builder = tessellator.getBuffer();
            //builder.noColor(); TODO uncomment when fast tesr
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            renderBlock(x, y, z, te, builder, destroyStage);
            Minecraft.getInstance().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            tessellator.draw();
        }
        //TODO remove when fast tesr. No render pass yet - always render
        double playerDistSq = Minecraft.getInstance().player.getDistanceSq(te.getPos());
        if (playerDistSq < (MCPaintConfig.CLIENT.maxPaintRenderDistance * MCPaintConfig.CLIENT.maxPaintRenderDistance)) {
            int light = te.getWorld().getCombinedLight(te.getPos(), 0);
            for (EnumFacing facing : EnumFacing.values()) {
                if (te.hasPaintFor(facing)) renderPicture(x, y, z, te, facing, light, playerDistSq);
            }
        } else {
            te.invalidateBuffers(); //We stay in the global cache for a little longer
        }
    }

    //We say we are fast so we can you the buffer. But we also use the "slow" mode for our picture
//    @Override
    public void renderTileEntityFast(TileEntityCanvas te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
        int renderPass = MinecraftForgeClient.getRenderPass();
        if (renderPass == 0) {
            renderBlock(x, y, z, te, buffer, -1);
        }
        else if (renderPass == 1) {
            double playerDistSq = Minecraft.getInstance().player.getDistanceSq(te.getPos());
            if (playerDistSq < (MCPaintConfig.CLIENT.maxPaintRenderDistance * MCPaintConfig.CLIENT.maxPaintRenderDistance)) {
                int light = te.getWorld().getCombinedLight(te.getPos(), 0);
                for (EnumFacing facing : EnumFacing.values()) {
                    if (te.hasPaintFor(facing)) renderPicture(x, y, z, te, facing, light, playerDistSq);
                }
            } else {
                te.invalidateBuffers(); //We stay in the global cache for a little longer
            }
        }
    }

    private static void renderPicture(double x, double y, double z, TileEntityCanvas te, EnumFacing facing, int light, double playerDistSq) {
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

        //GL setup
        PictureRenderer.setWorldGLState();
        GlStateManager.translated(x + translationXOffset + xOffset, y + translationYOffset + yOffset, z + translationZOffset + zOffset);
        int j = light % 65536;
        int k = light / 65536;
//        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k); TODO light

        if (angle != 0)
            GlStateManager.rotatef(angle, 0, 1, 0);
        else if (facing.getAxis().isVertical()) {
            GlStateManager.rotatef(facing == EnumFacing.DOWN ? -90.0F : 90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(facing == EnumFacing.UP ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
        }

        IPaintable paint = te.getPaintFor(facing);
        //Render picture
        boolean slow = !MCPaintConfig.CLIENT.optimizePictures;
        if (!slow) {
            BufferBuilder builder = te.getBuffer(facing);
            if (builder == null) {
                if (playerDistSq < (88D * 88D))
                    slow = true;
            } else {
                UPLOADER.draw(builder);
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

    private void renderBlock(double x, double y, double z, TileEntityCanvas te, BufferBuilder builder, int destroyStage) {
        //Render block
        BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        IBlockState state = te.getContainedState();
        if (state == null)
            state = EventHandler.CANVAS_WOOD.getDefaultState();
        BlockPos pos = te.getPos();
        builder.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
        if (destroyStage >= 0)
            dispatcher.renderBlockDamage(state, pos, Minecraft.getInstance().getTextureMap().getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage), te.getWorld());
        else
            dispatcher.getBlockModelRenderer().renderModel(te.getWorld(), dispatcher.getModelForState(state), state, te.getPos(), builder, true, new Random(), state.getPositionRandom(te.getPos()));
        builder.setTranslation(0, 0, 0);
    }
}
