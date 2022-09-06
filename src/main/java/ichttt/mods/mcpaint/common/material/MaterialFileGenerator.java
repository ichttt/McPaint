package ichttt.mods.mcpaint.common.material;

import ichttt.mods.mcpaint.MCPaint;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.loading.FMLLoader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class MaterialFileGenerator {

    public static MaterialDefinitions generateMaterialFile(MaterialDefinitions originalDefinitions) throws IOException, ReflectiveOperationException, InterruptedException {
        String mcVersion = FMLLoader.versionInfo().mcVersion();
        if (originalDefinitions != null) {
            if (mcVersion.equals(originalDefinitions.getVersion())) {
                MCPaint.LOGGER.info("Material definitions do not differ in versions! Using old definitions");
                return originalDefinitions;
            } else {
                MCPaint.LOGGER.info("Material definitions differ in version!");
            }
        }

        Map<String, String> srgToOfficial = ReverseMappingResolver.loadMappingFor(Material.class, MaterialFileGenerator::getNameForRegistry);

        MaterialDefinitions newDef = new MaterialDefinitions(mcVersion, srgToOfficial);
        writeDefinition(newDef);
        if (originalDefinitions != null) {
            if (!newDef.getSrg2Mcp().equals(originalDefinitions.getSrg2Mcp())) {
                MCPaint.LOGGER.warn("Material definitions differ! Manually audit the changes!");
                System.exit(-999);
                throw new RuntimeException("Cant get here");
            }
        }
        return newDef;
    }

    private static String getNameForRegistry(String name) {
        // legacy names, keep them static for compat reasons
        if (name.equalsIgnoreCase("stone"))
            return "rock";
        if (name.equalsIgnoreCase("dirt"))
            return "ground";
        return name.toLowerCase(Locale.ROOT);
    }

    private static void writeDefinition(MaterialDefinitions def) throws IOException {
        Path path = Paths.get("../src/main/resources/");
        if (!Files.exists(path)) {
            MCPaint.LOGGER.fatal("Failed to write file! Resource path does not exist!");
            System.exit(-888);
            throw new RuntimeException("Cant get here");
        }
        Path resolve = path.resolve("mcpaint_materials.txt");
        Set<Map.Entry<String, String>> entries = def.getSrg2Mcp().entrySet();
        List<Map.Entry<String, String>> sortedEntries = new ArrayList<>(entries);
        sortedEntries.sort(Map.Entry.comparingByValue());

        try (BufferedWriter writer = Files.newBufferedWriter(resolve, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            writer.write(def.getVersion());
            writer.newLine();
            for (Map.Entry<String, String> entry : sortedEntries) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        }
        Path baseFile = Paths.get("../blockstate_canvas.json");
        for (Map.Entry<String, String> sortedEntry : sortedEntries) {
            Files.copy(baseFile, path.resolve("assets/mcpaint/blockstates/canvas_" + sortedEntry.getValue() + ".json"), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
