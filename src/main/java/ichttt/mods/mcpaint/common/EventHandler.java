package ichttt.mods.mcpaint.common;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.capability.CapabilityProvider;
import ichttt.mods.mcpaint.common.item.ItemBrush;
import ichttt.mods.mcpaint.common.item.ItemStamp;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.Objects;

@GameRegistry.ObjectHolder(MCPaint.MODID)
public class EventHandler {
    public static final Item BRUSH = getNull();
    public static final Item STAMP = getNull();
    public static final BlockCanvas CANVAS = getNull();

    //Avoids warnings of a field being null because it is populated by the ObjectHolder
    //So Nonnull despite returning null
    @SuppressWarnings("ConstantConditions")
    @Nonnull
    private static <T> T getNull() {
        return null;
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(new ItemBrush(new ResourceLocation(MCPaint.MODID, "brush")));
        registry.register(new ItemStamp(new ResourceLocation(MCPaint.MODID, "stamp")));
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockCanvas(new ResourceLocation(MCPaint.MODID, "canvas")));
        GameRegistry.registerTileEntity(TileEntityCanvas.class, new ResourceLocation(MCPaint.MODID, "canvas"));
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() == STAMP)
            event.addCapability(CapabilityProvider.LOCATION, new CapabilityProvider());
    }

    @SubscribeEvent
    public static void onConfigChange(ConfigChangedEvent event) {
        if (event.getModID().equals(MCPaint.MODID)) {
            ConfigManager.sync(MCPaint.MODID, Config.Type.INSTANCE);
            MCPaint.proxy.onConfigReload();
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getEntityPlayer().getHeldItem(event.getHand());
        if (event.getEntityPlayer().isSneaking() && stack.getItem() == EventHandler.STAMP) {
            Objects.requireNonNull(stack.getCapability(CapabilityPaintable.PAINTABLE, null)).clear(null, null);
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }
}
