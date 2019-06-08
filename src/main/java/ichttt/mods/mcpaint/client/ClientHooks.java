package ichttt.mods.mcpaint.client;

import ichttt.mods.mcpaint.client.gui.GuiDraw;
import ichttt.mods.mcpaint.client.gui.GuiSetupCanvas;
import ichttt.mods.mcpaint.client.render.batch.RenderCache;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class ClientHooks {

    public static void showGuiDraw(List<IPaintable> canvasList, BlockPos pos, Direction facing, BlockState state) {
        Minecraft.getInstance().displayGuiScreen(new GuiDraw(canvasList.remove(canvasList.size() - 1), canvasList, pos, facing, state));
    }

    public static void showGuiDraw(BlockPos pos, Direction facing, BlockState state) {
        Minecraft.getInstance().displayGuiScreen(new GuiSetupCanvas(pos, facing, state, 8, 8));
    }

    public static void onConfigReload() {
        RenderCache.onConfigReload();
    }

    public static void invalidateCache(IPaintable paint, TileEntityCanvas canvas, Direction facing) {
        RenderCache.uncache(paint);
        if (canvas != null)
            canvas.invalidateBuffer(facing);
    }
}
