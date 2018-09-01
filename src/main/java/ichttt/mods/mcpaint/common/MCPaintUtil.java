package ichttt.mods.mcpaint.common;

import ichttt.mods.mcpaint.MCPaint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class MCPaintUtil {
    public static boolean isPosInvalid(NetHandlerPlayServer handler, BlockPos pos) {
        if (!handler.player.world.isBlockLoaded(pos)) {
            MCPaint.LOGGER.warn("Player" + handler.player.getName() + " is trying to write to unloaded block");
            handler.disconnect(new TextComponentString("Trying to write to unloaded block"));
            return true;
        }

        if (handler.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > (Math.round(handler.player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue()) + 5)) {
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
}
