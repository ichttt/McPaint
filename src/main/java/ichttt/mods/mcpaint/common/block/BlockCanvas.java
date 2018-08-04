package ichttt.mods.mcpaint.common.block;

import ichttt.mods.mcpaint.client.gui.GuiDraw;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCanvas extends BlockDirectional {
    public static final PropertyBool PAINTED = PropertyBool.create("painted");

    public BlockCanvas() {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(PAINTED, false));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return state.getValue(PAINTED);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityCanvas();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        world.setBlockState(pos, state.withProperty(BlockCanvas.PAINTED, true));
        if (world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityCanvas && ((TileEntityCanvas) tileEntity).paint.hasPaintData()) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiDraw(((TileEntityCanvas) tileEntity).paint, tileEntity.getPos()));
            } else {
                Minecraft.getMinecraft().displayGuiScreen(new GuiDraw((byte) 8, pos));
            }
        }
        return true;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PAINTED);
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
       return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return !state.getValue(PAINTED);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (meta == 1) {
            return this.getDefaultState().withProperty(PAINTED, true);
        } else if (meta == 0) {
            return this.getDefaultState().withProperty(PAINTED, false);
        } else {
            throw new RuntimeException("Unknown meta value " + meta);
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PAINTED) ? 1 : 0;
    }
}
