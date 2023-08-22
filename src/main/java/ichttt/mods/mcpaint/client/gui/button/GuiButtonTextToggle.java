package ichttt.mods.mcpaint.client.gui.button;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.gui.drawutil.EnumDrawType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.Locale;

public class GuiButtonTextToggle extends Button {
    private final int color;
    public final EnumDrawType type;
    public boolean toggled = true;

    public GuiButtonTextToggle(int x, int y, int widthIn, int heightIn, EnumDrawType type, OnPress pressable) {
        super(x, y, widthIn, heightIn, Component.translatable(MCPaint.MODID + ".gui." + type.toString().toLowerCase(Locale.ENGLISH)), pressable, DEFAULT_NARRATION);
        this.color = Color.GREEN.getRGB();
        this.type = type;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //GuiHollowButton
            if (toggled) {
                int x = this.getX();
                int y = this.getY();
                guiGraphics.vLine(x - 1, y - 1, y + this.height, this.color);
                guiGraphics.vLine(x + this.width, y - 1, y + this.height, this.color);
                guiGraphics.hLine(x - 1, x + this.width, y - 1, this.color);
                guiGraphics.hLine(x - 1, x + this.width, y + this.height, this.color);
            }
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }
}
