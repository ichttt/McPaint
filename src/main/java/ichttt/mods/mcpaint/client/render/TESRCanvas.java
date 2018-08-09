package ichttt.mods.mcpaint.client.render;

import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

public class TESRCanvas extends TileEntitySpecialRenderer<TileEntityCanvas> {
    //We say we are fast so we can you the buffer. But we also use the "slow" mode for our picture
    @Override
    public void renderTileEntityFast(TileEntityCanvas te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
        if (MinecraftForgeClient.getRenderPass() == 0) {
            renderBlock(x, y, z, te, buffer);
        }

        if (MinecraftForgeClient.getRenderPass() == 1) {
            double playerDistSq = Minecraft.getMinecraft().player.getDistanceSq(te.getPos());
            if (playerDistSq < 9216D) { // 96 blocks render dist for paint, 128 for block. Paint is much slower so that makes sense
                int light = te.getWorld().getCombinedLight(te.getPos(), 0);
                for (EnumFacing facing : EnumFacing.VALUES) {
                    if (te.hasPaintFor(facing)) renderPicture(x, y, z, te.getPaintFor(facing), facing, light, playerDistSq);
                }
            }
        }
    }

    private static void renderPicture(double x, double y, double z, IPaintable paint, EnumFacing facing, int light, double playerDistSq) {
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
                angle = 180;
                zOffset = -1;
                translationZOffset = 0.0015D;
                break;
            case EAST:
                angle = 90;
                translationXOffset = -0.0015D;
                break;
            case SOUTH:
                xOffset = 1;
                translationZOffset = -0.0015D;
                break;
            case WEST:
                angle = 270;
                xOffset = 1;
                zOffset = -1;
                translationXOffset = 0.0015D;
                break;
            case UP:
                yOffset = -1;
                translationYOffset = -0.0015D;
                break;
            case DOWN:
                xOffset = 1;
                yOffset = -1;
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
            System.out.println("64");
            translationXOffset *= 2D;
            translationYOffset *= 2D;
            translationZOffset *= 2D;
        }

        //GL setup
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(x + translationXOffset, y + translationYOffset, z + translationZOffset);
        int j = light % 65536;
        int k = light / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);

        //VertexBuffer setup
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        //Render picture
        PictureRenderer.renderInGame(xOffset, yOffset, zOffset, paint.getScaleFactor(), builder, paint.getPictureData());

        if (angle != 0)
                GlStateManager.rotate(angle, 0, 1, 0);
        else if (facing.getAxis().isVertical()) {
            GlStateManager.rotate(facing == EnumFacing.DOWN ? -90.0F : 90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(facing == EnumFacing.UP ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
        }

        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();

        //GL cleanup
    }

    private static void renderBlock(double x, double y, double z, TileEntityCanvas te, BufferBuilder builder) {
        //Render block
        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBlockState state = te.getContainedState();
        BlockPos pos = te.getPos();
        builder.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
        dispatcher.getBlockModelRenderer().renderModel(te.getWorld(), dispatcher.getModelForState(state), state, te.getPos(), builder, true);
        builder.setTranslation(0, 0, 0);
    }
}
