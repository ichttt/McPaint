package ichttt.mods.mcpaint.networking;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageClearSide {
    private final BlockPos pos;
    private final Direction facing;

    public MessageClearSide(PacketBuffer buffer) {
        this(buffer.readBlockPos(), Direction.from3DDataValue(buffer.readByte()));
    }

    public MessageClearSide(BlockPos pos, Direction facing) {
        this.pos = pos;
        this.facing = facing;
    }

    public void encode(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(facing.get3DDataValue());
    }

    public static class ServerHandler {
        public static void onMessage(MessageClearSide message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> MessagePaintData.ServerHandler.INSTANCE.handleSide(context, message.pos, message.facing, (byte) 0, null));
            context.setPacketHandled(true);
        }
    }

    public static class ClientHandler {
        public static void onMessage(MessageClearSide message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> MessagePaintData.ClientHandler.INSTANCE.handleSide(context, message.pos, message.facing, (byte) 0, null));
            context.setPacketHandled(true);
        }
    }

    public static class ClientMessage extends MessageClearSide {

        public ClientMessage(PacketBuffer buffer) {
            super(buffer);
        }

        public ClientMessage(BlockPos pos, Direction facing) {
            super(pos, facing);
        }
    }
}
