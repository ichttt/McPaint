package ichttt.mods.mcpaint.client;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.client.render.TEISRStamp;
import ichttt.mods.mcpaint.client.render.batch.RenderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ClientEventHandler {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
//        ModelLoader.setCustomModelResourceLocation(EventHandler.BRUSH, 0, new ModelResourceLocation(new ResourceLocation(MCPaint.MODID, "brush"), "inventory"));
//        ModelLoader.setCustomModelResourceLocation(EventHandler.STAMP, 0, new ModelResourceLocation(new ResourceLocation(MCPaint.MODID, "stamp"), "inventory"));
//        EventHandler.STAMP.setTileEntityItemStackRenderer(TEISRStamp.INSTANCE);
        Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(MCPaint.MODID, "stamp")), "Did not find stamp").addPropertyOverride(new ResourceLocation(MCPaint.MODID, "shift"), TEISRStamp.INSTANCE);
    }

    private static final IStateMapper MAPPER = new StateMapperBase() {
        @Nonnull
        @Override
        protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
            state = EventHandler.CANVAS_GROUND.getStateFromMeta(state.getBlock().getMetaFromState(state));
            return new ModelResourceLocation(Block.REGISTRY.getNameForObject(state.getBlock()), this.getPropertyString(state.getProperties()));
        }
    };

    private static void map(Block block) {
        ModelLoader.setCustomStateMapper(block, MAPPER);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote()) {
            RenderCache.clear();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().world != null && Minecraft.getInstance().world.getGameTime() % 200 == 0) {
            RenderCache.scheduleCleanup();
        }
    }
}
