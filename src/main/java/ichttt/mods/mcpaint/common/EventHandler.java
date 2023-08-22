package ichttt.mods.mcpaint.common;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.capability.CapabilityProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import java.util.List;

public class EventHandler {

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() == RegistryObjects.STAMP.get())
            event.addCapability(CapabilityProvider.LOCATION, new CapabilityProvider());
    }

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        List<MissingMappingsEvent.Mapping<Block>> mappings = event.getMappings(ForgeRegistries.BLOCKS.getRegistryKey(), MCPaint.MODID);
        for (MissingMappingsEvent.Mapping<Block> mapping : mappings) {
            if (mapping.getKey().getPath().startsWith("canvas_")) {
                mapping.remap(RegistryObjects.CANVAS_BLOCK.get());
            }
        }
    }
}
