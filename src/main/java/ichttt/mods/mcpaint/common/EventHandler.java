package ichttt.mods.mcpaint.common;

import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.CapabilityProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
    public static BlockEntityType<TileEntityCanvas> CANVAS_TE = getNull();

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
