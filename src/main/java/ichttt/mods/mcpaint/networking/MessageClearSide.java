package ichttt.mods.mcpaint.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageClearSide implements IMessage {
    private BlockPos pos;
    private EnumFacing facing;

    public MessageClearSide() {}

    public MessageClearSide(BlockPos pos, EnumFacing facing) {
        this.pos = pos;
        this.facing = facing;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        facing = EnumFacing.byIndex(buf.readByte());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeByte(facing.getIndex());
    }

    public static class Handler implements IMessageHandler<MessageClearSide, IMessage> {
        @Override
        public IMessage onMessage(MessageClearSide message, MessageContext ctx) {
            MessagePaintData.ServerHandler.setServerData(ctx, message.pos, message.facing, (byte) 0, null);
            return null;
        }
    }
}
