package ichttt.mods.mcpaint.client.gui;

import com.google.common.base.Preconditions;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.gui.drawutil.EnumDrawType;
import ichttt.mods.mcpaint.client.gui.drawutil.EnumPaintColor;
import ichttt.mods.mcpaint.client.gui.drawutil.PictureState;
import ichttt.mods.mcpaint.client.render.PictureRenderer;
import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import ichttt.mods.mcpaint.networking.MessageDrawAbort;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
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

    private final BlockPos pos;
    private final EnumFacing facing;
    private final IBlockState state;
    private final LinkedList<PictureState> statesForUndo = new LinkedList<>();
    private final LinkedList<PictureState> statesForRedo = new LinkedList<>();

    private Color color = Color.BLACK;
    private PictureState paintingState;
    private PictureState currentState;
    private int guiLeft;
    private int guiTop;
    private boolean clickStartedInPicture = false;
    private final List<GuiButtonTextToggle> textToggleList = new ArrayList<>();
    private EnumDrawType activeDrawType;
    private int toolSize = 1;
    private GuiButton undo, redo;
    private GuiButton lessSize, moreSize;
    private GuiSlider redSlider, blueSlider, greenSlider, alphaSlider;
    private boolean hasSizeWindow;
    private boolean handled = false;

    public GuiDraw(IPaintable canvas, List<IPaintable> prevImages, BlockPos pos, EnumFacing facing, IBlockState state) {
        Objects.requireNonNull(canvas, "Canvas is null");
        Preconditions.checkArgument(canvas.hasPaintData(), "No data in canvas");
        this.pos = pos;
        this.facing = facing;
        this.state = state;
        this.currentState = new PictureState(canvas);
        for (IPaintable paint : prevImages)
            this.statesForUndo.add(new PictureState(paint));
    }

    protected GuiDraw(byte scaleFactor, BlockPos pos, EnumFacing facing, IBlockState state) {
        this.pos = Objects.requireNonNull(pos);
        this.facing = facing;
        this.state = state;
        int[][] picture = new int[128 / scaleFactor][128 / scaleFactor];
        for (int[] tileArray : picture)
            Arrays.fill(tileArray, ZERO_ALPHA);
        this.currentState = new PictureState(picture, scaleFactor);
    }

    @Override
    public void initGui() {
        this.hasSizeWindow = false;
        this.textToggleList.clear();
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;

        this.redo = new GuiButton(-9, this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5 + 22 + 25, 36, 20, I18n.format("mcpaint.gui.redo"));
        this.undo = new GuiButton(-8, this.guiLeft - toolXSize + 3, this.guiTop + 5 + 22 + 25, 36, 20, I18n.format("mcpaint.gui.undo"));
        GuiButton pickColor = new GuiButtonTextToggle(-7, this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5 + 22, 36, 20, EnumDrawType.PICK_COLOR);
        GuiButton erase = new GuiButtonTextToggle(-6, this.guiLeft - toolXSize + 3, this.guiTop + 5 + 22, 36, 20, EnumDrawType.ERASER);
        GuiButton fill = new GuiButtonTextToggle(-5, this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5, 36, 20, EnumDrawType.FILL);
        GuiButton pencil = new GuiButtonTextToggle(-4, this.guiLeft - toolXSize + 3, this.guiTop + 5, 36, 20,  EnumDrawType.PENCIL);
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

        this.redSlider = new GuiSlider(this, 100, this.guiLeft + xSize + 3, this.guiTop + 4, I18n.format("mcpaint.gui.red"), 0, 255, 0, this);
        this.redSlider.width = 74;
        this.greenSlider = new GuiSlider(this, 100, this.guiLeft + xSize + 3, this.guiTop + 26, I18n.format("mcpaint.gui.green"), 0, 255, 0, this);
        this.greenSlider.width = 74;
        this.blueSlider = new GuiSlider(this, 100, this.guiLeft + xSize + 3, this.guiTop + 48, I18n.format("mcpaint.gui.blue"), 0, 255, 0, this);
        this.blueSlider.width = 74;
        this.alphaSlider = new GuiSlider(this, 100, this.guiLeft + xSize + 3, this.guiTop + 70, I18n.format("mcpaint.gui.alpha"), 0, 255, 0, this);
        this.alphaSlider.width = 74;

        addButton(redo);
        addButton(undo);
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
        //trigger defaults
        this.actionPerformed(pencil);
        this.actionPerformed(this.lessSize);
        this.actionPerformed(black);
        this.actionPerformed(this.redo);
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

        drawRect(this.guiLeft + 138, this.guiTop + 125, this.guiLeft + 138 + 32, this.guiTop + 125 + 32, this.color.getRGB());

        int offsetMouseX = mouseX - this.guiLeft - PICTURE_START_LEFT;
        int offsetMouseY = mouseY - this.guiTop - PICTURE_START_TOP;
        boolean drawSelect = isInWindow(offsetMouseX, offsetMouseY) && this.activeDrawType != EnumDrawType.PICK_COLOR;
        int toDraw[][] = this.paintingState == null ? this.currentState.picture : this.paintingState.picture;
        if (drawSelect) {
            int pixelPosX = offsetMouseX / this.currentState.scaleFactor;
            int pixelPosY = offsetMouseY / this.currentState.scaleFactor;
            toDraw = MCPaintUtil.copyOf(toDraw);
            this.activeDrawType.draw(toDraw, color, pixelPosX, pixelPosY, this.toolSize);
        }

        //draw picture
        //we batch everything together to increase the performance
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        PictureRenderer.renderInGui(this.guiLeft + PICTURE_START_LEFT, this.guiTop + PICTURE_START_TOP, this.currentState.scaleFactor, buffer, toDraw);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
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
        if (this.paintingState != null) {
            this.newPictureState(this.paintingState);
            this.paintingState = null;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && keyCode == 44) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                redo();
            else
                undo();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == -1) {
            if (Arrays.stream(this.currentState.picture).anyMatch(ints -> Arrays.stream(ints).anyMatch(value -> value != ZERO_ALPHA))) {
                this.handled = true;
                MCPaintUtil.uploadPictureToServer(this.mc.world.getTileEntity(this.pos), this.facing, this.currentState.scaleFactor, this.currentState.picture);
            }
            this.mc.displayGuiScreen(null);
        } else if (button.id == -2) {
            this.toolSize--;
            handleSizeChanged();
        } else if (button.id == -3) {
            this.toolSize++;
            handleSizeChanged();
        } else if(button.id == -8) {
            undo();
        } else if (button.id == -9) {
            redo();
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
    public void onGuiClosed() {
        if (!handled) {
            MCPaint.NETWORKING.sendToServer(new MessageDrawAbort(pos));
        }
    }

    private void newPictureState(PictureState state) {
        if (state.isSame(this.currentState)) return;
        this.statesForRedo.clear();
        this.statesForUndo.add(this.currentState);
        this.currentState = state;
        if (this.statesForUndo.size() > 20) {
            this.statesForUndo.removeFirst();
        }
        updateUndoRedoButton();
    }

    private void undo() {
        if (this.statesForUndo.size() > 0) {
            this.statesForRedo.add(this.currentState);
            this.currentState = this.statesForUndo.removeLast();
        }
        updateUndoRedoButton();
    }

    private void redo() {
        if (this.statesForRedo.size() > 0) {
            this.statesForUndo.add(this.currentState);
            this.currentState = this.statesForRedo.removeLast();
        }
        updateUndoRedoButton();
    }

    private void updateUndoRedoButton() {
        this.undo.enabled = !this.statesForUndo.isEmpty();
        this.redo.enabled = !this.statesForRedo.isEmpty();
    }

    private boolean handleMouse(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return false;
        int offsetMouseX = mouseX - this.guiLeft - PICTURE_START_LEFT;
        int offsetMouseY = mouseY - this.guiTop - PICTURE_START_TOP;
        if (isInWindow(offsetMouseX, offsetMouseY)) {
            int pixelPosX = offsetMouseX / this.currentState.scaleFactor;
            int pixelPosY = offsetMouseY / this.currentState.scaleFactor;
            if (this.paintingState == null)
                this.paintingState = new PictureState(this.currentState);
            if (pixelPosX < this.paintingState.picture.length && pixelPosY < this.paintingState.picture.length && this.color != null) {
                this.color = this.activeDrawType.draw(this.paintingState.picture, this.color, pixelPosX, pixelPosY, this.toolSize);
                this.redSlider.setSliderValue(this.color.getRed(), false);
                this.blueSlider.setSliderValue(this.color.getBlue(), false);
                this.greenSlider.setSliderValue(this.color.getGreen(), false);
                this.alphaSlider.setSliderValue(this.color.getAlpha(), false);
                return true;
            }
        }
        return false;
    }

    private boolean isInWindow(int offsetMouseX, int offsetMouseY) {
        return offsetMouseX >= 0 && offsetMouseX < (this.currentState.picture.length * this.currentState.scaleFactor) && offsetMouseY >= 0 && offsetMouseY < (this.currentState.picture.length * this.currentState.scaleFactor);
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
    public void setEntryValue(int id, @Nonnull String value) {}

    @Nonnull
    @Override
    public String getText(int id,@Nonnull String name, float value) {
        return name + ":" + Math.round(value);
    }
}
