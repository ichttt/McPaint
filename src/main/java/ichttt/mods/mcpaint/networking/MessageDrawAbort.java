package ichttt.mods.mcpaint.networking;

import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageDrawAbort {
    private final BlockPos pos;

    public MessageDrawAbort(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }

    public MessageDrawAbort(BlockPos pos) {
        this.pos = pos;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public static class Handler {

        public static void onMessage(MessageDrawAbort message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ServerPlayer player = MCPaintUtil.checkServer(ctx);
            ctx.setPacketHandled(true);
            ctx.enqueueWork(() -> {
                if (MCPaintUtil.isPosInvalid(player, message.pos)) return;

                BlockEntity te = player.level.getBlockEntity(message.pos);
                if (te instanceof TileEntityCanvas) {
                    TileEntityCanvas canvas = (TileEntityCanvas) te;
                    boolean hasData = false;
                    for (Direction facing : Direction.values()) {
                        if (canvas.hasPaintFor(facing)) {
                            hasData = true;
                            break;
                        }
                    }
                    if (!hasData && canvas.getContainedState() != null) {
                        player.level.setBlockAndUpdate(message.pos, canvas.getContainedState());
                    }
                }
            });
        }
    }
}
