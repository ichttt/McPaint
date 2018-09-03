package ichttt.mods.mcpaint.client.render;

import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TEISRStamp extends TileEntityItemStackRenderer implements IItemPropertyGetter {
    public static final TEISRStamp INSTANCE = new TEISRStamp();

    private TEISRStamp() {}

    @Override
    public void renderByItem(ItemStack itemStack, float partialTicks) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            IPaintable paint = itemStack.getCapability(CapabilityPaintable.PAINTABLE, null);
            if (paint != null && paint.hasPaintData()) {
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder builder = tessellator.getBuffer();
                builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                PictureRenderer.renderInGame(paint.getScaleFactor(), builder, paint.getPictureData());
                PictureRenderer.setWorldGLState();
                tessellator.draw();
                PictureRenderer.resetWorldGLState();
            }
        }
    }

    @Override
    public float apply(@Nonnull ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entity) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && entity != null && Minecraft.getMinecraft().player != null && entity.getName().equals(Minecraft.getMinecraft().player.getName())) {
            IPaintable paint = stack.getCapability(CapabilityPaintable.PAINTABLE, null);
            if (paint != null && paint.hasPaintData()) {
                return 1F;
            }
        }
        return 0F;
    }
}
