package ichttt.mods.mcpaint.client.gui;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.networking.MessageDrawAbort;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.*;

public class SetupCanvasScreen extends Screen {
    private static final ResourceLocation BACKGROUND = DrawScreen.BACKGROUND;
    private static final int yOffset = 166;
    private static final int xSize = 106;
    private static final int ySize = 79;
    private static final int MAX_MULTIPLIER = 16;

    private final BlockPos pos;
    private final Direction facing;
    private final BlockState state;
    private final int baseX;
    private final int baseY;

    private boolean handled = false;
    private Button moreSize;
    private Button lessSize;
    private int currentMulti;
    private int guiLeft;
    private int guiTop;

    public SetupCanvasScreen(BlockPos pos, Direction facing, BlockState state, int baseX, int baseY) {
        super(Component.translatable("mcpaint.drawsetup"));
        this.pos = pos;
        this.facing = facing;
        this.state = state;
        this.baseX = baseX;
        this.baseY = baseY;
        this.currentMulti = 2;
    }

    @Override
    public void init() {
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;
        this.lessSize = Button.builder(Component.literal("<"), button -> {
            currentMulti /= 2;
            handleSizeChanged();
        }).bounds(this.guiLeft + 5, this.guiTop + 26, 20, 20).build();
        this.moreSize = Button.builder(Component.literal(">"), button -> {
            SetupCanvasScreen.this.currentMulti *= 2;
            handleSizeChanged();
        }).bounds(this.guiLeft + 83, this.guiTop + 26, 20, 20).build();

        Button doneBtn = Button.builder(Component.translatable("gui.done"), button -> {
            handled = true;
            minecraft.setScreen(null);
            minecraft.setScreen(new DrawScreen((byte) (16 / SetupCanvasScreen.this.currentMulti), pos, facing, state));
        }).bounds(this.guiLeft + 5, this.guiTop + 56, xSize - 8, 20).build();

        addRenderableWidget(doneBtn);
        addRenderableWidget(this.lessSize);
        addRenderableWidget(this.moreSize);
        handleSizeChanged();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blit(BACKGROUND, this.guiLeft, this.guiTop, 0, yOffset, xSize, ySize);
        guiGraphics.drawCenteredString(minecraft.font, "Resolution:", this.guiLeft + (xSize / 2) + 1, this.guiTop + 8, Color.WHITE.getRGB());
        guiGraphics.drawCenteredString(minecraft.font, this.baseX * this.currentMulti + "x" + this.baseY * this.currentMulti, this.guiLeft + (xSize / 2) + 1, this.guiTop + 32, Color.WHITE.getRGB());
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void removed() {
        if (!handled) {
            MCPaint.NETWORKING.sendToServer(new MessageDrawAbort(pos));
        }
    }

    private void handleSizeChanged() {
        if (this.currentMulti >= MAX_MULTIPLIER) {
            this.currentMulti = MAX_MULTIPLIER;
            this.moreSize.active = false;
        } else {
            this.moreSize.active = true;
        }

        if (this.currentMulti <= 1) {
            this.currentMulti = 1;
            this.lessSize.active = false;
        } else {
            this.lessSize.active = true;
        }
    }
}
