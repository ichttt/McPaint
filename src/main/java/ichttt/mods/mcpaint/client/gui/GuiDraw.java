package ichttt.mods.mcpaint.client.gui;

import com.google.common.base.Preconditions;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.gui.button.GuiButtonTextToggle;
import ichttt.mods.mcpaint.client.gui.button.GuiColorButton;
import ichttt.mods.mcpaint.client.gui.drawutil.EnumDrawType;
import ichttt.mods.mcpaint.client.gui.drawutil.PictureState;
import ichttt.mods.mcpaint.client.render.PictureRenderer;
import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import ichttt.mods.mcpaint.networking.MessageDrawAbort;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.resources.IResource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class GuiDraw extends Screen implements GuiSlider.ISlider {
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
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    private final BlockPos pos;
    private final Direction facing;
    private final BlockState state;
    private final IBakedModel model;
    private final LinkedList<PictureState> statesForUndo = new LinkedList<>();
    private final LinkedList<PictureState> statesForRedo = new LinkedList<>();
    private final boolean hadPaint;

    private Color color = Color.BLACK;
    private PictureState paintingState;
    private PictureState currentState;
    private int guiLeft;
    private int guiTop;
    private boolean clickStartedInPicture = false;
    private final List<GuiButtonTextToggle> textToggleList = new ArrayList<>();
    private EnumDrawType activeDrawType = EnumDrawType.PENCIL;
    private int toolSize = 1;
    private Button undo, redo;
    private Button lessSize, moreSize;
    private GuiSlider redSlider, blueSlider, greenSlider, alphaSlider;
    private boolean hasSizeWindow;
    private boolean noRevert = false;
    private boolean updating = false;

    public GuiDraw(IPaintable canvas, List<IPaintable> prevImages, BlockPos pos, Direction facing, BlockState state) {
        Objects.requireNonNull(canvas, "Canvas is null");
        Preconditions.checkArgument(canvas.hasPaintData(), "No data in canvas");
        this.pos = pos;
        this.facing = facing;
        this.state = state;
        this.model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state);
        this.currentState = new PictureState(canvas);
        for (IPaintable paint : prevImages)
            this.statesForUndo.add(new PictureState(paint));
        hadPaint = true;
    }

    protected GuiDraw(byte scaleFactor, BlockPos pos, Direction facing, BlockState state) {
        this.pos = Objects.requireNonNull(pos);
        this.facing = facing;
        this.state = state;
        this.model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state);
        int[][] picture = new int[128 / scaleFactor][128 / scaleFactor];
        for (int[] tileArray : picture)
            Arrays.fill(tileArray, ZERO_ALPHA);
        this.currentState = new PictureState(picture, scaleFactor);
        hadPaint = false;
    }

    @Override
    public void initGui() {
        this.hasSizeWindow = false;
        this.textToggleList.clear();
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;

        Button saveImage = new Button(-12, this.guiLeft + xSize, this.guiTop + 96, 80, 20, "Export") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                try {
                    saveImage(true);
                } catch (IOException e) {
                    MCPaint.LOGGER.error("Could not save image!", e);
                    mc.player.sendStatusMessage(new StringTextComponent("Failed to save file!"), true);
                    mc.player.sendStatusMessage(new StringTextComponent("Failed to save file!"), false);
                }
            }
        };
        Button rotateRight = new Button(-11, this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5 + 22 + 22 + 22, 36, 20, I18n.format("mcpaint.gui.rright")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                int[][] newData = new int[GuiDraw.this.currentState.picture.length][GuiDraw.this.currentState.picture[0].length];
                for (int x = 0; x < GuiDraw.this.currentState.picture.length; x++) {
                    int[] yData = GuiDraw.this.currentState.picture[x];
                    for (int y = 0; y < yData.length; y++) {
                        newData[yData.length - y - 1][x] = yData[y];
                    }
                }
                newPictureState(new PictureState(newData, GuiDraw.this.currentState.scaleFactor));
            }
        };
        Button rotateLeft = new Button(-10, this.guiLeft - toolXSize + 3, this.guiTop + 5 + 22 + 22 + 22, 36, 20, I18n.format("mcpaint.gui.rleft")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                int[][] newData = new int[GuiDraw.this.currentState.picture.length][GuiDraw.this.currentState.picture[0].length];
                for (int x = 0; x < GuiDraw.this.currentState.picture.length; x++) {
                    int[] yData = GuiDraw.this.currentState.picture[x];
                    for (int y = 0; y < yData.length; y++) {
                        newData[y][GuiDraw.this.currentState.picture.length - x - 1] = yData[y];
                    }
                }
                newPictureState(new PictureState(newData, GuiDraw.this.currentState.scaleFactor));
            }
        };
        this.redo = new Button(-9, this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5 + 22 + 22, 36, 20, I18n.format("mcpaint.gui.redo")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                redo();
            }
        };
        this.undo = new Button(-8, this.guiLeft - toolXSize + 3, this.guiTop + 5 + 22 + 22, 36, 20, I18n.format("mcpaint.gui.undo")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                undo();
            }
        };
        Button pickColor = new GuiButtonTextToggle(-7, this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5 + 22, 36, 20, EnumDrawType.PICK_COLOR) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                GuiDraw.this.handleToolButton(this);
            }
        };
        Button erase = new GuiButtonTextToggle(-6, this.guiLeft - toolXSize + 3, this.guiTop + 5 + 22, 36, 20, EnumDrawType.ERASER) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                GuiDraw.this.handleToolButton(this);
            }
        };
        Button fill = new GuiButtonTextToggle(-5, this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5, 36, 20, EnumDrawType.FILL) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                GuiDraw.this.handleToolButton(this);
            }
        };
        Button pencil = new GuiButtonTextToggle(-4, this.guiLeft - toolXSize + 3, this.guiTop + 5, 36, 20,  EnumDrawType.PENCIL) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                GuiDraw.this.handleToolButton(this);
            }
        };
        this.moreSize = new Button(-3, this.guiLeft - toolXSize + 3 + 55, this.guiTop + toolYSize + 5, 20, 20, ">") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                GuiDraw.this.toolSize++;
                handleSizeChanged();
            }
        };
        this.lessSize = new Button(-2, this.guiLeft - toolXSize + 3, this.guiTop + toolYSize + 5, 20, 20, "<") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                GuiDraw.this.toolSize--;
                handleSizeChanged();
            }
        };
        Button done = new Button(-1, this.guiLeft + (xSize / 2) - (200 / 2), this.guiTop + ySize + 20, 200, 20, I18n.format("gui.done")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                if (Arrays.stream(GuiDraw.this.currentState.picture).anyMatch(ints -> Arrays.stream(ints).anyMatch(value -> value != ZERO_ALPHA))) {
                    GuiDraw.this.noRevert = true;
                    MCPaintUtil.uploadPictureToServer(GuiDraw.this.mc.world.getTileEntity(GuiDraw.this.pos), GuiDraw.this.facing, GuiDraw.this.currentState.scaleFactor, GuiDraw.this.currentState.picture, false);
                } else if (hadPaint) {
                    MCPaintUtil.uploadPictureToServer(GuiDraw.this.mc.world.getTileEntity(GuiDraw.this.pos), GuiDraw.this.facing, GuiDraw.this.currentState.scaleFactor, GuiDraw.this.currentState.picture, true);
                }
                GuiDraw.this.mc.displayGuiScreen(null);
            }
        };

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

        this.redSlider = make(100, this.guiLeft + xSize + 3, this.guiTop + 4, "mcpaint.gui.red");
        this.redSlider.width = 74;
        this.greenSlider = make(101, this.guiLeft + xSize + 3, this.guiTop + 26, "mcpaint.gui.green");
        this.greenSlider.width = 74;
        this.blueSlider = make(102, this.guiLeft + xSize + 3, this.guiTop + 48,"mcpaint.gui.blue");
        this.blueSlider.width = 74;
        this.alphaSlider = make(103, this.guiLeft + xSize + 3, this.guiTop + 70, "mcpaint.gui.alpha");

        addButton(saveImage);
        addButton(rotateRight);
        addButton(rotateLeft);
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
        for (Button button : this.buttons) {
            if (button instanceof GuiButtonTextToggle) {
                this.textToggleList.add((GuiButtonTextToggle) button);
            }
        }
        //trigger defaults
        pencil.onClick(0, 0);
        this.lessSize.onClick(0, 0);
        black.onClick(0, 0);
        this.redo.onClick(0, 0);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
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
        List<BakedQuad> quads = model.getQuads(state, facing.getOpposite(), new Random(), EmptyModelData.INSTANCE);
        for (BakedQuad quad : quads) {
            TextureAtlasSprite sprite = quad.getSprite();
            GlStateManager.pushMatrix();
            this.mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            this.zLevel = -1F;
            //See BlockModelRenderer
            if (quad.hasTintIndex()) {
                int color = mc.getBlockColors().getColor(state, mc.world, pos, quad.getTintIndex());
                float red = (float) (color >> 16 & 255) / 255.0F;
                float green = (float) (color >> 8 & 255) / 255.0F;
                float blue = (float) (color & 255) / 255.0F;
                GlStateManager.color3f(red, green, blue);
            }
            this.drawTexturedModalRect(this.guiLeft + PICTURE_START_LEFT, this.guiTop + PICTURE_START_TOP, sprite, 128, 128);
            this.zLevel = 0F;
            GlStateManager.popMatrix();
        }

        super.render(mouseX, mouseY, partialTicks);

        drawRect(this.guiLeft + 138, this.guiTop + 125, this.guiLeft + 138 + 32, this.guiTop + 125 + 32, this.color.getRGB());

        int offsetMouseX = mouseX - this.guiLeft - PICTURE_START_LEFT;
        int offsetMouseY = mouseY - this.guiTop - PICTURE_START_TOP;
        boolean drawSelect = isInWindow(offsetMouseX, offsetMouseY) && this.activeDrawType != EnumDrawType.PICK_COLOR;
        int[][] toDraw = this.paintingState == null ? this.currentState.picture : this.paintingState.picture;
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
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (handleMouse(mouseX, mouseY, mouseButton)) {
            this.clickStartedInPicture = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double v2, double v3) {
        if (this.clickStartedInPicture && handleMouse(mouseX, mouseY, clickedMouseButton))
            return true;
        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, v2, v3);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        this.clickStartedInPicture = false;
        if (this.paintingState != null) {
            this.newPictureState(this.paintingState);
            this.paintingState = null;
        }
        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean keyPressed(int keyCode, int i1, int i2) {
        if (keyCode == GLFW.GLFW_KEY_Z && InputMappings.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
            if (InputMappings.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT))
                redo();
            else
                undo();
            return true;
        }
        return super.keyPressed(keyCode, i1, i2);
    }

    private void handleColorChange(Color color) {
        this.color = color;
        updateSliders();
    }

    protected void handleToolButton(Button button) {
        for (GuiButtonTextToggle toggleButton : this.textToggleList) {
            boolean toggled = toggleButton.id == button.id;
            toggleButton.toggled = toggled;
            if (toggled) {
                this.activeDrawType = toggleButton.type;
                if (this.activeDrawType.hasSizeRegulator && !this.hasSizeWindow) {
                    addButton(moreSize);
                    addButton(lessSize);
                } else if (!this.activeDrawType.hasSizeRegulator && this.hasSizeWindow) {
                    this.buttons.remove(this.moreSize);
                    this.buttons.remove(this.lessSize);
                }
                this.hasSizeWindow = this.activeDrawType.hasSizeRegulator;
            }
        }
    }

    @Override
    public void onGuiClosed() {
        if (!noRevert) {
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

    private boolean handleMouse(double mouseXD, double mouseYD, int mouseButton) {
        if (mouseButton != 0) return false;
        int mouseX = (int) Math.round(mouseXD);
        int mouseY = (int) Math.round(mouseYD);
        int offsetMouseX = mouseX - this.guiLeft - PICTURE_START_LEFT;
        int offsetMouseY = mouseY - this.guiTop - PICTURE_START_TOP;
        if (isInWindow(offsetMouseX, offsetMouseY)) {
            int pixelPosX = offsetMouseX / this.currentState.scaleFactor;
            int pixelPosY = offsetMouseY / this.currentState.scaleFactor;
            if (this.paintingState == null)
                this.paintingState = new PictureState(this.currentState);
            if (pixelPosX < this.paintingState.picture.length && pixelPosY < this.paintingState.picture.length && this.color != null) {
                this.color = this.activeDrawType.draw(this.paintingState.picture, this.color, pixelPosX, pixelPosY, this.toolSize);
                updateSliders();
                return true;
            }
        }
        return false;
    }

    private boolean isInWindow(int offsetMouseX, int offsetMouseY) {
        return offsetMouseX >= 0 && offsetMouseX < (this.currentState.picture.length * this.currentState.scaleFactor) && offsetMouseY >= 0 && offsetMouseY < (this.currentState.picture.length * this.currentState.scaleFactor);
    }

    private GuiSlider make(int id, int xPos, int yPos, String key) {
        return new GuiSlider(id, xPos, yPos, 74, 20, I18n.format(key), "", 0, 255, 0,false, true, this);
    }

    private void updateSliders() {
        this.redSlider.setValue(this.color.getRed());
        this.blueSlider.setValue(this.color.getBlue());
        this.greenSlider.setValue(this.color.getGreen());
        this.alphaSlider.setValue(this.color.getAlpha());
        updating = true;
        this.redSlider.updateSlider();
        this.blueSlider.updateSlider();
        this.greenSlider.updateSlider();
        this.alphaSlider.updateSlider();
        updating = false;
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

    private void saveImage(boolean background) throws IOException {
        BufferedImage paint = new BufferedImage(128 / this.currentState.scaleFactor, 128 / this.currentState.scaleFactor, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < this.currentState.picture.length; x++) {
            for (int y = 0; y < this.currentState.picture[0].length; y++) {
                paint.setRGB(x, y, this.currentState.picture[x][y]);
            }
        }
        BufferedImage output = new BufferedImage(paint.getWidth(), paint.getHeight(), BufferedImage.TYPE_INT_ARGB);

        if (background) {
            List<BakedQuad> quads = model.getQuads(state, facing.getOpposite(), new Random(), EmptyModelData.INSTANCE);
            for (BakedQuad quad : quads) {
                TextureAtlasSprite sprite = quad.getSprite();
                try (IResource resource = mc.getResourceManager().getResource(getResourceLocation(sprite))) {
                    Image image = ImageIO.read(resource.getInputStream());
                    if (quad.hasTintIndex()) {
                        int color = mc.getBlockColors().getColor(state, mc.world, pos, quad.getTintIndex());
                        float red = (float) (color >> 16 & 255) / 255.0F;
                        float green = (float) (color >> 8 & 255) / 255.0F;
                        float blue = (float) (color & 255) / 255.0F;
                        BufferedImage asBufferedImage = (BufferedImage) image;
                        for (int x = 0; x < paint.getWidth(); x++) {
                            for (int y = 0; y < paint.getHeight(); y++) {
                                Color originalColor = new Color(asBufferedImage.getRGB(x, y), true);
                                int newRed = Math.round(originalColor.getRed() * red);
                                int newGreen = Math.round(originalColor.getGreen() * green);
                                int newBlue = Math.round(originalColor.getBlue() * blue);
                                asBufferedImage.setRGB(x, y, new Color(newRed, newGreen, newBlue, originalColor.getAlpha()).getRGB());
                            }
                        }
                    }
                    image = image.getScaledInstance(output.getWidth(), output.getHeight(), Image.SCALE_FAST);
                    output.getGraphics().drawImage(image, 0, 0, null);
                }
            }
        }
        output.getGraphics().drawImage(paint, 0, 0, null);

        File file = new File(this.mc.gameDir, "paintings");
        if (!file.exists() && !file.mkdir())
            throw new IOException("Could not create folder");
        final File finalFile = getTimestampedPNGFileForDirectory(file);
        if (!ImageIO.write(output, "png", finalFile))
            throw new IOException("Could not encode image as png!");
        ITextComponent component = new StringTextComponent(finalFile.getName());
        component = component.applyTextStyle(TextFormatting.UNDERLINE).applyTextStyle(style -> style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, finalFile.getAbsolutePath())));
        mc.player.sendStatusMessage(new TranslationTextComponent("mcpaint.gui.saved", component), false);
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        if (updating) return;
        this.color = new Color(this.redSlider.getValueInt(),
                this.greenSlider.getValueInt(),
                this.blueSlider.getValueInt(),
                this.alphaSlider.getValueInt());
    }

    private ResourceLocation getResourceLocation(TextureAtlasSprite p_184396_1_) {
        ResourceLocation resourcelocation = p_184396_1_.getName();
        return new ResourceLocation(resourcelocation.getNamespace(), String.format("textures/%s%s", resourcelocation.getPath(), ".png"));
    }

    //See ScreenShotHelper
    private static File getTimestampedPNGFileForDirectory(File gameDirectory) {
        String s = DATE_FORMAT.format(new Date());
        int i = 1;

        while(true) {
            File file1 = new File(gameDirectory, s + (i == 1 ? "" : "_" + i) + ".png");
            if (!file1.exists()) {
                return file1;
            }

            ++i;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
