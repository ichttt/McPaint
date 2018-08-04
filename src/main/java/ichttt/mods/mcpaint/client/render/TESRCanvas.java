package ichttt.mods.mcpaint.client.render;

import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

public class TESRCanvas extends TileEntitySpecialRenderer<TileEntityCanvas> {
    private final IBlockState HAS_NO_DATA = EventHandler.block.getDefaultState();
    private final IBlockState HAS_DATA = HAS_NO_DATA.withProperty(BlockCanvas.PAINTED, true);

    //We say we are fast so we can you the buffer. But we also use the "slow" mode for our picture
    @Override
    public void renderTileEntityFast(TileEntityCanvas te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
        renderBlock(x, y, z, te, buffer);

        if (te.paint.hasPaintData()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            renderPicture(x, y, z, te);
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
    }

    private static void renderPicture(double x, double y, double z, TileEntityCanvas te) {
        //GL setup
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0, 240);

        //VertexBuffer setup
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        //Render picture
        PictureRenderer.renderInGame(x, y, z, te.paint.getScaleFactor(), builder, te.paint.getPictureData());
        tessellator.draw();

        //GL cleanup
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
    }

    private void renderBlock(double x, double y, double z, TileEntityCanvas te, BufferBuilder builder) {
        //Render block
        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBlockState state = te.paint.hasPaintData() ? HAS_DATA : HAS_NO_DATA;
        BlockPos pos = te.getPos();
        builder.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
        dispatcher.renderBlock(state, pos, te.getWorld(), builder);
    }
}
