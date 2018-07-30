package ichttt.mods.mcpaint.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

import javax.annotation.Nonnull;

public class GuiColorButton extends GuiButton {
    private final int color;

    public GuiColorButton(int buttonId, int x, int y, int width, int height, int color) {
        //noinspection ConstantConditions - we override drawButton
        super(buttonId, x, y, width, height, null);
        this.color = color;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, this.color);
        }
    }
}
