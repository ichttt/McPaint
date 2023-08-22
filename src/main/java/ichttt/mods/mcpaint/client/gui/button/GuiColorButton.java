package ichttt.mods.mcpaint.client.gui.button;

import ichttt.mods.mcpaint.client.gui.drawutil.EnumPaintColor;
import net.minecraft.client.gui.GuiGraphics;
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        int x = this.getX();
        int y = this.getY();
        this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
        if (this.isHovered) {
            guiGraphics.vLine(x - 1, y - 1, y + this.height, this.borderColor);
            guiGraphics.vLine(x + this.width, y - 1, y + this.height, this.borderColor);
            guiGraphics.hLine(x - 1, x + this.width, y - 1, this.borderColor);
            guiGraphics.hLine(x - 1, x + this.width, y + this.height, this.borderColor);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        this.defaultButtonNarrationText(pNarrationElementOutput);
    }

    @Override
    public void onPress() {
        consumer.accept(color);
    }
}
