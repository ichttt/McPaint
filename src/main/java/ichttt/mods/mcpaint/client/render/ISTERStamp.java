package ichttt.mods.mcpaint.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class ISTERStamp extends ItemStackTileEntityRenderer implements IItemPropertyGetter {
    public static final ISTERStamp INSTANCE = new ISTERStamp();

    public static Callable<ItemStackTileEntityRenderer> getInstance() {
        return () -> INSTANCE;
    }

    private ISTERStamp() {}

    @Override
    public void render(ItemStack itemStack, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        if (InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            IPaintable paint = itemStack.getCapability(CapabilityPaintable.PAINTABLE, null).orElse(null);
            if (paint != null && paint.hasPaintData()) {
                matrixStack.push();
                IVertexBuilder vertexBuilder = buffer.getBuffer(RenderTypeHandler.CANVAS);
                RenderUtil.renderInGame(matrixStack.getLast().getPositionMatrix(), paint.getScaleFactor(), vertexBuilder, paint.getPictureData(), combinedLightIn);
                matrixStack.pop();
            }
        }
    }

    @Override
    public float call(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) {
        if (InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(),GLFW.GLFW_KEY_LEFT_SHIFT) && entity != null && Minecraft.getInstance().player != null && entity.getName().equals(Minecraft.getInstance().player.getName())) {
            IPaintable paint = stack.getCapability(CapabilityPaintable.PAINTABLE, null).orElse(null);
            if (paint == null) {
                MCPaint.LOGGER.warn(stack.getItem() + " is missing paint!");
                return 0F;
            }
            if (paint.hasPaintData()) {
                return 1F;
            }
        }
        return 0F;
    }
}
