package ichttt.mods.mcpaint.client.gui;

import ichttt.mods.mcpaint.MCPaint;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;

public class GuiSetupCanvas extends GuiScreen {
    private static final ResourceLocation BACKGROUND = GuiDraw.BACKGROUND;
    private static final int yOffset = 166;
    private static final int xSize = 106;
    private static final int ySize = 79;
    private static final int MAX_MULTIPLIER = 4; // 8 will cause a too large CPacket

    private final BlockPos pos;
    private final EnumFacing facing;
    private final IBlockState state;
    private final int baseX;
    private final int baseY;

    private GuiButton moreSize;
    private GuiButton lessSize;
    private int currentMulti;
    private int guiLeft;
    private int guiTop;

    public GuiSetupCanvas(BlockPos pos, EnumFacing facing, IBlockState state, int baseX, int baseY) {
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
        this.lessSize = new GuiButton(1, this.guiLeft + 5, this.guiTop + 26, 20, 20, "<");
        this.moreSize = new GuiButton(2, this.guiLeft + 83, this.guiTop + 26, 20, 20, ">");
        addButton(new GuiButton(0, this.guiLeft + 5, this.guiTop + 56, xSize - 8, 20, I18n.format("gui.done")));
        addButton(this.lessSize);
        addButton(this.moreSize);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.buttonList.clear();
        this.initGui();
        mc.getTextureManager().bindTexture(BACKGROUND);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, yOffset, xSize, ySize);
        this.drawCenteredString(mc.fontRenderer, "Resolution:", this.guiLeft + (xSize / 2) + 1, this.guiTop + 8, Color.WHITE.getRGB());
        this.drawCenteredString(mc.fontRenderer, this.baseX * this.currentMulti + "x" + this.baseY * this.currentMulti, this.guiLeft + (xSize / 2) + 1, this.guiTop + 32, Color.WHITE.getRGB());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(null);
                mc.displayGuiScreen(new GuiDraw((byte) (8 / this.currentMulti), pos, facing, state));
                break;
            case 1:
                this.currentMulti /= 2;
                handleSizeChanged();
                break;
            case 2:
                this.currentMulti *= 2;
                handleSizeChanged();
                break;
            default:
                MCPaint.LOGGER.warn("Unknown button id: " + button.id + " with button " + button);
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
