package ichttt.mods.mcpaint.client.gui.button;

import com.mojang.blaze3d.vertex.PoseStack;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.gui.drawutil.EnumDrawType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TranslatableComponent;

import java.awt.*;
import java.util.Locale;

import net.minecraft.client.gui.components.Button.OnPress;

public class GuiButtonTextToggle extends Button {
    private final int color;
    public final EnumDrawType type;
    public boolean toggled = true;

    public GuiButtonTextToggle(int x, int y, int widthIn, int heightIn, EnumDrawType type, OnPress pressable) {
        super(x, y, widthIn, heightIn, new TranslatableComponent(MCPaint.MODID + ".gui." + type.toString().toLowerCase(Locale.ENGLISH)), pressable);
        this.color = Color.GREEN.getRGB();
        this.type = type;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //GuiHollowButton
            if (toggled) {
                this.vLine(stack, this.x - 1, this.y - 1, this.y + this.height, this.color);
                this.vLine(stack, this.x + this.width, this.y - 1, this.y + this.height, this.color);
                this.hLine(stack, this.x - 1, this.x + this.width, this.y - 1, this.color);
                this.hLine(stack, this.x - 1, this.x + this.width, this.y + this.height, this.color);
            }
            super.render(stack, mouseX, mouseY, partialTicks);
        }
    }
}
