package ichttt.mods.mcpaint.client.gui.button;

import com.mojang.blaze3d.vertex.PoseStack;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.gui.drawutil.EnumDrawType;
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
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //GuiHollowButton
            if (toggled) {
                int x = this.getX();
                int y = this.getY();
                this.vLine(stack, x - 1, y - 1, y + this.height, this.color);
                this.vLine(stack, x + this.width, y - 1, y + this.height, this.color);
                this.hLine(stack, x - 1, x + this.width, y - 1, this.color);
                this.hLine(stack, x - 1, x + this.width, y + this.height, this.color);
            }
            super.render(stack, mouseX, mouseY, partialTicks);
        }
    }
}
