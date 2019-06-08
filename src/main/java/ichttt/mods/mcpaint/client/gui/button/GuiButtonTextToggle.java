package ichttt.mods.mcpaint.client.gui.button;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.gui.drawutil.EnumDrawType;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;

import java.awt.*;
import java.util.Locale;

public class GuiButtonTextToggle extends Button {
    private final int color;
    public final EnumDrawType type;
    public boolean toggled = true;

    public GuiButtonTextToggle(int x, int y, int widthIn, int heightIn, EnumDrawType type, IPressable pressable) {
        super(x, y, widthIn, heightIn, I18n.format(MCPaint.MODID + ".gui." + type.toString().toLowerCase(Locale.ENGLISH)), pressable);
        this.color = Color.GREEN.getRGB();
        this.type = type;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //GuiHollowButton
            if (toggled) {
                this.vLine(this.x - 1, this.y - 1, this.y + this.height, this.color);
                this.vLine(this.x + this.width, this.y - 1, this.y + this.height, this.color);
                this.hLine(this.x - 1, this.x + this.width, this.y - 1, this.color);
                this.hLine(this.x - 1, this.x + this.width, this.y + this.height, this.color);
            }
            super.render(mouseX, mouseY, partialTicks);
        }
    }
}
