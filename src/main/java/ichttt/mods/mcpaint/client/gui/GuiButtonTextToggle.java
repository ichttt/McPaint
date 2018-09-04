package ichttt.mods.mcpaint.client.gui;

import ichttt.mods.mcpaint.client.gui.drawutil.EnumDrawType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import javax.annotation.Nonnull;
import java.awt.*;

public class GuiButtonTextToggle extends GuiButton {
    private final int color;
    public final EnumDrawType type;
    public boolean toggled = true;

    public GuiButtonTextToggle(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, EnumDrawType type) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.color = Color.GREEN.getRGB();
        this.type = type;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //GuiHollowButton
            if (toggled) {
                this.drawVerticalLine(this.x - 1, this.y - 1, this.y + this.height, this.color);
                this.drawVerticalLine(this.x + this.width, this.y - 1, this.y + this.height, this.color);
                this.drawHorizontalLine(this.x - 1, this.x + this.width, this.y - 1, this.color);
                this.drawHorizontalLine(this.x - 1, this.x + this.width, this.y + this.height, this.color);
            }
            super.drawButton(mc, mouseX, mouseY, partialTicks);
        }
    }
}
