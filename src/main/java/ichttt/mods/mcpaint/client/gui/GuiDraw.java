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
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.resources.IResource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GuiDraw extends GuiScreen implements GuiSlider.ISlider {
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
    private GuiButton undo, redo;
    private GuiButton lessSize, moreSize;
    private GuiSlider redSlider, blueSlider, greenSlider, alphaSlider;
    private boolean hasSizeWindow;
    private boolean noRevert = false;

    public GuiDraw(IPaintable canvas, List<IPaintable> prevImages, BlockPos pos, EnumFacing facing, IBlockState state) {
        Objects.requireNonNull(canvas, "Canvas is null");
        Preconditions.checkArgument(canvas.hasPaintData(), "No data in canvas");
        this.pos = pos;
        this.facing = facing;
        this.state = state;
        this.currentState = new PictureState(canvas);
        for (IPaintable paint : prevImages)
            this.statesForUndo.add(new PictureState(paint));
        hadPaint = true;
    }

    protected GuiDraw(byte scaleFactor, BlockPos pos, EnumFacing facing, IBlockState state) {
        this.pos = Objects.requireNonNull(pos);
        this.facing = facing;
        this.state = state;
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

        GuiButton saveImage = new GuiButton(-12, this.guiLeft + xSize, this.guiTop + 96, 80, 20, "Export") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                try {
                    saveImage(true);
                } catch (IOException e) {
                    MCPaint.LOGGER.error("Could not save image!", e);
                    mc.player.sendStatusMessage(new TextComponentString("Failed to save file!"), true);
                    mc.player.sendStatusMessage(new TextComponentString("Failed to save file!"), false);
                }
                super.onClick(mouseX, mouseY);
            }
        };
        GuiButton rotateRight = new GuiButton(-11, this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5 + 22 + 22 + 22, 36, 20, I18n.format("mcpaint.gui.rright")) {
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
                super.onClick(mouseX, mouseY);
            }
        };
        GuiButton rotateLeft = new GuiButton(-10, this.guiLeft - toolXSize + 3, this.guiTop + 5 + 22 + 22 + 22, 36, 20, I18n.format("mcpaint.gui.rleft")) {
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
                super.onClick(mouseX, mouseY);
            }
        };
        this.redo = new GuiButton(-9, this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5 + 22 + 22, 36, 20, I18n.format("mcpaint.gui.redo")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                redo();
                super.onClick(mouseX, mouseY);
            }
        };
        this.undo = new GuiButton(-8, this.guiLeft - toolXSize + 3, this.guiTop + 5 + 22 + 22, 36, 20, I18n.format("mcpaint.gui.undo")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                undo();
                super.onClick(mouseX, mouseY);
            }
        };
        GuiButton pickColor = new GuiButtonTextToggle(-7, this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5 + 22, 36, 20, EnumDrawType.PICK_COLOR);
        GuiButton erase = new GuiButtonTextToggle(-6, this.guiLeft - toolXSize + 3, this.guiTop + 5 + 22, 36, 20, EnumDrawType.ERASER);
        GuiButton fill = new GuiButtonTextToggle(-5, this.guiLeft - toolXSize + 2 + 39, this.guiTop + 5, 36, 20, EnumDrawType.FILL);
        GuiButton pencil = new GuiButtonTextToggle(-4, this.guiLeft - toolXSize + 3, this.guiTop + 5, 36, 20,  EnumDrawType.PENCIL);
        this.moreSize = new GuiButton(-3, this.guiLeft - toolXSize + 3 + 55, this.guiTop + toolYSize + 5, 20, 20, ">") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                GuiDraw.this.toolSize++;
                handleSizeChanged();
            }
        };
        this.lessSize = new GuiButton(-2, this.guiLeft - toolXSize + 3, this.guiTop + toolYSize + 5, 20, 20, "<") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                GuiDraw.this.toolSize--;
                handleSizeChanged();
            }
        };
        GuiButton done = new GuiButton(-1, this.guiLeft + (xSize / 2) - (200 / 2), this.guiTop + ySize + 20, 200, 20, I18n.format("gui.done")) {
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

        this.redSlider = new GuiSlider(100, this.guiLeft + xSize + 3, this.guiTop + 4, I18n.format("mcpaint.gui.red"), 0, 255, 0, this) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                handleButton(this);
                super.onClick(mouseX, mouseY);
            }
        };
        this.redSlider.width = 74;
        this.greenSlider = new GuiSlider(101, this.guiLeft + xSize + 3, this.guiTop + 26, I18n.format("mcpaint.gui.green"), 0, 255, 0, this);
        this.greenSlider.width = 74;
        this.blueSlider = new GuiSlider(102, this.guiLeft + xSize + 3, this.guiTop + 48, I18n.format("mcpaint.gui.blue"), 0, 255, 0, this);
        this.blueSlider.width = 74;
        this.alphaSlider = new GuiSlider(103, this.guiLeft + xSize + 3, this.guiTop + 70, I18n.format("mcpaint.gui.alpha"), 0, 255, 0, this);
        this.alphaSlider.width = 74;

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
        for (GuiButton button : this.buttons) {
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
        List<BakedQuad> quads = this.mc.getBlockRendererDispatcher().getModelForState(state).getQuads(state, facing.getOpposite(), new Random());
        for (BakedQuad quad : quads) {
            TextureAtlasSprite sprite = quad.getSprite();
            GlStateManager.pushMatrix();
            this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
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

    protected void handleButton(GuiButton button) {
        if (button.id >= 0 && button.id < 100) {
            this.color = EnumPaintColor.VALUES[button.id].color;
            this.redSlider.setValue(this.color.getRed());
            this.blueSlider.setValue(this.color.getBlue());
            this.greenSlider.setValue(this.color.getGreen());
            this.alphaSlider.setValue(this.color.getAlpha());
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
                        this.buttons.remove(this.moreSize);
                        this.buttons.remove(this.lessSize);
                    }
                    this.hasSizeWindow = this.activeDrawType.hasSizeRegulator;
                }
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
                this.redSlider.setValue(this.color.getRed());
                this.blueSlider.setValue(this.color.getBlue());
                this.greenSlider.setValue(this.color.getGreen());
                this.alphaSlider.setValue(this.color.getAlpha());
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

    private void saveImage(boolean background) throws IOException {
        BufferedImage paint = new BufferedImage(128 / this.currentState.scaleFactor, 128 / this.currentState.scaleFactor, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < this.currentState.picture.length; x++) {
            for (int y = 0; y < this.currentState.picture[0].length; y++) {
                paint.setRGB(x, y, this.currentState.picture[x][y]);
            }
        }
        BufferedImage output = new BufferedImage(paint.getWidth(), paint.getHeight(), BufferedImage.TYPE_INT_ARGB);

        if (background) {
            List<BakedQuad> quads = this.mc.getBlockRendererDispatcher().getModelForState(state).getQuads(state, facing.getOpposite(), new Random());
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
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select the path to save");
//        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || !f.getName().contains(".") || f.getName().endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "png";
            }
        });
        LookAndFeel old = UIManager.getLookAndFeel();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            MCPaint.LOGGER.warn("Unable to set system look and feel", e);
        }
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file.isDirectory())
                throw new IOException("Expected file, got dir!");
            if (!file.getName().endsWith(".png"))
                file = new File(file.toString() + ".png");
            if (!ImageIO.write(output, "png", file))
                throw new IOException("Could not encode image as png!");
        }
        try {
            UIManager.setLookAndFeel(old);
        } catch (UnsupportedLookAndFeelException e) {
            MCPaint.LOGGER.error("Could not revert look and feel!", e);
        }
    }

//    @Nonnull
//    @Override TODO
//    public String getText(int id,@Nonnull String name, float value) {
//        return name + ":" + Math.round(value);
//    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        this.color = new Color(Math.round(this.redSlider.getValue()),
                Math.round(this.greenSlider.getValue()),
                Math.round(this.blueSlider.getValue()),
                Math.round(this.alphaSlider.getValue()));
    }

    private ResourceLocation getResourceLocation(TextureAtlasSprite p_184396_1_)
    {
        ResourceLocation resourcelocation = p_184396_1_.getName();
        return new ResourceLocation(resourcelocation.getNamespace(), String.format("textures/%s%s", resourcelocation.getPath(), ".png"));
    }
}
