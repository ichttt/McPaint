package ichttt.mods.mcpaint.networking;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.primitives.Shorts;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MessagePaintData {
    private final BlockPos pos;
    private final EnumFacing facing;
    private final byte scale;
    private final byte part;
    private final byte maxParts;
    private final int[][] data;

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

    public MessagePaintData(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
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

    public void encode(PacketBuffer buf) {
        buf.writeBlockPos(this.pos);
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
        public static final ServerHandler INSTANCE = new ServerHandler();

        @SuppressWarnings("UnstableApiUsage")
        private final Multimap<BlockPos, MessagePaintData> partMap = MultimapBuilder.hashKeys().hashSetValues().build();

        public void onMessage(MessagePaintData message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            if (message.maxParts == 0) //single message
                ctx.enqueueWork(() -> handleSide(ctx, message.pos, message.facing, message.scale, message.data));
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
                        ctx.enqueueWork(() -> handleSide(ctx, message.pos, message.facing, message.scale, data));
                    }
                }
            }
        }

        protected void handleSide(NetworkEvent.Context context, BlockPos pos, EnumFacing facing, byte scale, int[][] data) {
            setServerData(MCPaintUtil.getNetHandler(context), pos, facing, scale, data);
        }

        public static void setServerData(NetHandlerPlayServer handler, BlockPos pos, EnumFacing facing, byte scale, int[][] data) {
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
            PlayerChunkMapEntry entry = Objects.requireNonNull((WorldServer) te.getWorld()).getPlayerChunkMap().getEntry(MathHelper.floor(pos.getX()) >> 4, MathHelper.floor(pos.getZ()) >> 4);
            if (entry == null)
                return;

            for (EntityPlayerMP player : entry.getWatchingPlayers()) {
                if (data == null) {
                    MCPaint.NETWORKING.sendTo(new MessageClearSide(pos, facing), player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
                } else {
                    MessagePaintData.createAndSend(pos, facing, scale, data, messagePaintData -> MCPaint.NETWORKING.sendTo(new MessagePaintData.ClientMessage(messagePaintData), player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT));
                }
            }
        }
    }

    public static class ClientHandler extends ServerHandler {
        public static final ClientHandler INSTANCE = new ClientHandler();

        @OnlyIn(Dist.CLIENT)
        @Override
        protected void handleSide(NetworkEvent.Context ctx, BlockPos pos, EnumFacing facing, byte scale, int[][] data) {
            World world = Minecraft.getInstance().world;
            if (!world.isBlockLoaded(pos)) {
                MCPaint.LOGGER.warn("Invalid pos " + pos + " when updating data - Not loaded");
            }

            TileEntity te = world.getTileEntity(pos);
            if (!(te instanceof TileEntityCanvas)) {
                MCPaint.LOGGER.warn("Invalid block at pos " + pos + " when updating data - TE invalid");
                return;
            }
            TileEntityCanvas canvas = (TileEntityCanvas) te;
            canvas.getPaintFor(facing).setData(scale, data, canvas, facing);
            te.markDirty();
        }
    }

    public static class ClientMessage extends MessagePaintData {
        public ClientMessage(MessagePaintData msg) {
            super(msg.pos, msg.facing, msg.scale, msg.data, msg.part, msg.maxParts);
        }

        public ClientMessage(PacketBuffer buf) {
            super(buf);
        }
    }
}
