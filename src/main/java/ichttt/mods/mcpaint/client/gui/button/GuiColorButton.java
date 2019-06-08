package ichttt.mods.mcpaint.client.gui.button;

import ichttt.mods.mcpaint.client.gui.drawutil.EnumPaintColor;
import net.minecraft.client.gui.widget.button.Button;

import java.awt.*;
import java.util.function.Consumer;

public class GuiColorButton extends Button {
    private final int borderColor;
    private final Consumer<Color> consumer;

    public GuiColorButton(int buttonId, int x, int y, int widthIn, int heightIn, int borderColor, Consumer<Color> consumer) {
        super(buttonId, x, y, widthIn, heightIn, null);
        this.borderColor = borderColor;
        this.consumer = consumer;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        if (this.hovered) {
            this.drawVerticalLine(this.x - 1, this.y - 1, this.y + this.height, this.borderColor);
            this.drawVerticalLine(this.x + this.width, this.y - 1, this.y + this.height, this.borderColor);
            this.drawHorizontalLine(this.x - 1, this.x + this.width, this.y - 1, this.borderColor);
            this.drawHorizontalLine(this.x - 1, this.x + this.width, this.y + this.height, this.borderColor);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        consumer.accept(EnumPaintColor.VALUES[this.id].color);
    }
}
