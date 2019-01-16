package ichttt.mods.mcpaint.networking;

import ichttt.mods.mcpaint.common.MCPaintUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageClearSide {
    private BlockPos pos;
    private EnumFacing facing;

    public MessageClearSide(PacketBuffer buffer) {
        this(buffer.readBlockPos(), EnumFacing.byIndex(buffer.readByte()));
    }

    public MessageClearSide(BlockPos pos, EnumFacing facing) {
        this.pos = pos;
        this.facing = facing;
    }

    public void encode(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(facing.getIndex());
    }

    public static class Handler {
        public static void onMessage(MessageClearSide message, Supplier<NetworkEvent.Context> supplier) {
            MessagePaintData.ServerHandler.setServerData(MCPaintUtil.getNetHandler(supplier.get()), message.pos, message.facing, (byte) 0, null);
        }
    }
}
