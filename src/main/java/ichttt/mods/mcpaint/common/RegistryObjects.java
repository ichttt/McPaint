package ichttt.mods.mcpaint.common;

import com.google.common.collect.ImmutableMap;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.INameMappingService;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.item.ItemBrush;
import ichttt.mods.mcpaint.common.item.ItemStamp;
import ichttt.mods.mcpaint.common.material.MaterialDefinitions;
import ichttt.mods.mcpaint.common.material.MaterialFileGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class RegistryObjects {
    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MCPaint.MODID);
    private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, MCPaint.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MCPaint.MODID);

    public static final RegistryObject<Item> BRUSH;
    public static final RegistryObject<Item> STAMP;

    public static final Map<Material, RegistryObject<Block>> CANVAS_BLOCKS;
    
    public static final RegistryObject<BlockEntityType<TileEntityCanvas>> CANVAS_BE;

    static {
        // ITEMS
        BRUSH = ITEM_REGISTER.register("brush", ItemBrush::new);
        STAMP = ITEM_REGISTER.register("stamp", ItemStamp::new);

        // BLOCKS
        CANVAS_BLOCKS = generateCanvasBlockEntries();


        // Block Entity Types
        CANVAS_BE = BLOCK_ENTITY_TYPE_REGISTER.register("canvas_te", () -> BlockEntityType.Builder.of(TileEntityCanvas::new, CANVAS_BLOCKS.values().stream().map(Supplier::get).toArray(Block[]::new)).build(null));
    }

    private static Map<Material, RegistryObject<Block>> generateCanvasBlockEntries() {
        ImmutableMap.Builder<Material, RegistryObject<Block>> builder = ImmutableMap.builder();

        MaterialDefinitions definitions;
        if (!FMLLoader.isProduction()) {
            MCPaint.LOGGER.info("Not running in prod mode. Regenerating block mappings");

            MaterialDefinitions oldDefinitions = null;
            try {
                oldDefinitions = new MaterialDefinitions(RegistryObjects.class.getResourceAsStream("/mcpaint_materials.txt"));
            } catch (IOException | RuntimeException e) {
                MCPaint.LOGGER.warn("Old definitions not present!");
            }
            try {
                definitions = MaterialFileGenerator.generateMaterialFile(oldDefinitions);
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate material file!", e);
            }
        } else {
            try {
                definitions = new MaterialDefinitions(RegistryObjects.class.getResourceAsStream("/mcpaint_materials.txt"));
            } catch (IOException | RuntimeException e) {
                MCPaint.LOGGER.fatal("Failed to read materials list!", e);
                throw new RuntimeException("Failed to read materials list!", e);
            }
        }

        for (Map.Entry<String, String> entry : definitions.getSrg2Mcp().entrySet()) {
            String keyToUse = entry.getKey();
            if (!FMLLoader.isProduction()) {
                BiFunction<INameMappingService.Domain, String, String> mappingFunc = Launcher.INSTANCE.environment().findNameMapping("srg").orElseThrow(() -> new RuntimeException("Missing srg provider"));
                keyToUse = mappingFunc.apply(INameMappingService.Domain.FIELD, keyToUse);
            }
            try {
                Field declaredField = Material.class.getDeclaredField(keyToUse);
                Material material = (Material) declaredField.get(null);
                builder.put(material, BLOCK_REGISTER.register("canvas_" + entry.getValue(), () -> new BlockCanvas(material)));
            } catch (ReflectiveOperationException | ClassCastException e) {
                MCPaint.LOGGER.error("Failed to lookup material {} (srg:{}, regname:{})", keyToUse, entry.getKey(), entry.getValue(), e);
            }
        }
        ImmutableMap<Material, RegistryObject<Block>> build = builder.build();
        MCPaint.LOGGER.debug("Found {} materials", build);
        return build;
    }

    public static void registerToBus(IEventBus bus) {
        ITEM_REGISTER.register(bus);
        BLOCK_REGISTER.register(bus);
        BLOCK_ENTITY_TYPE_REGISTER.register(bus);
    }
}
