package ichttt.mods.mcpaint.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.gui.button.GuiButtonTextToggle;
import ichttt.mods.mcpaint.client.gui.button.GuiColorButton;
import ichttt.mods.mcpaint.client.gui.drawutil.EnumDrawType;
import ichttt.mods.mcpaint.client.gui.drawutil.PictureState;
import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DrawScreen extends Screen implements IDrawGuiCallback {
    private static final int PICTURE_START_LEFT = 6;
    private static final int PICTURE_START_TOP = 9;
    public static final ResourceLocation BACKGROUND = new ResourceLocation(MCPaint.MODID, "textures/gui/setup.png");
    private static final int xSize = 176;
    private static final int ySize = 166;
    private static final int toolXSize = 80;
    private static final int toolYSize = 95;
    private static final int sizeXSize = toolXSize;
    private static final int sizeYSize = 34;
    private final DrawScreenHelper helper;

    private int guiLeft;
    private int guiTop;
    private Button undo, redo;
    private Button lessSize, moreSize;
    private final List<GuiButtonTextToggle> textToggleList = new ArrayList<>();
    private ForgeSlider redSlider, blueSlider, greenSlider, alphaSlider;

    public DrawScreen(IPaintable canvas, List<IPaintable> prevImages, BlockPos pos, Direction facing, BlockState state) {
        super(Component.translatable("mcpaint.drawgui"));
        this.helper = new DrawScreenHelper(canvas, prevImages, pos, facing, state, this);
    }

    public DrawScreen(byte scaleFactor, BlockPos pos, Direction facing, BlockState state) {
        super(Component.translatable("mcpaint.drawgui"));
        this.helper = new DrawScreenHelper(scaleFactor, pos, facing, state, this);
    }

    @Override
    public void init() {
        this.textToggleList.clear();
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;

        Button saveImage = Button.builder(Component.translatable("mcpaint.gui.export"), button -> DrawScreen.this.helper.saveImage())
                .bounds(this.guiLeft + xSize, this.guiTop + 96, 80, 20)
                .build();
        Button rotateRight = Button.builder(Component.translatable("mcpaint.gui.rright"), button -> DrawScreen.this.helper.rotateRight())
                .bounds(this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5 + 22 + 22 + 22, 36, 20)
                .build();
        Button rotateLeft = Button.builder(Component.translatable("mcpaint.gui.rleft"), button -> DrawScreen.this.helper.rotateLeft())
                .bounds(this.guiLeft - toolXSize + 3, this.guiTop + 5 + 22 + 22 + 22, 36, 20)
                .build();
        this.redo = Button.builder(Component.translatable("mcpaint.gui.redo"), button -> helper.redo())
                .bounds(this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5 + 22 + 22, 36, 20)
                .build();
        this.undo = Button.builder(Component.translatable("mcpaint.gui.undo"), button -> helper.undo())
                .bounds(this.guiLeft - toolXSize + 3, this.guiTop + 5 + 22 + 22, 36, 20)
                .build();
        Button pickColor = new GuiButtonTextToggle(this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5 + 22, 36, 20, EnumDrawType.PICK_COLOR, DrawScreen.this::handleToolButton);
        Button erase = new GuiButtonTextToggle(this.guiLeft - toolXSize + 3, this.guiTop + 5 + 22, 36, 20, EnumDrawType.ERASER, DrawScreen.this::handleToolButton);
        Button fill = new GuiButtonTextToggle(this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5, 36, 20, EnumDrawType.FILL, DrawScreen.this::handleToolButton);
        Button pencil = new GuiButtonTextToggle(this.guiLeft - toolXSize + 3, this.guiTop + 5, 36, 20,  EnumDrawType.PENCIL, DrawScreen.this::handleToolButton);
        this.moreSize = Button.builder(Component.literal(">"), button -> {
            DrawScreen.this.helper.toolSize++;
            handleSizeChanged();
        }).bounds(this.guiLeft - toolXSize + 3 + 55, this.guiTop + toolYSize + 5, 20, 20).build();
        this.lessSize = Button.builder(Component.literal("<"), button -> {
            DrawScreen.this.helper.toolSize--;
            handleSizeChanged();
        }).bounds(this.guiLeft - toolXSize + 3, this.guiTop + toolYSize + 5, 20, 20).build();

        Button done = Button.builder(Component.translatable("gui.done"), button -> DrawScreen.this.helper.saveAndClose())
                .bounds(this.guiLeft + (xSize / 2) - (200 / 2), this.guiTop + ySize + 20, 200, 20)
                .build();

        GuiColorButton black = new GuiColorButton(0, this.guiLeft + 137, this.guiTop + 9, 16, 16, Color.BLUE.getRGB(), this::handleColorChange);
        GuiColorButton white = new GuiColorButton(1, this.guiLeft + 137 + 18, this.guiTop + 9, 16, 16, Color.BLUE.getRGB(), this::handleColorChange);
        GuiColorButton gray = new GuiColorButton(2, this.guiLeft + 137, this.guiTop + 9 + 18, 16, 16, Color.BLUE.getRGB(), this::handleColorChange);
        GuiColorButton red = new GuiColorButton(3, this.guiLeft + 137 + 18, this.guiTop + 9 + 18, 16, 16, Color.BLUE.getRGB(), this::handleColorChange);
        GuiColorButton orange = new GuiColorButton(4, this.guiLeft + 137, this.guiTop + 9 + 36, 16, 16, Color.BLUE.getRGB(), this::handleColorChange);
        GuiColorButton yellow = new GuiColorButton(5, this.guiLeft + 137 + 18, this.guiTop + 9 + 36, 16, 16, Color.BLUE.getRGB(), this::handleColorChange);

        GuiColorButton lime = new GuiColorButton(6, this.guiLeft + 137, this.guiTop + 9 + 54, 16, 16, Color.BLACK.getRGB(), this::handleColorChange);
        GuiColorButton green = new GuiColorButton(7, this.guiLeft + 137 + 18, this.guiTop + 9 + 54, 16, 16, Color.BLACK.getRGB(), this::handleColorChange);
        GuiColorButton lightBlue = new GuiColorButton(8, this.guiLeft + 137, this.guiTop + 9 + 72, 16, 16, Color.BLACK.getRGB(), this::handleColorChange);
        GuiColorButton darkBlue = new GuiColorButton(9, this.guiLeft + 137 + 18, this.guiTop + 9 + 72, 16, 16, Color.BLACK.getRGB(), this::handleColorChange);
        GuiColorButton purple = new GuiColorButton(10, this.guiLeft + 137, this.guiTop + 9 + 90, 16, 16, Color.BLACK.getRGB(), this::handleColorChange);
        GuiColorButton pink = new GuiColorButton(11, this.guiLeft + 137 + 18, this.guiTop + 9 + 90, 16, 16, Color.BLACK.getRGB(), this::handleColorChange);

        this.redSlider = makeSlider(this.guiLeft + xSize + 3, this.guiTop + 4, "mcpaint.gui.red");
        this.greenSlider = makeSlider(this.guiLeft + xSize + 3, this.guiTop + 26, "mcpaint.gui.green");
        this.blueSlider = makeSlider(this.guiLeft + xSize + 3, this.guiTop + 48,"mcpaint.gui.blue");
        this.alphaSlider = makeSlider(this.guiLeft + xSize + 3, this.guiTop + 70, "mcpaint.gui.alpha");

        addRenderableWidget(saveImage);
        addRenderableWidget(rotateRight);
        addRenderableWidget(rotateLeft);
        addRenderableWidget(redo);
        addRenderableWidget(undo);
        addRenderableWidget(pickColor);
        addRenderableWidget(erase);
        addRenderableWidget(fill);
        addRenderableWidget(pencil);
        addRenderableWidget(done);
        addRenderableWidget(black);
        addRenderableWidget(white);
        addRenderableWidget(gray);
        addRenderableWidget(red);
        addRenderableWidget(orange);
        addRenderableWidget(yellow);
        addRenderableWidget(lime);
        addRenderableWidget(green);
        addRenderableWidget(lightBlue);
        addRenderableWidget(darkBlue);
        addRenderableWidget(purple);
        addRenderableWidget(pink);
        addRenderableWidget(this.redSlider);
        addRenderableWidget(this.greenSlider);
        addRenderableWidget(this.blueSlider);
        addRenderableWidget(this.alphaSlider);

        //trigger defaults
        pencil.onClick(0, 0);
        this.lessSize.onClick(0, 0);
        black.onClick(0, 0);
        this.redo.onClick(0, 0);
    }

    @Override
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T pWidget) {
        T t = super.addRenderableWidget(pWidget);
        if (pWidget instanceof GuiButtonTextToggle) {
            this.textToggleList.add((GuiButtonTextToggle) pWidget);
        }
        return t;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //main
        guiGraphics.blit(BACKGROUND, this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
        //color
        guiGraphics.blit(BACKGROUND, this.guiLeft + xSize, this.guiTop, xSize, 0, toolXSize, toolYSize);
        //tools
        guiGraphics.blit(BACKGROUND, this.guiLeft - toolXSize, this.guiTop, xSize, 0, toolXSize, toolYSize);
        //size
        if (this.helper.hasSizeWindow()) {
            guiGraphics.blit(BACKGROUND, this.guiLeft - toolXSize, this.guiTop + toolYSize + 1, xSize, toolYSize + 1, sizeXSize, sizeYSize);
            guiGraphics.drawCenteredString(this.font, helper.toolSize + "", this.guiLeft - toolXSize + 40, this.guiTop + toolYSize + 11, Color.WHITE.getRGB());
        }

        //Background block
        helper.renderBackgroundBlock(guiGraphics, this.guiLeft + PICTURE_START_LEFT, this.guiTop + PICTURE_START_TOP);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.fill(this.guiLeft + 138, this.guiTop + 125, this.guiLeft + 138 + 32, this.guiTop + 125 + 32, this.helper.color.getRGB());

        int offsetMouseX = offsetMouseX(mouseX) ;
        int offsetMouseY = offsetMouseY(mouseY);
        boolean drawSelect = isInPicture(offsetMouseX, offsetMouseY) && this.helper.activeDrawType() != EnumDrawType.PICK_COLOR;
        int[][] toDraw = this.helper.getBasePicture();
        if (drawSelect) {
            int pixelPosX = offsetMouseX / this.helper.currentState().scaleFactor;
            int pixelPosY = offsetMouseY / this.helper.currentState().scaleFactor;
            toDraw = MCPaintUtil.copyOf(toDraw);
            this.helper.activeDrawType().draw(toDraw, helper.color, pixelPosX, pixelPosY, this.helper.toolSize);
        }

        //draw picture
        //we batch everything together to increase the performance
        helper.renderImage(guiGraphics, this.guiLeft + PICTURE_START_LEFT, this.guiTop + PICTURE_START_TOP, toDraw);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (helper.handleMouseClick(mouseX, mouseY, mouseButton))
            return true;
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double v2, double v3) {
        if (helper.handleMouseDragged(mouseX, mouseY, clickedMouseButton))
            return true;
        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, v2, v3);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        helper.handleMouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean keyPressed(int keyCode, int i1, int i2) {
        if (keyCode == GLFW.GLFW_KEY_Z && InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
            if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
                helper.redo();
            else
                helper.undo();
            return true;
        }
        return super.keyPressed(keyCode, i1, i2);
    }

    private void handleColorChange(Color color) {
        this.helper.color = color;
        updateSliders();
    }

    protected void handleToolButton(Button button) {
        for (GuiButtonTextToggle toggleButton : this.textToggleList) {
            boolean toggled = toggleButton == button;
            toggleButton.toggled = toggled;
            if (toggled) {
                boolean hadSizeWindow = helper.switchToolButton(toggleButton.type);
                if (this.helper.activeDrawType().hasSizeRegulator && !hadSizeWindow) {
                    addRenderableWidget(moreSize);
                    addRenderableWidget(lessSize);
                } else if (!this.helper.activeDrawType().hasSizeRegulator && hadSizeWindow) {
                    this.removeWidget(this.moreSize);
                    this.removeWidget(this.lessSize);
                }
            }
        }
    }

    @Override
    public void removed() {
        helper.onClose();
    }

    private ForgeSlider makeSlider(int xPos, int yPos, String key) {
        return new ForgeSlider(xPos, yPos, 74, 20, Component.translatable(key), Component.empty(), 0, 255, 0, true) {
            @Override
            protected void applyValue() {
                DrawScreen.this.onChangeSliderValue();
            }
        };
    }


    private void handleSizeChanged() {
        if (this.helper.toolSize >= 10) {
            this.helper.toolSize = 10;
            this.moreSize.active = false;
        } else {
            this.moreSize.active = true;
        }
        if (this.helper.toolSize <= 1) {
            this.helper.toolSize = 1;
            this.lessSize.active = false;
        } else {
            this.lessSize.active = true;
        }
    }

    public void onChangeSliderValue() {
        this.helper.color = new Color(this.redSlider.getValueInt(),
                this.greenSlider.getValueInt(),
                this.blueSlider.getValueInt(),
                this.alphaSlider.getValueInt());
    }

    @Override
    public void updateSliders() {
        Color color = this.helper.color;
        this.redSlider.setValue(color.getRed());
        this.blueSlider.setValue(color.getBlue());
        this.greenSlider.setValue(color.getGreen());
        this.alphaSlider.setValue(color.getAlpha());
    }

    @Override
    public void updateUndoRedoButton(boolean hasUndo, boolean hasRedo) {
        this.undo.active = hasUndo;
        this.redo.active = hasRedo;
    }

    @Override
    public boolean isInPicture(int offsetMouseX, int offsetMouseY) {
        PictureState currentState = this.helper.currentState();
        return offsetMouseX >= 0 && offsetMouseX < (currentState.picture.length * currentState.scaleFactor) && offsetMouseY >= 0 && offsetMouseY < (currentState.picture.length * currentState.scaleFactor);
    }

    @Override
    public int offsetMouseX(int mouseX) {
        return mouseX - this.guiLeft - PICTURE_START_LEFT;
    }

    @Override
    public int offsetMouseY(int mouseY) {
        return mouseY - this.guiTop - PICTURE_START_TOP;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
