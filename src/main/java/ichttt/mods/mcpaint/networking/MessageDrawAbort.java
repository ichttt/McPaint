package ichttt.mods.mcpaint.networking;

import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageDrawAbort {
    private final BlockPos pos;

    public MessageDrawAbort(PacketBuffer buffer) {
        this.pos = buffer.readBlockPos();
    }

    public MessageDrawAbort(BlockPos pos) {
        this.pos = pos;
    }

    public void encode(PacketBuffer buf) {
        buf.writeBlockPos(this.pos);
    }

    public static class Handler {

        public static void onMessage(MessageDrawAbort message, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ServerPlayerEntity player = MCPaintUtil.checkServer(ctx);
            ctx.setPacketHandled(true);
            ctx.enqueueWork(() -> {
                if (MCPaintUtil.isPosInvalid(player, message.pos)) return;

                TileEntity te = player.world.getTileEntity(message.pos);
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
                        player.world.setBlockState(message.pos, canvas.getContainedState());
                    }
                }
            });
        }
    }
}
