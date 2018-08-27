package ichttt.mods.mcpaint.client;

import ichttt.mods.mcpaint.IProxy;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.gui.GuiDraw;
import ichttt.mods.mcpaint.client.gui.GuiSetupCanvas;
import ichttt.mods.mcpaint.client.render.TESRCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy implements IProxy {
    @Override
    public void preInit() {
        MCPaint.LOGGER.debug("Loading ClientProxy");
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCanvas.class, new TESRCanvas());
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
    }

    @Override
    public void showGuiDraw(IPaintable canvas, BlockPos pos, EnumFacing facing, IBlockState state) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiDraw(canvas, pos, facing, state));
    }

    @Override
    public void showGuiDraw(BlockPos pos, EnumFacing facing, IBlockState state) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiSetupCanvas(pos, facing, state, 8, 8));
    }

    public static int[][] copyOf(int[][] array) {
        int[][] copy = new int[array.length][];
        for (int i = 0; i < array.length; i++) {
            copy[i] = array[i].clone();
        }
        return copy;
    }
}
