package ichttt.mods.mcpaint.common;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.item.ItemBrush;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Objects;

public class EventHandler {
    public static final Item BRUSH = new ItemBrush().setRegistryName(new ResourceLocation(MCPaint.MODID, "brush"));
    public static final Block CANVAS = new BlockCanvas().setRegistryName(new ResourceLocation(MCPaint.MODID, "canvas"));

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(BRUSH);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(CANVAS);
        GameRegistry.registerTileEntity(TileEntityCanvas.class, new ResourceLocation(MCPaint.MODID, "canvas"));
    }
}
