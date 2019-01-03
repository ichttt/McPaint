package ichttt.mods.mcpaint.client.render;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.MCPaintConfig;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

public class TESRCanvas extends TileEntitySpecialRenderer<TileEntityCanvas> {
    private boolean renderedSlow = false;

    @Override
    public void render(TileEntityCanvas te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        int renderPass = MinecraftForgeClient.getRenderPass();
        if (destroyStage >= 0 && renderPass == 0) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder builder = tessellator.getBuffer();
            builder.noColor();
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            renderBlock(x, y, z, te, builder, destroyStage);
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            tessellator.draw();
        } else if (renderPass == 0) {
            if (!renderedSlow) {
                MCPaint.LOGGER.warn("SLOW RENDERING BLOCK!");
                MCPaint.LOGGER.warn("McPaint detected that the fast block rendering path could not be used.");
                MCPaint.LOGGER.warn("This may reduce your FPS significantly");
                MCPaint.LOGGER.warn("This message will only be shown once, even if it occurs after this");
                renderedSlow = true;
            }
            //Assume we can not render fast - fall back to slow rendering
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();
            GlStateManager.disableCull();
            if (Minecraft.isAmbientOcclusionEnabled()) {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
            }
            else {
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            renderBlock(x, y, z, te, buffer, -1);
            buffer.setTranslation(0, 0, 0);
            tessellator.draw();
            RenderHelper.enableStandardItemLighting();
        } else if (renderPass == 1) {
            renderPaint(te, x, y, z);
        }
    }

    //We say we are fast so we can you the buffer. But we also use the "slow" mode for our picture
    @Override
    public void renderTileEntityFast(TileEntityCanvas te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
        int renderPass = MinecraftForgeClient.getRenderPass();
        if (renderPass == 0) {
            renderBlock(x, y, z, te, buffer, -1);
        }
        else if (renderPass == 1) {
            renderPaint(te, x, y, z);
        }
    }

    private static void renderPaint(TileEntityCanvas te, double x, double y, double z) {
        double playerDistSq = Minecraft.getMinecraft().player.getDistanceSq(te.getPos());
        if (playerDistSq < (MCPaintConfig.CLIENT.maxPaintRenderDistance * MCPaintConfig.CLIENT.maxPaintRenderDistance)) {
            int light = te.getWorld().getCombinedLight(te.getPos(), 0);
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (te.hasPaintFor(facing)) renderFace(x, y, z, te, facing, light, playerDistSq);
            }
        } else {
            te.invalidateBuffers(); //We stay in the global cache for a little longer
        }
    }

    private static void renderFace(double x, double y, double z, TileEntityCanvas te, EnumFacing facing, int light, double playerDistSq) {
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
        GlStateManager.translate(x + translationXOffset + xOffset, y + translationYOffset + yOffset, z + translationZOffset + zOffset);
        int j = light % 65536;
        int k = light / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);

        if (angle != 0)
            GlStateManager.rotate(angle, 0, 1, 0);
        else if (facing.getAxis().isVertical()) {
            GlStateManager.rotate(facing == EnumFacing.DOWN ? -90.0F : 90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(facing == EnumFacing.UP ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
        }

        IPaintable paint = te.getPaintFor(facing);
        //Render picture
        boolean slow = !MCPaintConfig.CLIENT.optimizePictures;
        if (!slow) {
            BufferManager builder = te.getBuffer(facing);
            if (builder == null) {
                if (playerDistSq < ((MCPaintConfig.CLIENT.maxPaintRenderDistance - 8) * (MCPaintConfig.CLIENT.maxPaintRenderDistance - 8)))
                    slow = true;
            } else {
                Tessellator.getInstance().vboUploader.draw(builder.get(getRes(playerDistSq)));
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

    private void renderBlock(double x, double y, double z, TileEntityCanvas te, BufferBuilder builder, int destroyStage) {
        //Render block
        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBlockState state = te.getContainedState();
        if (state == null)
            state = EventHandler.CANVAS_WOOD.getDefaultState();
        BlockPos pos = te.getPos();
        builder.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
        if (destroyStage >= 0)
            dispatcher.renderBlockDamage(state, pos, Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage), te.getWorld());
        else
            dispatcher.getBlockModelRenderer().renderModel(te.getWorld(), dispatcher.getModelForState(state), state, te.getPos(), builder, true);
        builder.setTranslation(0, 0, 0);
    }
}
