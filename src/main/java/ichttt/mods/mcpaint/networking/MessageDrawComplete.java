package ichttt.mods.mcpaint.networking;

import com.google.common.primitives.Shorts;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.EventHandler;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageDrawComplete implements IMessage {
    private BlockPos pos;
    private EnumFacing facing;
    private byte scale;
    private int[][] data;

    public MessageDrawComplete() {

    }

    public MessageDrawComplete(BlockPos pos, EnumFacing facing, byte scale, int[][] data) {
        this.pos = pos;
        this.facing = facing;
        this.scale = scale;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.scale = buf.readByte();
        this.facing = EnumFacing.byIndex(buf.readByte());
        short max = buf.readShort();
        this.data = new int[max][max];
        for (int i = 0; i < max; i++) {
            for (int j = 0; j < max; j++) {
                data[i][j] = buf.readInt();
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeByte(scale);
        buf.writeByte(facing.getIndex());
        buf.writeShort(Shorts.checkedCast(data.length));
        for (short i = 0; i < data.length; i++) {
            int[] subarray = data[i];
            if (subarray.length != data.length) throw new RuntimeException("Wrong length: " + subarray.length + " needs to be " + data.length);
            for (int value : subarray) {
                buf.writeInt(value);
            }
        }
    }

    public static class Handler implements IMessageHandler<MessageDrawComplete, IMessage> {

        @Override
        public IMessage onMessage(MessageDrawComplete message, MessageContext ctx) {
            NetHandlerPlayServer handler = ctx.getServerHandler();
            handler.player.server.addScheduledTask(() ->{
                if (!handler.player.world.isBlockLoaded(message.pos)) {
                    handler.disconnect(new TextComponentString("Trying to write to unloaded block"));
                    return;
                }

                if (handler.player.getDistance(message.pos.getX(), message.pos.getY(), message.pos.getZ()) > (Math.round(handler.player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue()) + 5)) {
                    MCPaint.LOGGER.warn("Player" + handler.player.getName() + " is writing to out of reach block!");
                }

                IBlockState state = handler.player.world.getBlockState(message.pos);
                if (state.getBlock() != EventHandler.CANVAS) {
                    MCPaint.LOGGER.warn("Invalid block at pos " + message.pos + " has been selected by player " + handler.player.getName() + " - Block invalid");
                    return;
                }

                TileEntity te = handler.player.world.getTileEntity(message.pos);
                if (!(te instanceof TileEntityCanvas)) {
                    MCPaint.LOGGER.warn("Invalid block at pos " + message.pos + " has been selected by player " + handler.player.getName() + " - TE invalid");
                    return;
                }
                ((TileEntityCanvas) te).getPaintFor(message.facing).setData(message.scale, message.data);
                te.markDirty();
            });
            return null;
        }
    }
}
