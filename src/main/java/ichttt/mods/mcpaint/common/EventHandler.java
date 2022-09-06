package ichttt.mods.mcpaint.common;

import ichttt.mods.mcpaint.common.capability.CapabilityProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() == RegistryObjects.STAMP.get())
            event.addCapability(CapabilityProvider.LOCATION, new CapabilityProvider());
    }
}
