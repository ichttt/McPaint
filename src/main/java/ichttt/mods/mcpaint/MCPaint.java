package ichttt.mods.mcpaint;

import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.networking.MessageDrawAbort;
import ichttt.mods.mcpaint.networking.MessagePaintData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = MCPaint.MODID,
        name = MCPaint.NAME,
        version = MCPaint.VERSION,
        acceptedMinecraftVersions = "[1.12.2,1.13)",
        certificateFingerprint = "7904c4e13947c8a616c5f39b26bdeba796500722")
public class MCPaint {
    public static final String MODID = "mcpaint";
    public static final String NAME = "MC Paint";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "ichttt.mods.mcpaint.client.ClientProxy", serverSide = "ichttt.mods.mcpaint.server.ServerProxy")
    public static IProxy proxy;

    public static final SimpleNetworkWrapper NETWORKING = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("{} version {} starting", NAME, VERSION);
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        proxy.preInit();
        checkEarlyExit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NETWORKING.registerMessage(MessagePaintData.Handler.class, MessagePaintData.class, 1, Side.SERVER);
        NETWORKING.registerMessage(MessageDrawAbort.Handler.class, MessageDrawAbort.class, 2, Side.SERVER);
        CapabilityPaintable.register();
        checkEarlyExit();
    }


    private static void checkEarlyExit() {
        if (FMLCommonHandler.instance().isDisplayCloseRequested()) { //another early exit (forge only covers stage transition)
            LOGGER.info("Early exit requested by user - terminating minecraft");
            FMLCommonHandler.instance().exitJava(0, false);
        }
    }

    public static boolean isPosInvalid(NetHandlerPlayServer handler, BlockPos pos) {
        if (!handler.player.world.isBlockLoaded(pos)) {
            handler.disconnect(new TextComponentString("Trying to write to unloaded block"));
            return true;
        }

        if (handler.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > (Math.round(handler.player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue()) + 5)) {
            MCPaint.LOGGER.warn("Player" + handler.player.getName() + " is writing to out of reach block!");
            return true;
        }
        return false;
    }
}
