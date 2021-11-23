package ichttt.mods.mcpaint.client.gui.button;

import com.mojang.blaze3d.vertex.PoseStack;
import ichttt.mods.mcpaint.client.gui.drawutil.EnumPaintColor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.awt.*;
import java.util.function.Consumer;

public class GuiColorButton extends AbstractButton {
    private final int borderColor;
    private final Color color;
    private final Consumer<Color> consumer;

    public GuiColorButton(int colorIndex, int x, int y, int widthIn, int heightIn, int borderColor, Consumer<Color> consumer) {
        super(x, y, widthIn, heightIn, null);
        this.borderColor = borderColor;
        this.color = EnumPaintColor.VALUES[colorIndex].color;
        this.consumer = consumer;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        if (this.isHovered) {
            this.vLine(stack, this.x - 1, this.y - 1, this.y + this.height, this.borderColor);
            this.vLine(stack, this.x + this.width, this.y - 1, this.y + this.height, this.borderColor);
            this.hLine(stack, this.x - 1, this.x + this.width, this.y - 1, this.borderColor);
            this.hLine(stack, this.x - 1, this.x + this.width, this.y + this.height, this.borderColor);
        }
    }

    @Override
    public void onPress() {
        consumer.accept(color);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        this.defaultButtonNarrationText(pNarrationElementOutput);
    }
}
