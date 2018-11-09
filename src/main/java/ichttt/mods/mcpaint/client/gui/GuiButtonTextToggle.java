package ichttt.mods.mcpaint.client.gui;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.gui.drawutil.EnumDrawType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

import java.awt.*;
import java.util.Locale;

public class GuiButtonTextToggle extends GuiButton {
    private final int color;
    public final EnumDrawType type;
    public boolean toggled = true;

    public GuiButtonTextToggle(int buttonId, int x, int y, int widthIn, int heightIn, EnumDrawType type) {
        super(buttonId, x, y, widthIn, heightIn, I18n.format(MCPaint.MODID + ".gui." + type.toString().toLowerCase(Locale.ENGLISH)));
        this.color = Color.GREEN.getRGB();
        this.type = type;
    }



    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //GuiHollowButton
            if (toggled) {
                this.drawVerticalLine(this.x - 1, this.y - 1, this.y + this.height, this.color);
                this.drawVerticalLine(this.x + this.width, this.y - 1, this.y + this.height, this.color);
                this.drawHorizontalLine(this.x - 1, this.x + this.width, this.y - 1, this.color);
                this.drawHorizontalLine(this.x - 1, this.x + this.width, this.y + this.height, this.color);
            }
            super.render(mouseX, mouseY, partialTicks);
        }
    }
}
