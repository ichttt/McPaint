package ichttt.mods.mcpaint.common.item;

import ichttt.mods.mcpaint.MCPaintConfig;
import ichttt.mods.mcpaint.client.ClientHooks;
import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import ichttt.mods.mcpaint.common.capability.CapabilityPaintable;
import ichttt.mods.mcpaint.common.capability.IPaintable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ItemStamp extends ItemBrush {

    public ItemStamp(ResourceLocation registryName) {
        super(registryName);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(ItemUseContext context) {
        ItemStack hold = context.getItem();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        EntityPlayer player = context.getPlayer();
        EnumFacing facing = context.getFace();
        IBlockState state = world.getBlockState(pos);
        IPaintable paint = Objects.requireNonNull(hold.getCapability(CapabilityPaintable.PAINTABLE, null).orElse(null));
        if (paint != null && paint.hasPaintData()) {
            return super.onItemUse(context);
        } else if (player.isSneaking()) {
            facing = facing.getOpposite();
            if (state.getBlock() instanceof BlockCanvas) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof TileEntityCanvas) {
                    TileEntityCanvas canvas = (TileEntityCanvas) te;
                    if (canvas.hasPaintFor(facing)) {
                        paint.copyFrom(canvas.getPaintFor(facing), canvas, facing);
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
        }
        return EnumActionResult.FAIL;
    }

    @Override
    protected void startPainting(TileEntityCanvas canvas, World world, ItemStack heldItem, BlockPos pos, EnumFacing facing, IBlockState state) {
        if (world.isRemote) {
            IPaintable heldPaint = Objects.requireNonNull(heldItem.getCapability(CapabilityPaintable.PAINTABLE, null).orElseThrow(() -> new RuntimeException("No paint in stamp")));
            if (MCPaintConfig.CLIENT.directApplyStamp) {
                canvas.getPaintFor(facing).copyFrom(heldPaint, canvas, facing);
                MCPaintUtil.uploadPictureToServer(canvas, facing, heldPaint.getScaleFactor(), heldPaint.getPictureData(), false);
            } else {
                List<IPaintable> paintList = new LinkedList<>();
                if (canvas.hasPaintFor(facing)) {
                    paintList.add(canvas.getPaintFor(facing));
                }
                paintList.add(heldPaint);
                DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientHooks.showGuiDraw(paintList, pos, facing, canvas.getContainedState()));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        IPaintable paintable = stack.getCapability(CapabilityPaintable.PAINTABLE, null).orElse(null);
        if (paintable != null && paintable.hasPaintData()) {
            tooltip.add(new TextComponentTranslation("mcpaint.tooltip.stamp.paint"));
        } else {
            tooltip.add(new TextComponentTranslation("mcpaint.tooltip.stamp.nopaint"));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
