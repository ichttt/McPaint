package ichttt.mods.mcpaint.client;

import ichttt.mods.mcpaint.IProxy;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.gui.GuiDraw;
import ichttt.mods.mcpaint.client.gui.GuiSetupCanvas;
import ichttt.mods.mcpaint.client.render.TESRCanvas;
import ichttt.mods.mcpaint.client.render.batch.RenderCache;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy implements IProxy {
    @Override
    public void preInit() {
        MCPaint.LOGGER.debug("Loading ClientProxy");
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCanvas.class, new TESRCanvas());
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
        if (FMLClientHandler.instance().hasOptifine()) {
            MCPaint.LOGGER.warn("OPTIFINE PRESENT");
            MCPaint.LOGGER.warn("Some things may not render correctly");
            MCPaint.LOGGER.warn("Especially shaders are known to cause issues");
            MCPaint.LOGGER.warn("Please DO NOT report any render errors with optifine to MCPaint, but report them to Optifine instead");
        }
    }

    @Override
    public void showGuiDraw(IPaintable canvas, BlockPos pos, EnumFacing facing, IBlockState state) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiDraw(canvas, pos, facing, state));
    }

    @Override
    public void showGuiDraw(BlockPos pos, EnumFacing facing, IBlockState state) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiSetupCanvas(pos, facing, state, 8, 8));
    }

    @Override
    public void onConfigReload() {
        RenderCache.onConfigReload();
    }
}
