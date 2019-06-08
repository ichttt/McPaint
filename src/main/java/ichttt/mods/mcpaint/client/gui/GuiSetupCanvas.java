package ichttt.mods.mcpaint.client.gui;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.networking.MessageDrawAbort;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class GuiSetupCanvas extends Screen {
    private static final ResourceLocation BACKGROUND = GuiDraw.BACKGROUND;
    private static final int yOffset = 166;
    private static final int xSize = 106;
    private static final int ySize = 79;
    private static final int MAX_MULTIPLIER = 16;

    private final BlockPos pos;
    private final Direction facing;
    private final BlockState state;
    private final int baseX;
    private final int baseY;

    private boolean handled = false;
    private Button moreSize;
    private Button lessSize;
    private int currentMulti;
    private int guiLeft;
    private int guiTop;

    public GuiSetupCanvas(BlockPos pos, Direction facing, BlockState state, int baseX, int baseY) {
        this.pos = pos;
        this.facing = facing;
        this.state = state;
        this.baseX = baseX;
        this.baseY = baseY;
        this.currentMulti = 2;
    }

    @Override
    public void initGui() {
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;
        this.lessSize = new Button(1, this.guiLeft + 5, this.guiTop + 26, 20, 20, "<") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                GuiSetupCanvas.this.currentMulti /= 2;
                handleSizeChanged();
                super.onClick(mouseX, mouseY);
            }
        };
        this.moreSize = new Button(2, this.guiLeft + 83, this.guiTop + 26, 20, 20, ">") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                GuiSetupCanvas.this.currentMulti *= 2;
                handleSizeChanged();
                super.onClick(mouseX, mouseY);
            }
        };
        addButton(new Button(0, this.guiLeft + 5, this.guiTop + 56, xSize - 8, 20, I18n.format("gui.done")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                handled = true;
                mc.displayGuiScreen(null);
                mc.displayGuiScreen(new GuiDraw((byte) (16 / GuiSetupCanvas.this.currentMulti), pos, facing, state));
                super.onClick(mouseX, mouseY);
            }
        });
        addButton(this.lessSize);
        addButton(this.moreSize);
        handleSizeChanged();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(BACKGROUND);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, yOffset, xSize, ySize);
        this.drawCenteredString(mc.fontRenderer, "Resolution:", this.guiLeft + (xSize / 2) + 1, this.guiTop + 8, Color.WHITE.getRGB());
        this.drawCenteredString(mc.fontRenderer, this.baseX * this.currentMulti + "x" + this.baseY * this.currentMulti, this.guiLeft + (xSize / 2) + 1, this.guiTop + 32, Color.WHITE.getRGB());
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        if (!handled) {
            MCPaint.NETWORKING.sendToServer(new MessageDrawAbort(pos));
        }
    }

    private void handleSizeChanged() {
        if (this.currentMulti >= MAX_MULTIPLIER) {
            this.currentMulti = MAX_MULTIPLIER;
            this.moreSize.enabled = false;
        } else {
            this.moreSize.enabled = true;
        }

        if (this.currentMulti <= 1) {
            this.currentMulti = 1;
            this.lessSize.enabled = false;
        } else {
            this.lessSize.enabled = true;
        }
    }
}
