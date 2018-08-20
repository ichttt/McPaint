package ichttt.mods.mcpaint.client.gui;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.EnumPaintColor;
import ichttt.mods.mcpaint.client.render.PictureRenderer;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import ichttt.mods.mcpaint.networking.MessageDrawAbort;
import ichttt.mods.mcpaint.networking.MessagePaintData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GuiDraw extends GuiScreen implements GuiPageButtonList.GuiResponder, GuiSlider.FormatHelper {
    public static final ResourceLocation BACKGROUND = new ResourceLocation(MCPaint.MODID, "textures/gui/setup.png");
    public static final int ZERO_ALPHA = new Color(255, 255,255, 0).getRGB();
    private static final int PICTURE_START_LEFT = 6;
    private static final int PICTURE_START_TOP = 9;
    private static final int xSize = 176;
    private static final int ySize = 166;
    private static final int toolXSize = 80;
    private static final int toolYSize = 95;
    private static final int sizeXSize = toolXSize;
    private static final int sizeYSize = 34;

    private final byte scaleFactor;
    private final BlockPos pos;
    private final EnumFacing facing;
    private final IBlockState state;

    public int[][] picture;
    public Color color = null;
    private int guiLeft;
    private int guiTop;
    private boolean clickStartedInPicture = false;
    private final List<GuiButtonTextToggle> textToggleList = new ArrayList<>();
    private EnumDrawType activeDrawType;
    private int toolSize = 1;
    private GuiButton lessSize, moreSize;
    private GuiSlider redSlider, blueSlider, greenSlider, alphaSlider;
    private boolean hasSizeWindow;
    private boolean synced = false;
    private boolean handled = false;

    public GuiDraw(IPaintable canvas, BlockPos pos, EnumFacing facing, IBlockState state) {
        Objects.requireNonNull(canvas);
        if (!canvas.hasPaintData())
            throw new IllegalArgumentException("No data in canvas");
        this.pos = pos;
        this.facing = facing;
        this.state = state;
        this.scaleFactor = canvas.getScaleFactor();
        this.picture = canvas.getPictureData();
        this.synced = true;
        canvas.setData(this.scaleFactor, this.picture, true);
    }

    public GuiDraw(byte scaleFactor, BlockPos pos, EnumFacing facing, IBlockState state) {
        this.pos = Objects.requireNonNull(pos);
        this.facing = facing;
        this.state = state;
        this.scaleFactor = scaleFactor;
        this.picture = new int[128 / scaleFactor][128 / scaleFactor];
        for (int[] tileArray : picture)
            Arrays.fill(tileArray, ZERO_ALPHA);
    }

    @Override
    public void initGui() {
        this.hasSizeWindow = false;
        this.textToggleList.clear();
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;

        GuiButton pickColor = new GuiButtonTextToggle(-7, this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5 + 22, 36, 20, "Picker", EnumDrawType.PICK_COLOR);
        GuiButton erase = new GuiButtonTextToggle(-6, this.guiLeft - toolXSize + 3, this.guiTop + 5 + 22, 36, 20, "Eraser", EnumDrawType.ERASER);
        GuiButton fill = new GuiButtonTextToggle(-5, this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5, 36, 20, "Fill", EnumDrawType.FILL);
        GuiButton pencil = new GuiButtonTextToggle(-4, this.guiLeft - toolXSize + 3, this.guiTop + 5, 36, 20, "Pencil", EnumDrawType.PENCIL);
        this.moreSize = new GuiButton(-3, this.guiLeft - toolXSize + 3 + 55, this.guiTop + toolYSize + 5, 20, 20, ">");
        this.lessSize = new GuiButton(-2, this.guiLeft - toolXSize + 3, this.guiTop + toolYSize + 5, 20, 20, "<");
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

        this.redSlider = new GuiSlider(this, 100, this.guiLeft + xSize + 3, this.guiTop + 4, "red", 0, 255, 0, this);
        this.redSlider.width = 74;
        this.greenSlider = new GuiSlider(this, 100, this.guiLeft + xSize + 3, this.guiTop + 26, "green", 0, 255, 0, this);
        this.greenSlider.width = 74;
        this.blueSlider = new GuiSlider(this, 100, this.guiLeft + xSize + 3, this.guiTop + 48, "blue", 0, 255, 0, this);
        this.blueSlider.width = 74;
        this.alphaSlider = new GuiSlider(this, 100, this.guiLeft + xSize + 3, this.guiTop + 70, "alpha", 0, 255, 0, this);
        this.alphaSlider.width = 74;

        addButton(pickColor);
        addButton(erase);
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
        addButton(this.redSlider);
        addButton(this.greenSlider);
        addButton(this.blueSlider);
        addButton(this.alphaSlider);
        for (GuiButton button : this.buttonList) {
            if (button instanceof GuiButtonTextToggle) {
                this.textToggleList.add((GuiButtonTextToggle) button);
            }
        }
        this.actionPerformed(pencil);
        this.actionPerformed(this.lessSize);
        this.actionPerformed(black);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(BACKGROUND);
        //main
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
        //color
        this.drawTexturedModalRect(this.guiLeft + xSize, this.guiTop, xSize, 0, toolXSize, toolYSize);
        //tools
        this.drawTexturedModalRect(this.guiLeft - toolXSize, this.guiTop, xSize, 0, toolXSize, toolYSize);
        //size
        if (this.hasSizeWindow) {
            this.drawTexturedModalRect(this.guiLeft - toolXSize, this.guiTop + toolYSize + 1, xSize, toolYSize + 1, sizeXSize, sizeYSize);
            drawCenteredString(this.fontRenderer, toolSize + "", this.guiLeft - toolXSize + 40, this.guiTop + toolYSize + 11, Color.WHITE.getRGB());
        }


        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        //Background block
        List<BakedQuad> quads = this.mc.getBlockRendererDispatcher().getModelForState(state).getQuads(state, facing.getOpposite(), 0);
        for (BakedQuad quad : quads) {
            TextureAtlasSprite sprite = quad.getSprite();
            GlStateManager.pushMatrix();
            this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            this.zLevel = -1F;
            //See BlockModelRenderer
            if (quad.hasTintIndex()) {
                int color = mc.getBlockColors().colorMultiplier(state, mc.world, pos, quad.getTintIndex());
                float red = (float) (color >> 16 & 255) / 255.0F;
                float green = (float) (color >> 8 & 255) / 255.0F;
                float blue = (float) (color & 255) / 255.0F;
                GlStateManager.color(red, green, blue);
            }
            this.drawTexturedModalRect(this.guiLeft + PICTURE_START_LEFT, this.guiTop + PICTURE_START_TOP, sprite, 128, 128);
            this.zLevel = 0F;
            GlStateManager.popMatrix();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.color != null) {
            drawRect(this.guiLeft + 138, this.guiTop + 125, this.guiLeft + 138 + 32, this.guiTop + 125 + 32, color.getRGB());
        }

        int offsetMouseX = mouseX - this.guiLeft - PICTURE_START_LEFT;
        int offsetMouseY = mouseY - this.guiTop - PICTURE_START_TOP;
        //TODO clean this up, implement strg+z
        boolean drawSelect = this.color != null && isInWindow(offsetMouseX, offsetMouseY) && this.activeDrawType != EnumDrawType.PICK_COLOR;
        int orig[][] = this.picture;
        if (drawSelect) {
            int pixelPosX = offsetMouseX / this.scaleFactor;
            int pixelPosY = offsetMouseY / this.scaleFactor;
            this.picture = new int[this.picture.length][];
            for (int i = 0; i < orig.length; i++)
                this.picture[i] = orig[i].clone();
            this.activeDrawType.draw(this, pixelPosX, pixelPosY, this.toolSize);
        }

        //draw picture
        //we batch everything together to increase the performance
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        PictureRenderer.renderInGui(this.guiLeft + PICTURE_START_LEFT, this.guiTop + PICTURE_START_TOP, this.scaleFactor, buffer, this.picture);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        if (drawSelect) {
            this.picture = orig;
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
            if (Arrays.stream(picture).anyMatch(ints -> Arrays.stream(ints).anyMatch(value -> value != ZERO_ALPHA))) {
                this.handled = true;
                MessagePaintData.createAndSend(this.pos, this.facing, this.scaleFactor, this.picture);
                IPaintable paintable = ((TileEntityCanvas) Objects.requireNonNull(mc.world.getTileEntity(pos))).getPaintFor(facing);
                paintable.setData(this.scaleFactor, this.picture, false);
            }
            this.mc.displayGuiScreen(null);
        } else if (button.id == -2) {
            this.toolSize--;
            handleSizeChanged();
        } else if (button.id == -3) {
            this.toolSize++;
            handleSizeChanged();
        } else if (button.id >= 0 && button.id < 100) {
            this.color = EnumPaintColor.VALUES[button.id].color;
            this.redSlider.setSliderValue(this.color.getRed(), false);
            this.blueSlider.setSliderValue(this.color.getBlue(), false);
            this.greenSlider.setSliderValue(this.color.getGreen(), false);
            this.alphaSlider.setSliderValue(this.color.getAlpha(), false);
        } else if (button.id < 100){
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
                ((TileEntityCanvas) tileEntity).getPaintFor(facing).setData(this.scaleFactor, this.picture, true);
                this.synced = true;
            }
        }
    }

    @Override
    public void onGuiClosed() {
        if (!handled) {
            MCPaint.NETWORKING.sendToServer(new MessageDrawAbort(pos));
        }
    }

    private boolean handleMouse(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return false;
        int offsetMouseX = mouseX - this.guiLeft - PICTURE_START_LEFT;
        int offsetMouseY = mouseY - this.guiTop - PICTURE_START_TOP;
        if (isInWindow(offsetMouseX, offsetMouseY)) {
            int pixelPosX = offsetMouseX / this.scaleFactor;
            int pixelPosY = offsetMouseY / this.scaleFactor;
            if (pixelPosX < picture.length && pixelPosY < picture.length && this.color != null) {
                this.activeDrawType.draw(this, pixelPosX, pixelPosY, this.toolSize);
                return true;
            }
        }
        return false;
    }

    private boolean isInWindow(int offsetMouseX, int offsetMouseY) {
        return offsetMouseX >= 0 && offsetMouseX < (picture.length * this.scaleFactor) && offsetMouseY >= 0 && offsetMouseY < (picture.length * this.scaleFactor);
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

    @Override
    public void setEntryValue(int id, boolean value) {}

    @Override
    public void setEntryValue(int id, float value) {
        this.color = new Color(Math.round(this.redSlider.getSliderValue()),
                Math.round(this.greenSlider.getSliderValue()),
                Math.round(this.blueSlider.getSliderValue()),
                Math.round(this.alphaSlider.getSliderValue()));
    }

    @Override
    public void setEntryValue(int id, String value) {}

    @Override
    public String getText(int id, String name, float value) {
        return name + ":" + Math.round(value);
    }
}
