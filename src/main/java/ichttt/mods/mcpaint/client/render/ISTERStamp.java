package ichttt.mods.mcpaint.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class ISTERStamp extends BlockEntityWithoutLevelRenderer implements ItemPropertyFunction {
    public static final ISTERStamp INSTANCE = new ISTERStamp();

    public static Callable<BlockEntityWithoutLevelRenderer> getInstance() {
        return () -> INSTANCE;
    }

    private ISTERStamp() {
        super(null, null);
    }

    @Override
    public void onResourceManagerReload(ResourceManager pResourceManager) {}

    @Override
    public void renderByItem(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) { //render
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            IPaintable paint = itemStack.getCapability(CapabilityPaintable.PAINTABLE, null).orElse(null);
            if (paint != null && paint.hasPaintData()) {
                matrixStack.pushPose();
                VertexConsumer vertexBuilder = buffer.getBuffer(RenderTypeHandler.CANVAS);
                RenderUtil.renderInGame(matrixStack.last().pose(), paint.getScaleFactor(), vertexBuilder, paint.getPictureData(true), combinedLightIn);
                matrixStack.popPose();
            }
        }
    }

    @Override
    public float call(ItemStack stack, @Nullable ClientLevel pLevel, @Nullable LivingEntity entity, int pSeed) {
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) && entity != null && Minecraft.getInstance().player != null && entity.getName().equals(Minecraft.getInstance().player.getName())) {
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
