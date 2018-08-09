package ichttt.mods.mcpaint.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import javax.annotation.Nonnull;

public class GuiHollowButton extends GuiButton {
    private final int color;

    public GuiHollowButton(int buttonId, int x, int y, int widthIn, int heightIn, int color) {
        //noinspection ConstantConditions
        super(buttonId, x, y, widthIn, heightIn, null);
        this.color = color;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        if (this.hovered) {
            this.drawVerticalLine(this.x - 1, this.y - 1, this.y + this.height, this.color);
            this.drawVerticalLine(this.x + this.width, this.y - 1, this.y + this.height, this.color);
            this.drawHorizontalLine(this.x - 1, this.x + this.width, this.y - 1, this.color);
            this.drawHorizontalLine(this.x - 1, this.x + this.width, this.y + this.height, this.color);
        }
    }
}
