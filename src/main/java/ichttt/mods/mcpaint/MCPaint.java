package ichttt.mods.mcpaint;

import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.networking.MessageClearSide;
import ichttt.mods.mcpaint.networking.MessageDrawAbort;
import ichttt.mods.mcpaint.networking.MessagePaintData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = MCPaint.MODID,
        name = MCPaint.NAME,
        version = MCPaint.VERSION,
        dependencies = "required:forge@[14.23.3.2658,)",
        acceptedMinecraftVersions = "[1.12.2,1.13)",
        certificateFingerprint = MCPaint.CERTIFICATE)
public class MCPaint {
    public static final String MODID = "mcpaint";
    public static final String NAME = "MC Paint";
    public static final String VERSION = "1.2.2";
    public static final String CERTIFICATE = "7904c4e13947c8a616c5f39b26bdeba796500722";
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
        NETWORKING.registerMessage(MessagePaintData.ServerHandler.class, MessagePaintData.class, 1, Side.SERVER);
        NETWORKING.registerMessage(MessageDrawAbort.Handler.class, MessageDrawAbort.class, 2, Side.SERVER);
        NETWORKING.registerMessage(MessagePaintData.ClientHandler.class, MessagePaintData.class, 3, Side.CLIENT);
        NETWORKING.registerMessage(MessageClearSide.Handler.class, MessageClearSide.class, 4, Side.SERVER);
        CapabilityPaintable.register();
        if (MCPaintConfig.enableOneProbeCompat)
            FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "ichttt.mods.mcpaint.common.OneProbeCompat");
        checkEarlyExit();
    }

    @Mod.EventHandler
    public void fingerprintInvalid(FMLFingerprintViolationEvent event) {
        if (event.getExpectedFingerprint().equals(CERTIFICATE)) {
            LOGGER.error("Missing/Invalid fingerprint for " + event.getSource() + " detected");
            LOGGER.warn("It should be " + CERTIFICATE);
            LOGGER.warn("Found fingerprint(s):");
            if (event.getFingerprints().isEmpty()) {
                LOGGER.warn("NONE");
            } else {
                for (String fingerprint : event.getFingerprints()) {
                    LOGGER.warn("\t" + fingerprint);
                }
            }
        }
    }

    private static void checkEarlyExit() {
        if (FMLCommonHandler.instance().isDisplayCloseRequested()) { //another early exit (forge only covers stage transition)
            LOGGER.info("Early exit requested by user - terminating minecraft");
            FMLCommonHandler.instance().exitJava(0, false);
        }
    }

}
