package ichttt.mods.mcpaint.common;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import ichttt.mods.mcpaint.networking.MessageClearSide;
import ichttt.mods.mcpaint.networking.MessagePaintData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.network.NetworkEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MCPaintUtil {
    public static boolean isPosInvalid(NetHandlerPlayServer handler, BlockPos pos) {
        if (!handler.player.world.isBlockLoaded(pos)) {
            MCPaint.LOGGER.warn("Player" + handler.player.getName() + " is trying to write to unloaded block");
            handler.disconnect(new TextComponentString("Trying to write to unloaded block"));
            return true;
        }

        if (handler.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > (Math.round(handler.player.getAttribute(EntityPlayer.REACH_DISTANCE).getValue()) + 5)) {
            MCPaint.LOGGER.warn("Player" + handler.player.getName() + " is writing to out of reach block!");
            return true;
        }
        return false;
    }

    public static int[][] copyOf(int[][] array) {
        int[][] copy = new int[array.length][];
        for (int i = 0; i < array.length; i++) {
            copy[i] = array[i].clone();
        }
        return copy;
    }

    public static boolean[][] copyOf(boolean[][] array) {
        boolean[][] copy = new boolean[array.length][];
        for (int i = 0; i < array.length; i++) {
            copy[i] = array[i].clone();
        }
        return copy;
    }

    public static void uploadPictureToServer(TileEntity te, EnumFacing facing, byte scaleFactor, int[][] picture, boolean clear) {
        if (!(te instanceof TileEntityCanvas)) {
            MCPaint.LOGGER.error("Could not set paint! Found block " + te.getType());
            Minecraft.getInstance().player.sendStatusMessage(new TextComponentString("Could not set paint!"), true);
            return;
        }
        TileEntityCanvas canvas = (TileEntityCanvas) te;
        if (clear) {
            MCPaint.NETWORKING.sendToServer(new MessageClearSide(te.getPos(), facing));
            canvas.removePaint(facing);
        } else {
            MessagePaintData.createAndSend(te.getPos(), facing, scaleFactor, picture, MCPaint.NETWORKING::sendToServer);
            IPaintable paintable = canvas.getPaintFor(facing);
            paintable.setData(scaleFactor, picture, canvas, facing);
        }
    }


    private static final Method getNetworkManager;

    static {
        Method m;
        ReflectiveOperationException ex = null;
        try {
            m = NetworkEvent.Context.class.getDeclaredMethod("getNetworkManager");
            m.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            m = null;
            ex = e;
        }
        getNetworkManager = m;
        if (ex != null)
            throw new RuntimeException(ex);
    }

    public static<T extends INetHandler> T getNetHandler(NetworkEvent.Context context) {
        try {
            //noinspection unchecked
            return (T) (((NetworkManager) getNetworkManager.invoke(context)).getNetHandler());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
