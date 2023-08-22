package ichttt.mods.mcpaint.networking;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.primitives.Shorts;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MessagePaintData {
    private final BlockPos pos;
    private final Direction facing;
    private final byte scale;
    private final byte part;
    private final byte maxParts;
    private final int[][] data;
    private final int[] palette;

    public static void createAndSend(BlockPos pos, Direction facing, byte scale, int[] palette, int[][] data, Consumer<MessagePaintData> sender) {
        int length = data.length;
        if (length > 0)
            length *= data[0].length;
        if ((length > 32000) || (palette == null && length > 8000)) { //We need to split
            int partsAsInt = (length / 32000) + 1;
            while (data.length % partsAsInt != 0) {
                partsAsInt++;
                if (partsAsInt > 32) throw new RuntimeException("Hell I'm not sending " + partsAsInt + "+ packets for a single image of length " + length);
            }

            if (partsAsInt > Byte.MAX_VALUE)
                throw new IllegalArgumentException("Picture too large: " + length);
            byte parts = (byte) partsAsInt;
            for (byte b = 1; b <= parts; b++) {
                MessagePaintData toSend = new MessagePaintData(pos, facing, scale, palette, data, b, parts);
                sender.accept(toSend);
            }
        } else {
            MessagePaintData toSend = new MessagePaintData(pos, facing, scale, palette, data, (byte) 0, (byte) 0);
            sender.accept(toSend);
        }
    }

    public MessagePaintData(BlockPos pos, Direction facing, byte scale, int[] palette, int[][] data, byte part, byte maxParts) {
        this.pos = pos;
        this.facing = facing;
        this.scale = scale;
        this.palette = palette;
        this.data = data;
        this.part = part;
        this.maxParts = maxParts;
    }

    public MessagePaintData(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.scale = buf.readByte();
        this.facing = Direction.from3DDataValue(buf.readByte());
        this.part = buf.readByte();
        this.maxParts = buf.readByte();
        short max = buf.readShort();
        short secondMax = buf.readShort();

        byte paletteLength = buf.readByte();
        if (paletteLength <= 0) {
            this.palette = null;
        } else {
            this.palette = new int[paletteLength];
            for (int i = 0; i < paletteLength; i++) {
                this.palette[i] = buf.readInt();
            }
        }

        this.data = new int[max][secondMax];
        for (int i = 0; i < max; i++) {
            for (int j = 0; j < secondMax; j++) {
                if (palette == null) {
                    data[i][j] = buf.readInt();
                } else {
                    data[i][j] = this.palette[buf.readByte()];
                }
            }
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeByte(scale);
        buf.writeByte(facing.get3DDataValue());
        buf.writeByte(this.part);
        buf.writeByte(this.maxParts);
        short max = Shorts.checkedCast(this.maxParts == 0 ? data.length : data.length / this.maxParts);
        buf.writeShort(Shorts.checkedCast(max));
        buf.writeShort(Shorts.checkedCast(data[0].length));

        buf.writeByte(this.palette == null ? 0 : this.palette.length);
        Int2ByteMap reversePalette = null;

        if (this.palette != null) {
            for (int i : this.palette) {
                buf.writeInt(i);
            }
            reversePalette = MCPaintUtil.buildReversePalette(this.palette);
        }

        int offset = this.maxParts == 0 ? max : max * this.part;
        for (int i = offset - max; i < offset; i++) {
            int[] subarray = this.data[i];
            for (int value : subarray) {
                if (this.palette != null)
                    buf.writeByte(reversePalette.get(value));
                else
                    buf.writeInt(value);
            }
        }
    }

    public static class ServerHandler {
        public static final ServerHandler INSTANCE = new ServerHandler();

        private final Multimap<BlockPos, MessagePaintData> partMap = MultimapBuilder.hashKeys().hashSetValues().build();

        public void onMessage(MessagePaintData message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ctx.setPacketHandled(true);
            if (message.maxParts == 0) //single message
                ctx.enqueueWork(() -> handleSide(ctx, message.pos, message.facing, message.scale, message.palette, message.data));
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
                        ctx.enqueueWork(() -> handleSide(ctx, message.pos, message.facing, message.scale, message.palette, data));
                    }
                }
            }
        }

        public void handleSide(NetworkEvent.Context ctx, BlockPos pos, Direction facing, byte scale, int[] palette, int[][] data) {
            ServerPlayer player = MCPaintUtil.checkServer(ctx);
            if (MCPaintUtil.isPosInvalid(player, pos)) return;

            BlockState state = player.level().getBlockState(pos);
            if (!(state.getBlock() instanceof BlockCanvas)) {
                MCPaint.LOGGER.warn("Invalid block at pos " + pos + " has been selected by player " + player.getName() + " - Block invalid");
                return;
            }

            BlockEntity te = player.level().getBlockEntity(pos);
            if (!(te instanceof TileEntityCanvas canvas)) {
                MCPaint.LOGGER.warn("Invalid block at pos " + pos + " has been selected by player " + player.getName() + " - TE invalid");
                return;
            }
            if (data == null)
                canvas.removePaint(facing);
            else
                canvas.getPaintFor(facing).setDataWithPalette(scale, data, palette, canvas, facing);
            te.setChanged();
            PacketDistributor.PacketTarget target = PacketDistributor.TRACKING_CHUNK.with(() -> (LevelChunk) Objects.requireNonNull(te.getLevel()).getChunk(te.getBlockPos()));
            if (data == null) {
                MCPaint.NETWORKING.send(target, new MessageClearSide.ClientMessage(pos, facing));
            } else {
                MessagePaintData.createAndSend(pos, facing, scale, palette, data, messagePaintData -> MCPaint.NETWORKING.send(target, new MessagePaintData.ClientMessage(messagePaintData)));
            }
        }
    }

    public static class ClientHandler extends ServerHandler {
        public static final ClientHandler INSTANCE = new ClientHandler();

        @OnlyIn(Dist.CLIENT)
        @Override
        public void handleSide(NetworkEvent.Context ctx, BlockPos pos, Direction facing, byte scale, int[] palette, int[][] data) {
            MCPaintUtil.checkClient(ctx);
            Level world = Minecraft.getInstance().level;
            if (!world.hasChunkAt(pos)) {
                MCPaint.LOGGER.warn("Invalid pos " + pos + " when updating data - Not loaded");
            }

            BlockEntity te = world.getBlockEntity(pos);
            if (!(te instanceof TileEntityCanvas canvas)) {
                MCPaint.LOGGER.warn("Invalid block at pos " + pos + " when updating data - TE invalid");
                return;
            }
            canvas.getPaintFor(facing).setDataWithPalette(scale, data, palette, canvas, facing);
            te.setChanged();
        }
    }

    public static class ClientMessage extends MessagePaintData {
        public ClientMessage(MessagePaintData msg) {
            super(msg.pos, msg.facing, msg.scale, msg.palette, msg.data, msg.part, msg.maxParts);
        }

        public ClientMessage(FriendlyByteBuf buf) {
            super(buf);
        }
    }
}
