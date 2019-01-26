package ichttt.mods.mcpaint.common;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.capability.CapabilityProvider;
import ichttt.mods.mcpaint.common.item.ItemBrush;
import ichttt.mods.mcpaint.common.item.ItemStamp;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

public class EventHandler {
    @ObjectHolder("mcpaint:brush")
    public static Item BRUSH = getNull();
    @ObjectHolder("mcpaint:stamp")
    public static Item STAMP = getNull();
    @ObjectHolder("mcpaint:canvas_wood")
    public static BlockCanvas CANVAS_WOOD = getNull();
    @ObjectHolder("mcpaint:canvas_rock")
    public static BlockCanvas CANVAS_ROCK = getNull();
    @ObjectHolder("mcpaint:canvas_ground")
    public static BlockCanvas CANVAS_GROUND = getNull();
    @ObjectHolder("mcpaint:canvas_te")
    public static TileEntityType<?> CANVAS_TE = getNull();

    //Avoids warnings of a field being null because it is populated by the ObjectHolder
    //So Nonnull despite returning null
    @SuppressWarnings({"ConstantConditions", "SameReturnValue"})
    @Nonnull
    private static <T> T getNull() {
        return null;
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() == STAMP)
            event.addCapability(CapabilityProvider.LOCATION, new CapabilityProvider());
    }
}
