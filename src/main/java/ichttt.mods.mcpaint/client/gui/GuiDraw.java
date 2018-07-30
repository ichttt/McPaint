package ichttt.mods.mcpaint.client.gui;

import ichttt.mods.mcpaint.MCPaint;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

public class GuiDraw extends GuiScreen {
    private static final int PICTURE_START_LEFT = 6;
    private static final int PICTURE_START_TOP = 9;
    private static final ResourceLocation BACKGROUND = new ResourceLocation(MCPaint.MODID, "textures/gui/setup.png");
    public static final int xSize = 176;
    public static final int ySize = 166;
    private GuiButton done;
    private GuiHollowButton black;
    private GuiHollowButton white;
    private GuiHollowButton gray;
    private GuiHollowButton red;
    private GuiHollowButton orange;
    private GuiHollowButton yellow;
    private GuiHollowButton lime;
    private GuiHollowButton green;
    private GuiHollowButton lightBlue;
    private GuiHollowButton darkBlue;
    private GuiHollowButton purple;
    private GuiHollowButton pink;
    private EnumPaintColor color = null;
    private final int scaleFactor;
    private final int[][] PICTURE;

    private int guiLeft;
    private int guiTop;
    private boolean clickStartedInPicture = false;

    public GuiDraw(int scaleFactor) {
        this.scaleFactor = scaleFactor;
        this.PICTURE = new int[128 / scaleFactor][128 / scaleFactor];
        for (int[] tileArray : PICTURE)
            Arrays.fill(tileArray, Color.WHITE.getRGB());
    }

    @Override
    public void initGui() {
//        for (int i = 0; i < 16; i++) {
//            PICTURE[0][i] = Color.YELLOW.getRGB();
//            PICTURE[15][i] = Color.YELLOW.getRGB();
//        }
//        this.buttonList.clear();
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;
        done = new GuiButton(-1, this.guiLeft + (xSize / 2) - (200 / 2), this.guiTop + ySize + 20, 200, 20, I18n.format("gui.done"));
        black = new GuiHollowButton(0, this.guiLeft + 137, this.guiTop + 9, 16, 16, Color.BLUE.getRGB());
        white = new GuiHollowButton(1, this.guiLeft + 137 + 18, this.guiTop + 9, 16, 16, Color.BLUE.getRGB());
        gray = new GuiHollowButton(2, this.guiLeft + 137, this.guiTop + 9 + 18, 16, 16, Color.BLUE.getRGB());
        red = new GuiHollowButton(3, this.guiLeft + 137 + 18, this.guiTop + 9 + 18, 16, 16, Color.BLUE.getRGB());
        orange = new GuiHollowButton(4, this.guiLeft + 137, this.guiTop + 9 + 36, 16, 16, Color.BLUE.getRGB());
        yellow = new GuiHollowButton(5, this.guiLeft + 137 + 18, this.guiTop + 9 + 36, 16, 16, Color.BLUE.getRGB());

        lime = new GuiHollowButton(6, this.guiLeft + 137, this.guiTop + 9 + 54, 16, 16, Color.BLACK.getRGB());
        green = new GuiHollowButton(7, this.guiLeft + 137 + 18, this.guiTop + 9 + 54, 16, 16, Color.BLACK.getRGB());
        lightBlue = new GuiHollowButton(8, this.guiLeft + 137, this.guiTop + 9 + 72, 16, 16, Color.BLACK.getRGB());
        darkBlue = new GuiHollowButton(9, this.guiLeft + 137 + 18, this.guiTop + 9 + 72, 16, 16, Color.BLACK.getRGB());
        purple = new GuiHollowButton(10, this.guiLeft + 137, this.guiTop + 9 + 90, 16, 16, Color.BLACK.getRGB());
        pink = new GuiHollowButton(11, this.guiLeft + 137 + 18, this.guiTop + 9 + 90, 16, 16, Color.BLACK.getRGB());
        this.buttonList.add(done);
        this.buttonList.add(black);
        this.buttonList.add(white);
        this.buttonList.add(gray);
        this.buttonList.add(red);
        this.buttonList.add(orange);
        this.buttonList.add(yellow);
        this.buttonList.add(lime);
        this.buttonList.add(green);
        this.buttonList.add(lightBlue);
        this.buttonList.add(darkBlue);
        this.buttonList.add(purple);
        this.buttonList.add(pink);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//        this.initGui();
        mc.getTextureManager().bindTexture(BACKGROUND);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (color != null) {
            drawRect(this.guiLeft + 138, this.guiTop + 125, this.guiLeft + 138 + 32, this.guiTop + 125 + 32, color.RGB);
        }
        final boolean drawNew = true;
        Tessellator tessellator = null;
        BufferBuilder builder = null;
        if (drawNew) {
            tessellator = Tessellator.getInstance();
            builder = tessellator.getBuffer();
            builder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        }
        for (int x = 0; x < PICTURE.length; x++) {
            int[] yPos = PICTURE[x];
            for (int y = 0; y < yPos.length; y++) {
                int left = this.guiLeft + 6 + (x * this.scaleFactor);
                int top = this.guiTop + 9 + (y * this.scaleFactor);
                int right = left + this.scaleFactor;
                int bottom = top + this.scaleFactor;
                int color = PICTURE[x][y];
                if (drawNew) {
                    float f3 = (float) (color >> 24 & 255) / 255.0F;
                    float f = (float) (color >> 16 & 255) / 255.0F;
                    float f1 = (float) (color >> 8 & 255) / 255.0F;
                    float f2 = (float) (color & 255) / 255.0F;
                    builder.pos((double) left, (double) bottom, 0.0D).color(f, f1, f2, f3).endVertex();
                    builder.pos((double) right, (double) bottom, 0.0D).color(f, f1, f2, f3).endVertex();
                    builder.pos((double) right, (double) top, 0.0D).color(f, f1, f2, f3).endVertex();
                    builder.pos((double) left, (double) top, 0.0D).color(f, f1, f2, f3).endVertex();
                } else
                drawRect(left, top, right, bottom, color);
            }
        }
        if (drawNew) {
            GlStateManager.disableTexture2D();
            tessellator.draw();
            GlStateManager.enableTexture2D();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (handleMouse(mouseX, mouseY, mouseButton)) {
            this.clickStartedInPicture = true;
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private boolean handleMouse(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return false;
        int offsetMouseX = mouseX - this.guiLeft - PICTURE_START_LEFT;
        int offsetMouseY = mouseY - this.guiTop - PICTURE_START_TOP;
        if (offsetMouseX > 0 && offsetMouseX < (PICTURE.length * this.scaleFactor) && offsetMouseY > 0 && offsetMouseY < (PICTURE.length * this.scaleFactor)) {
//            System.out.println("Click in Picture");
            int pixelPosX = offsetMouseX / this.scaleFactor;
            int pixelPosY = offsetMouseY / this.scaleFactor;
            if (pixelPosX < PICTURE.length && pixelPosY < PICTURE.length && this.color != null) {
                PICTURE[pixelPosX][pixelPosY] = this.color.RGB;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.clickStartedInPicture && handleMouse(mouseX, mouseY, clickedMouseButton))
            return;
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.clickStartedInPicture = false;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= 0)
            color = EnumPaintColor.VALUES[button.id];
    }
}
