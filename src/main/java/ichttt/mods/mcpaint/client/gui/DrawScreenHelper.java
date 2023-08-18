package ichttt.mods.mcpaint.client.gui;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.gui.drawutil.EnumDrawType;
import ichttt.mods.mcpaint.client.gui.drawutil.PictureState;
import ichttt.mods.mcpaint.client.render.RenderUtil;
import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import ichttt.mods.mcpaint.networking.MessageDrawAbort;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class DrawScreenHelper {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    public static final int ZERO_ALPHA = new Color(255, 255,255, 0).getRGB();
    public final BlockPos pos;
    public final Direction facing;
    public final BlockState state;
    public final BakedModel model;
    public final LinkedList<PictureState> statesForUndo = new LinkedList<>();
    public final LinkedList<PictureState> statesForRedo = new LinkedList<>();
    public final boolean hadPaint;
    private final IDrawGuiCallback callback;

    public Color color = Color.BLACK;
    public int toolSize = 1;
    private PictureState paintingState;
    private PictureState currentState;
    private boolean clickStartedInPicture = false;

    private EnumDrawType activeDrawType = EnumDrawType.PENCIL;

    private boolean hasSizeWindow;
    private boolean noRevert = false;
    private boolean updating = false;

    public DrawScreenHelper(IPaintable canvas, List<IPaintable> prevImages, BlockPos pos, Direction facing, BlockState state, IDrawGuiCallback callback) {
        this.callback = callback;
        Objects.requireNonNull(canvas, "Canvas is null");
        Preconditions.checkArgument(canvas.hasPaintData(), "No data in canvas");
        this.pos = pos;
        this.facing = facing;
        this.state = state;
        this.model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        this.currentState = new PictureState(canvas);
        for (IPaintable paint : prevImages)
            this.statesForUndo.add(new PictureState(paint));
        hadPaint = true;
    }

    protected DrawScreenHelper(byte scaleFactor, BlockPos pos, Direction facing, BlockState state, IDrawGuiCallback callback) {
        this.pos = Objects.requireNonNull(pos);
        this.facing = facing;
        this.state = state;
        this.model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        this.callback = callback;
        int[][] picture = new int[128 / scaleFactor][128 / scaleFactor];
        for (int[] tileArray : picture)
            Arrays.fill(tileArray, ZERO_ALPHA);
        this.currentState = new PictureState(picture, scaleFactor);
        hadPaint = false;
    }

    private void init() {
        this.hasSizeWindow = false;
    }

    public void saveImage() {
        try {
            saveImage(true);
        } catch (IOException e) {
            MCPaint.LOGGER.error("Could not save image!", e);
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.player.displayClientMessage(Component.literal("Failed to save file!"), true);
            minecraft.player.displayClientMessage(Component.literal("Failed to save file!"), false);
        }
    }

    public void rotateRight() {
        int[][] newData = new int[this.currentState.picture.length][this.currentState.picture[0].length];
        for (int x = 0; x < this.currentState.picture.length; x++) {
            int[] yData = this.currentState.picture[x];
            for (int y = 0; y < yData.length; y++) {
                newData[yData.length - y - 1][x] = yData[y];
            }
        }
        newPictureState(new PictureState(newData, this.currentState.scaleFactor));
    }

    public void rotateLeft() {
        int[][] newData = new int[this.currentState.picture.length][this.currentState.picture[0].length];
        for (int x = 0; x < this.currentState.picture.length; x++) {
            int[] yData = this.currentState.picture[x];
            for (int y = 0; y < yData.length; y++) {
                newData[y][this.currentState.picture.length - x - 1] = yData[y];
            }
        }
        newPictureState(new PictureState(newData, this.currentState.scaleFactor));
    }

    public void saveAndClose() {
        Minecraft mc = Minecraft.getInstance();
        if (Arrays.stream(this.currentState.picture).anyMatch(ints -> Arrays.stream(ints).anyMatch(value -> value != DrawScreenHelper.ZERO_ALPHA))) {
            this.noRevert = true;
            MCPaintUtil.uploadPictureToServer(mc.level.getBlockEntity(this.pos), this.facing, this.currentState.scaleFactor, this.currentState.picture, false);
        } else if (hadPaint) {
            MCPaintUtil.uploadPictureToServer(mc.level.getBlockEntity(this.pos), this.facing, this.currentState.scaleFactor, this.currentState.picture, true);
        }
        mc.setScreen(null);
    }

    public void onClose() {
        if (!noRevert) {
            MCPaint.NETWORKING.sendToServer(new MessageDrawAbort(pos));
        }
    }

    public void renderBackgroundBlock(PoseStack stack, int startLeft, int startTop) {
        Minecraft mc = Minecraft.getInstance();
        RandomSource randomSource = RandomSource.create();
        ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, randomSource, ModelData.EMPTY);
        for (RenderType renderType : renderTypes) {
            List<BakedQuad> quads = model.getQuads(state, facing.getOpposite(), randomSource, ModelData.EMPTY, renderType);
            for (BakedQuad quad : quads) {
                TextureAtlasSprite sprite = quad.getSprite();
                stack.pushPose();
                RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
                //See BlockModelRenderer
                if (quad.isTinted()) {
                    int color = mc.getBlockColors().getColor(state, mc.level, pos, quad.getTintIndex());
                    float red = (float) (color >> 16 & 255) / 255.0F;
                    float green = (float) (color >> 8 & 255) / 255.0F;
                    float blue = (float) (color & 255) / 255.0F;
                    RenderSystem.setShaderColor(red, green, blue, 1F);
                }
                GuiComponent.blit(stack, startLeft, startTop, -1, 128, 128, sprite);
                stack.popPose();
            }
        }
    }

    public void renderImage(PoseStack stack, int startLeft, int startTop, int[][] toDraw) {
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        //draw picture
        //we batch everything together to increase the performance
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        RenderUtil.renderInGui(stack.last().pose(), startLeft, startTop, this.currentState.scaleFactor, buffer, toDraw);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        tessellator.end();
        RenderSystem.disableBlend();
    }

    public boolean switchToolButton(EnumDrawType newDrawType) {
        this.activeDrawType = newDrawType;
        boolean hadSizeWindow = this.hasSizeWindow;
        this.hasSizeWindow = this.activeDrawType.hasSizeRegulator;
        return hadSizeWindow;
    }

    public boolean handleMouseClick(double mouseX, double mouseY, int mouseButton) {
        if (handleMouse(mouseX, mouseY, mouseButton)) {
            this.clickStartedInPicture = true;
            return true;
        }
        return false;
    }

    public boolean handleMouseDragged(double mouseX, double mouseY, int mouseButton) {
        return this.clickStartedInPicture && handleMouse(mouseX, mouseY, mouseButton);
    }

    public void handleMouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.clickStartedInPicture = false;
        if (this.paintingState != null) {
            this.newPictureState(this.paintingState);
            this.paintingState = null;
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
        callback.updateUndoRedoButton(!this.statesForUndo.isEmpty(), !this.statesForRedo.isEmpty());
    }

    public void undo() {
        if (this.statesForUndo.size() > 0) {
            this.statesForRedo.add(this.currentState);
            this.currentState = this.statesForUndo.removeLast();
        }
        callback.updateUndoRedoButton(!this.statesForUndo.isEmpty(), !this.statesForRedo.isEmpty());
    }

    public void redo() {
        if (this.statesForRedo.size() > 0) {
            this.statesForUndo.add(this.currentState);
            this.currentState = this.statesForRedo.removeLast();
        }
        callback.updateUndoRedoButton(!this.statesForUndo.isEmpty(), !this.statesForRedo.isEmpty());
    }


    private boolean handleMouse(double mouseXD, double mouseYD, int mouseButton) {
        if (mouseButton != 0) return false;
        int mouseX = (int) Math.round(mouseXD);
        int mouseY = (int) Math.round(mouseYD);
        int offsetMouseX = callback.offsetMouseX(mouseX);//mouseX - this.guiLeft - PICTURE_START_LEFT;
        int offsetMouseY = callback.offsetMouseY(mouseY);//mouseY - this.guiTop - PICTURE_START_TOP;
        if (callback.isInPicture(offsetMouseX, offsetMouseY)) {
            int pixelPosX = offsetMouseX / this.currentState.scaleFactor;
            int pixelPosY = offsetMouseY / this.currentState.scaleFactor;
            if (this.paintingState == null)
                this.paintingState = new PictureState(this.currentState);
            if (pixelPosX < this.paintingState.picture.length && pixelPosY < this.paintingState.picture.length && this.color != null) {
                this.color = this.activeDrawType.draw(this.paintingState.picture, this.color, pixelPosX, pixelPosY, this.toolSize);
                callback.updateSliders();
                return true;
            }
        }
        return false;
    }


    private ResourceLocation getResourceLocation(TextureAtlasSprite p_184396_1_) {
        ResourceLocation resourcelocation = p_184396_1_.atlasLocation();
        return new ResourceLocation(resourcelocation.getNamespace(), String.format("textures/%s%s", resourcelocation.getPath(), ".png"));
    }

    private void saveImage(boolean background) throws IOException {
        Minecraft minecraft = Minecraft.getInstance();
        BufferedImage paint = new BufferedImage(128 / this.currentState.scaleFactor, 128 / this.currentState.scaleFactor, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < this.currentState.picture.length; x++) {
            for (int y = 0; y < this.currentState.picture[0].length; y++) {
                paint.setRGB(x, y, this.currentState.picture[x][y]);
            }
        }
        BufferedImage output = new BufferedImage(paint.getWidth(), paint.getHeight(), BufferedImage.TYPE_INT_ARGB);

        if (background) {
            RandomSource randomSource = RandomSource.create();
            ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, randomSource, ModelData.EMPTY);
            for (RenderType renderType : renderTypes) {
                List<BakedQuad> quads = model.getQuads(state, facing.getOpposite(), randomSource, ModelData.EMPTY, renderType);
                for (BakedQuad quad : quads) {
                    TextureAtlasSprite sprite = quad.getSprite();
                    try (InputStream stream = minecraft.getResourceManager().open(getResourceLocation(sprite))) {
                        Image image = ImageIO.read(stream);
                        if (quad.isTinted()) {
                            int color = minecraft.getBlockColors().getColor(state, minecraft.level, pos, quad.getTintIndex());
                            float red = (float) (color >> 16 & 255) / 255.0F;
                            float green = (float) (color >> 8 & 255) / 255.0F;
                            float blue = (float) (color & 255) / 255.0F;
                            BufferedImage asBufferedImage = (BufferedImage) image;
                            for (int x = 0; x < asBufferedImage.getWidth(); x++) {
                                for (int y = 0; y < asBufferedImage.getHeight(); y++) {
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
        }
        output.getGraphics().drawImage(paint, 0, 0, null);

        File file = new File(minecraft.gameDirectory, "paintings");
        if (!file.exists() && !file.mkdir())
            throw new IOException("Could not create folder");
        final File finalFile = getTimestampedPNGFileForDirectory(file);
        if (!ImageIO.write(output, "png", finalFile))
            throw new IOException("Could not encode image as png!");
        MutableComponent component = Component.literal(finalFile.getName());
        component = component.withStyle(ChatFormatting.UNDERLINE).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, finalFile.getAbsolutePath())));
        minecraft.player.displayClientMessage(Component.translatable("mcpaint.gui.saved", component), false);
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

    public EnumDrawType activeDrawType() {
        return activeDrawType;
    }

    public boolean hasSizeWindow() {
        return hasSizeWindow;
    }

    public PictureState currentState() {
        return currentState;
    }

    public int[][] getBasePicture() {
        return this.paintingState == null ? this.currentState.picture : this.paintingState.picture;
    }
}
