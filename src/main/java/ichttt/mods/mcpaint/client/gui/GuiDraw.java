package ichttt.mods.mcpaint.client.gui;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.EnumPaintColor;
import ichttt.mods.mcpaint.client.render.PictureRenderer;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.networking.MessageDrawComplete;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class GuiDraw extends GuiScreen {
    private static final int PICTURE_START_LEFT = 14;
    private static final int PICTURE_START_TOP = 17;
    private static final ResourceLocation BACKGROUND = new ResourceLocation(MCPaint.MODID, "textures/gui/setup.png");
    public static final int xSize = 176;
    public static final int ySize = 166;

    private final byte scaleFactor;
    private final int[][] picture;
    private final BlockPos pos;

    private EnumPaintColor color = null;
    private int guiLeft;
    private int guiTop;
    private boolean clickStartedInPicture = false;

    public GuiDraw(byte scaleFactor, BlockPos pos) {
        this.pos = Objects.requireNonNull(pos);
        this.scaleFactor = scaleFactor;
        this.picture = new int[TileEntityCanvas.CANVAS_PIXEL_COUNT / scaleFactor][TileEntityCanvas.CANVAS_PIXEL_COUNT / scaleFactor];
        for (int[] tileArray : picture)
            Arrays.fill(tileArray, Color.WHITE.getRGB());
    }

    @Override
    public void initGui() {
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;
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
            mc.world.setBlockState(pos, mc.world.getBlockState(pos).withProperty(BlockCanvas.PAINTED, true));
            ((TileEntityCanvas) mc.world.getTileEntity(pos)).storeData(this.scaleFactor, this.picture);
            mc.displayGuiScreen(null);
        }
        if (button.id >= 0)
            color = EnumPaintColor.VALUES[button.id];
    }

    private boolean handleMouse(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return false;
        int offsetMouseX = mouseX - this.guiLeft - PICTURE_START_LEFT;
        int offsetMouseY = mouseY - this.guiTop - PICTURE_START_TOP;
        if (offsetMouseX >= 0 && offsetMouseX < (picture.length * this.scaleFactor) && offsetMouseY >= 0 && offsetMouseY < (picture.length * this.scaleFactor)) {
            int pixelPosX = offsetMouseX / this.scaleFactor;
            int pixelPosY = offsetMouseY / this.scaleFactor;
            if (pixelPosX < picture.length && pixelPosY < picture.length && this.color != null) {
                picture[pixelPosX][pixelPosY] = this.color.RGB;
                return true;
            }
        }
        return false;
    }
}
