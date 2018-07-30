package ichttt.mods.mcpaint.client.render;

import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.FastTESR;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class TESRCanvas extends TileEntitySpecialRenderer<TileEntityCanvas> {

    @Override
    public void render(TileEntityCanvas te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.hasData()) return;
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        //TODO Color fucked up, sync fucked up, brightness fucked up

        BlockPos pos = te.getPos();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        PictureRenderer.renderInGame(x, y, z + 1.0001F, te.getScaleFactor(), builder, te.getPicture());
        tessellator.draw();
        GlStateManager.enableTexture2D();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBlockState state = EventHandler.block.getDefaultState();
        IBakedModel model = dispatcher.getModelForState(state);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        builder.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
        dispatcher.renderBlock(state, pos, te.getWorld(), builder);
        builder.setTranslation(0, 0 ,0);
//        System.out.println("RENDERING");
        tessellator.draw();
        GlStateManager.popMatrix();
    }

//
//
//    @Override
//    public void renderTileEntityFast(TileEntityCanvas te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
//        BlockPos pos = te.getPos();
//        buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
//        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(EventHandler.block.getDefaultState());
//        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(Minecraft.getMinecraft().world, model, EventHandler.block.getDefaultState(), te.getPos(), buffer, false);
//    }
}
