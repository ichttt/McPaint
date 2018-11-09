package ichttt.mods.mcpaint.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class MessageClearSide {
    private BlockPos pos;
    private EnumFacing facing;

    public MessageClearSide() {}

    public MessageClearSide(BlockPos pos, EnumFacing facing) {
        this.pos = pos;
        this.facing = facing;
    }

    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        facing = EnumFacing.byIndex(buf.readByte());
    }

    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeByte(facing.getIndex());
    }

    public static class Handler {
        public void onMessage(MessageClearSide message) {
//            MessagePaintData.ServerHandler.setServerData(ctx, message.pos, message.facing, (byte) 0, null);
        }
    }
}
