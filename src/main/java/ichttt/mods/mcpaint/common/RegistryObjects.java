package ichttt.mods.mcpaint.common;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.item.ItemBrush;
import ichttt.mods.mcpaint.common.item.ItemStamp;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RegistryObjects {
    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MCPaint.MODID);
    private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, MCPaint.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MCPaint.MODID);

    public static final RegistryObject<Item> BRUSH;
    public static final RegistryObject<Item> STAMP;

    public static final RegistryObject<BlockCanvas> CANVAS_BLOCK;
    
    public static final RegistryObject<BlockEntityType<TileEntityCanvas>> CANVAS_BE;

    static {
        // ITEMS
        BRUSH = ITEM_REGISTER.register("brush", ItemBrush::new);
        STAMP = ITEM_REGISTER.register("stamp", ItemStamp::new);

        // BLOCKS
        CANVAS_BLOCK = BLOCK_REGISTER.register("canvas", BlockCanvas::new);


        // Block Entity Types
        CANVAS_BE = BLOCK_ENTITY_TYPE_REGISTER.register("canvas_te", () -> BlockEntityType.Builder.of(TileEntityCanvas::new, CANVAS_BLOCK.get()).build(null));
    }

    public static void registerToBus(IEventBus bus) {
        ITEM_REGISTER.register(bus);
        BLOCK_REGISTER.register(bus);
        BLOCK_ENTITY_TYPE_REGISTER.register(bus);
    }
}
