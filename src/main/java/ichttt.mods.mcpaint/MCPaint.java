package ichttt.mods.mcpaint;

import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.networking.MessageDrawComplete;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

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

    public static CreativeTabs creativeTab;
    public static SimpleNetworkWrapper NETWORKING = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("{} version {} starting", NAME, VERSION);
        creativeTab = new CreativeTabs(MCPaint.MODID) {
            @Nonnull
            @Override
            public ItemStack createIcon() {
                return new ItemStack(Items.BREAD); //TODO
            }
        };

        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        NETWORKING.registerMessage(MessageDrawComplete.Handler.class, MessageDrawComplete.class, 1, Side.SERVER);
        proxy.preInit();
        checkEarlyExit();
    }


    private static void checkEarlyExit() {
        if (FMLCommonHandler.instance().isDisplayCloseRequested()) { //another early exit (forge only covers stage transition)
            LOGGER.info("Early exit requested by user - terminating minecraft");
            FMLCommonHandler.instance().exitJava(0, false);
        }
    }
}
