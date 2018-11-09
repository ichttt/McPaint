package ichttt.mods.mcpaint.networking;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.primitives.Shorts;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Consumer;

public class MessagePaintData {
    private BlockPos pos;
    private EnumFacing facing;
    private byte scale;
    private byte part;
    private byte maxParts;
    private int[][] data;

    public MessagePaintData() {}

    public static void createAndSend(BlockPos pos, EnumFacing facing, byte scale, int[][] data, Consumer<MessagePaintData> sender) {
        int length = data.length;
        if (length > 0)
            length *= data[0].length;
        if (length > 8000) { //We need to split
            int partsAsInt = (length / 8000) + 1;
            while (data.length % partsAsInt != 0) {
                partsAsInt++;
                if (partsAsInt > 32) throw new RuntimeException("Hell I'm not sending " + partsAsInt + "+ packets for a single image of length " + length);
            }

            if (partsAsInt > Byte.MAX_VALUE)
                throw new IllegalArgumentException("Picture too large: " + length);
            byte parts = (byte) partsAsInt;
            for (byte b = 1; b <= parts; b++) {
                MessagePaintData toSend = new MessagePaintData(pos, facing, scale, data, b, parts);
                sender.accept(toSend);
            }
        } else {
            MessagePaintData toSend = new MessagePaintData(pos, facing, scale, data, (byte) 0, (byte) 0);
            sender.accept(toSend);
        }
    }

    public MessagePaintData(BlockPos pos, EnumFacing facing, byte scale, int[][] data, byte part, byte maxParts) {
        this.pos = pos;
        this.facing = facing;
        this.scale = scale;
        this.data = data;
        this.part = part;
        this.maxParts = maxParts;
    }

    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.scale = buf.readByte();
        this.facing = EnumFacing.byIndex(buf.readByte());
        this.part = buf.readByte();
        this.maxParts = buf.readByte();
        short max = buf.readShort();
        short secondMax = buf.readShort();

        this.data = new int[max][secondMax];
        for (int i = 0; i < max; i++) {
            for (int j = 0; j < secondMax; j++) {
                data[i][j] = buf.readInt();
            }
        }
    }

    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeByte(scale);
        buf.writeByte(facing.getIndex());
        buf.writeByte(this.part);
        buf.writeByte(this.maxParts);
        short max = Shorts.checkedCast(this.maxParts == 0 ? data.length : data.length / this.maxParts);
        buf.writeShort(Shorts.checkedCast(max));
        buf.writeShort(Shorts.checkedCast(data[0].length));

        int offset = this.maxParts == 0 ? max : max * this.part;
        for (int i = offset - max; i < offset; i++) {
            int[] subarray = this.data[i];
            for (int value : subarray) {
                buf.writeInt(value);
            }
        }
    }

    public static class ServerHandler {
        @SuppressWarnings("UnstableApiUsage")
        private final Multimap<BlockPos, MessagePaintData> partMap = MultimapBuilder.hashKeys().hashSetValues().build();

        public void onMessage(MessagePaintData message, INetHandler ctx) {
            if (message.maxParts == 0) //single message
                handleSide(ctx, message.pos, message.facing, message.scale, message.data);
            else {
                synchronized (partMap) {
                    partMap.put(message.pos, message);
                    Collection<MessagePaintData> messages = partMap.get(message.pos);
                    if (messages.size() == message.maxParts) {
                        int[][] data = new int[message.data.length * message.maxParts][message.data[0].length];
                        messages.stream().sorted(Comparator.comparingInt(o -> o.part)).forEachOrdered(messagePaintData -> {
                            int offset = messagePaintData.data.length * (messagePaintData.part - 1);
                            for (int i = 0; i < messagePaintData.data.length; i++) {
                                int[] subarray = messagePaintData.data[i];
                                System.arraycopy(messagePaintData.data[i], 0, data[i + offset], 0, subarray.length);
                            }
                        });
                        partMap.removeAll(message.pos);
                        handleSide(ctx, message.pos, message.facing, message.scale, data);
                    }
                }
            }
        }

        protected void handleSide(INetHandler ctx, BlockPos pos, EnumFacing facing, byte scale, int[][] data) {
            setServerData(ctx, pos, facing, scale, data);
        }

        public static void setServerData(INetHandler ctx, BlockPos pos, EnumFacing facing, byte scale, int[][] data) {
            NetHandlerPlayServer handler = (NetHandlerPlayServer) ctx;
            handler.player.server.addScheduledTask(() ->{
                if (MCPaintUtil.isPosInvalid(handler, pos)) return;

                IBlockState state = handler.player.world.getBlockState(pos);
                if (!(state.getBlock() instanceof BlockCanvas)) {
                    MCPaint.LOGGER.warn("Invalid block at pos " + pos + " has been selected by player " + handler.player.getName() + " - Block invalid");
                    return;
                }

                TileEntity te = handler.player.world.getTileEntity(pos);
                if (!(te instanceof TileEntityCanvas)) {
                    MCPaint.LOGGER.warn("Invalid block at pos " + pos + " has been selected by player " + handler.player.getName() + " - TE invalid");
                    return;
                }
                TileEntityCanvas canvas = (TileEntityCanvas) te;
                if (data == null)
                    canvas.removePaint(facing);
                else
                    canvas.getPaintFor(facing).setData(scale, data, canvas, facing);
                te.markDirty();
                //TODO networking mess
//                TargetPoint point = new NetworkRegistry.TargetPoint(handler.player.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), -1);
                if (data == null) {
//                    MCPaint.NETWORKING.sendTo(new MessageClearSide(pos, facing), handler.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
                } else {
//                    MessagePaintData.createAndSend(pos, facing, scale, data, messagePaintData -> MCPaint.NETWORKING.sendToAllTracking(messagePaintData, point));
                }
            });
        }
    }

//    public static class ClientHandler extends ServerHandler { TODO networking
//
//        @SideOnly(Side.CLIENT)
//        @Override
//        protected void handleSide(MessageContext ctx, BlockPos pos, EnumFacing facing, byte scale, int[][] data) {
//            Minecraft.getMinecraft().addScheduledTask(() -> {
//                World world = Minecraft.getMinecraft().world;
//                if (!world.isBlockLoaded(pos)) {
//                    MCPaint.LOGGER.warn("Invalid pos " + pos + " when updating data - Not loaded");
//                }
//
//                TileEntity te = world.getTileEntity(pos);
//                if (!(te instanceof TileEntityCanvas)) {
//                    MCPaint.LOGGER.warn("Invalid block at pos " + pos + " when updating data - TE invalid");
//                    return;
//                }
//                TileEntityCanvas canvas = (TileEntityCanvas) te;
//                canvas.getPaintFor(facing).setData(scale, data, canvas, facing);
//                te.markDirty();
//            });
//        }
//    }
}
