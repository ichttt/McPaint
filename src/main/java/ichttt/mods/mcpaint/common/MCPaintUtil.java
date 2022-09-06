package ichttt.mods.mcpaint.common;

import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import ichttt.mods.mcpaint.networking.MessageClearSide;
import ichttt.mods.mcpaint.networking.MessagePaintData;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class MCPaintUtil {
    public static boolean isPosInvalid(ServerPlayer player, BlockPos pos) {
        if (!player.level.hasChunkAt(pos)) {
            MCPaint.LOGGER.warn("Player" + player.getName() + " is trying to write to unloaded block");
            player.connection.disconnect(Component.literal("Trying to write to unloaded block"));
            return true;
        }

        if (Math.sqrt(player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ())) > (Math.round(player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue()) + 5)) {
            MCPaint.LOGGER.warn("Player" + player.getName() + " is writing to out of reach block!");
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

    public static void uploadPictureToServer(@Nullable BlockEntity te, Direction facing, byte scaleFactor, int[][] picture, boolean clear) {
        if (!(te instanceof TileEntityCanvas)) {
            MCPaint.LOGGER.error("Could not set paint! Found block " + (te == null ? "NONE" : te.getType()));
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Could not set paint!"), true);
            return;
        }
        TileEntityCanvas canvas = (TileEntityCanvas) te;
        if (clear) {
            MCPaint.NETWORKING.sendToServer(new MessageClearSide(te.getBlockPos(), facing));
            canvas.removePaint(facing);
        } else {
            IPaintable paintable = canvas.getPaintFor(facing);
            paintable.setData(scaleFactor, picture, canvas, facing);
            MessagePaintData.createAndSend(te.getBlockPos(), facing, scaleFactor, paintable.getPalette(), picture, MCPaint.NETWORKING::sendToServer);
        }
    }

    @Nonnull
    public static ServerPlayer checkServer(NetworkEvent.Context context) {
        if (context.getDirection() != NetworkDirection.PLAY_TO_SERVER)
            throw new IllegalArgumentException("Wrong side for server packet handler " + context.getDirection());
        context.setPacketHandled(true);
        return Objects.requireNonNull(context.getSender());
    }

    public static void checkClient(NetworkEvent.Context context) {
        if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            throw new IllegalArgumentException("Wrong side for client packet handler: " + context.getDirection());
        context.setPacketHandled(true);
    }

    public static Int2ByteMap buildReversePalette(int[] palette) {
        Int2ByteMap map = new Int2ByteOpenHashMap();
        for (int i = 0; i < palette.length; i++) {
            map.put(palette[i], (byte) i);
        }
        return map;
    }
}
