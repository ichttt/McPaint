package ichttt.mods.mcpaint.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;

public class MessageDrawAbort {
    private BlockPos pos;

    public MessageDrawAbort() {}

    public MessageDrawAbort(BlockPos pos) {
        this.pos = pos;
    }

    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
    }

    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
    }

//    public static class Handler implements IMessageHandler<MessageDrawAbort, IMessage> {
//
//        @Override
//        public IMessage onMessage(MessageDrawAbort message, MessageContext ctx) {
//            NetHandlerPlayServer handler = ctx.getServerHandler();
//            handler.player.server.addScheduledTask(() -> {
//                if (MCPaintUtil.isPosInvalid(handler, message.pos)) return;
//
//                TileEntity te = handler.player.world.getTileEntity(message.pos);
//                if (te instanceof TileEntityCanvas) {
//                    TileEntityCanvas canvas = (TileEntityCanvas) te;
//                    boolean hasData = false;
//                    for (EnumFacing facing : EnumFacing.VALUES) {
//                        if (canvas.hasPaintFor(facing)) {
//                            hasData = true;
//                            break;
//                        }
//                    }
//                    if (!hasData && canvas.getContainedState() != null) {
//                        handler.player.world.setBlockState(message.pos, canvas.getContainedState());
//                    }
//                }
//            });
//            return null;
//        }
//    }
}
