package ichttt.mods.mcpaint.client.gui;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.EnumPaintColor;
import ichttt.mods.mcpaint.client.render.PictureRenderer;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.networking.MessageDrawComplete;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GuiDraw extends GuiScreen {
    private static final int PICTURE_START_LEFT = 14;
    private static final int PICTURE_START_TOP = 17;
    private static final ResourceLocation BACKGROUND = new ResourceLocation(MCPaint.MODID, "textures/gui/setup.png");
    public static final int xSize = 176;
    public static final int ySize = 166;
    public static final int toolXSize = 80;
    public static final int toolYSize = 95;
    public static final int sizeXSize = toolXSize;
    public static final int sizeYSize = 34;

    private final byte scaleFactor;
    private final int[][] picture;
    private final BlockPos pos;

    private EnumPaintColor color = null;
    private int guiLeft;
    private int guiTop;
    private boolean clickStartedInPicture = false;
    private final List<GuiButtonTextToggle> textToggleList = new ArrayList<>();
    private EnumDrawType activeDrawType;
    private int toolSize = 1;
    private GuiButton lessSize, moreSize;
    private boolean hasSizeWindow;
    private boolean synced = false;

    public GuiDraw(TileEntityCanvas canvas) {
        Objects.requireNonNull(canvas);
        if (!canvas.hasData())
            throw new IllegalArgumentException("No data in canvas");
        this.pos = canvas.getPos();
        this.scaleFactor = canvas.getScaleFactor();
        this.picture = canvas.getPicture();
        this.synced = true;
    }

    public GuiDraw(byte scaleFactor, BlockPos pos) {
        this.pos = Objects.requireNonNull(pos);
        this.scaleFactor = scaleFactor;
        this.picture = new int[TileEntityCanvas.CANVAS_PIXEL_COUNT / scaleFactor][TileEntityCanvas.CANVAS_PIXEL_COUNT / scaleFactor];
        for (int[] tileArray : picture)
            Arrays.fill(tileArray, Color.WHITE.getRGB());
    }

    @Override
    public void initGui() {
        this.hasSizeWindow = false;
        this.textToggleList.clear();
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;
        GuiButton fill = new GuiButtonTextToggle(-5, this.guiLeft + xSize + 2 + 39, this.guiTop + 5, 36, 20, "Fill", EnumDrawType.FILL);
        GuiButton pencil = new GuiButtonTextToggle(-4, this.guiLeft + xSize + 3, this.guiTop + 5, 36, 20, "Pencil", EnumDrawType.PENCIL);
        this.moreSize = new GuiButton(-3, this.guiLeft + xSize + 3 + 55, this.guiTop + toolYSize + 5, 20, 20, ">");
        this.lessSize = new GuiButton(-2, this.guiLeft + xSize + 3, this.guiTop + toolYSize + 5, 20, 20, "<");
        GuiButton done = new GuiButton(-1, this.guiLeft + (xSize / 2) - (200 / 2), this.guiTop + ySize + 20, 200, 20, I18n.format("gui.done"));

        GuiHollowButton black = new GuiHollowButton(0, this.guiLeft + 137, this.guiTop + 9, 16, 16, Color.BLUE.getRGB());
        GuiHollowButton white = new GuiHollowButton(1, this.guiLeft + 137 + 18, this.guiTop + 9, 16, 16, Color.BLUE.getRGB());
        GuiHollowButton gray = new GuiHollowButton(2, this.guiLeft + 137, this.guiTop + 9 + 18, 16, 16, Color.BLUE.getRGB());
        GuiHollowButton red = new GuiHollowButton(3, this.guiLeft + 137 + 18, this.guiTop + 9 + 18, 16, 16, Color.BLUE.getRGB());
        GuiHollowButton orange = new GuiHollowButton(4, this.guiLeft + 137, this.guiTop + 9 + 36, 16, 16, Color.BLUE.getRGB());
        GuiHollowButton yellow = new GuiHollowButton(5, this.guiLeft + 137 + 18, this.guiTop + 9 + 36, 16, 16, Color.BLUE.getRGB());

        GuiHollowButton lime = new GuiHollowButton(6, this.guiLeft + 137, this.guiTop + 9 + 54, 16, 16, Color.BLACK.getRGB());
        GuiHollowButton green = new GuiHollowButton(7, this.guiLeft + 137 + 18, this.guiTop + 9 + 54, 16, 16, Color.BLACK.getRGB());
        GuiHollowButton lightBlue = new GuiHollowButton(8, this.guiLeft + 137, this.guiTop + 9 + 72, 16, 16, Color.BLACK.getRGB());
        GuiHollowButton darkBlue = new GuiHollowButton(9, this.guiLeft + 137 + 18, this.guiTop + 9 + 72, 16, 16, Color.BLACK.getRGB());
        GuiHollowButton purple = new GuiHollowButton(10, this.guiLeft + 137, this.guiTop + 9 + 90, 16, 16, Color.BLACK.getRGB());
        GuiHollowButton pink = new GuiHollowButton(11, this.guiLeft + 137 + 18, this.guiTop + 9 + 90, 16, 16, Color.BLACK.getRGB());

        addButton(fill);
        addButton(pencil);
        addButton(done);
        addButton(black);
        addButton(white);
        addButton(gray);
        addButton(red);
        addButton(orange);
        addButton(yellow);
        addButton(lime);
        addButton(green);
        addButton(lightBlue);
        addButton(darkBlue);
        addButton(purple);
        addButton(pink);
        for (GuiButton button : this.buttonList) {
            if (button instanceof GuiButtonTextToggle) {
                this.textToggleList.add((GuiButtonTextToggle) button);
            }
        }
        this.actionPerformed(pencil);
        this.actionPerformed(this.lessSize);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(BACKGROUND);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
        this.drawTexturedModalRect(this.guiLeft + xSize, this.guiTop, xSize, 0, toolXSize, toolYSize);
        if (this.hasSizeWindow) {
            this.drawTexturedModalRect(this.guiLeft + xSize, this.guiTop + toolYSize + 1, xSize, toolYSize + 1, sizeXSize, sizeYSize);
            drawCenteredString(this.fontRenderer, toolSize + "", this.guiLeft + xSize + 40, this.guiTop + toolYSize + 11, Color.WHITE.getRGB());
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (color != null) {
            drawRect(this.guiLeft + 138, this.guiTop + 125, this.guiLeft + 138 + 32, this.guiTop + 125 + 32, color.RGB);
        }

        //draw picture
        //we batch everything together to increase the performance
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        PictureRenderer.renderInGui(this.guiLeft + PICTURE_START_LEFT, this.guiTop + PICTURE_START_TOP, this.scaleFactor, builder, picture);
        GlStateManager.disableTexture2D();
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (handleMouse(mouseX, mouseY, mouseButton)) {
            this.clickStartedInPicture = true;
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
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
    protected void actionPerformed(GuiButton button) {
        if (button.id == -1) {
            MCPaint.NETWORKING.sendToServer(new MessageDrawComplete(this.pos, this.scaleFactor, this.picture));
            ((TileEntityCanvas) mc.world.getTileEntity(pos)).storeData(this.scaleFactor, this.picture);
            mc.displayGuiScreen(null);
        } else if (button.id == -2) {
            this.toolSize--;
            handleSizeChanged();
        } else if (button.id == -3) {
            this.toolSize++;
            handleSizeChanged();
        } else if (button.id >= 0) {
            color = EnumPaintColor.VALUES[button.id];
        } else {
            for (GuiButtonTextToggle toggleButton : this.textToggleList) {
                boolean toggled = toggleButton.id == button.id;
                toggleButton.toggled = toggled;
                if (toggled) {
                    this.activeDrawType = toggleButton.type;
                    if (this.activeDrawType.hasSizeRegulator && !this.hasSizeWindow) {
                        addButton(moreSize);
                        addButton(lessSize);
                    } else if (!this.activeDrawType.hasSizeRegulator && this.hasSizeWindow) {
                        this.buttonList.remove(this.moreSize);
                        this.buttonList.remove(this.lessSize);
                    }
                    this.hasSizeWindow = this.activeDrawType.hasSizeRegulator;
                }
            }
        }
    }

    @Override
    public void updateScreen() {
        if (!this.synced) {
            TileEntity tileEntity = Minecraft.getMinecraft().world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityCanvas) {
                ((TileEntityCanvas) tileEntity).storeData(this.scaleFactor ,this.picture);
                this.synced = true;
            }
        }
    }

    private boolean handleMouse(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return false;
        int offsetMouseX = mouseX - this.guiLeft - PICTURE_START_LEFT;
        int offsetMouseY = mouseY - this.guiTop - PICTURE_START_TOP;
        if (offsetMouseX >= 0 && offsetMouseX < (picture.length * this.scaleFactor) && offsetMouseY >= 0 && offsetMouseY < (picture.length * this.scaleFactor)) {
            int pixelPosX = offsetMouseX / this.scaleFactor;
            int pixelPosY = offsetMouseY / this.scaleFactor;
            if (pixelPosX < picture.length && pixelPosY < picture.length && this.color != null) {
                this.activeDrawType.draw(this.picture, this.color.RGB, pixelPosX, pixelPosY, this.toolSize);
                return true;
            }
        }
        return false;
    }

    private void handleSizeChanged() {
        if (this.toolSize >= 10) {
            this.toolSize = 10;
            this.moreSize.enabled = false;
        } else {
            this.moreSize.enabled = true;
        }
        if (this.toolSize <= 1) {
            this.toolSize = 1;
            this.lessSize.enabled = false;
        } else {
            this.lessSize.enabled = true;
        }
    }
}
