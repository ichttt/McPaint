package ichttt.mods.mcpaint.common.block;

import ichttt.mods.mcpaint.client.gui.GuiDraw;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCanvas extends Block {
    public static final PropertyBool PAINTED = PropertyBool.create("painted");

    public BlockCanvas() {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(PAINTED, false));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityCanvas();
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return state.getValue(PAINTED) ? EnumBlockRenderType.ENTITYBLOCK_ANIMATED : EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote)
            Minecraft.getMinecraft().displayGuiScreen(new GuiDraw((byte) 8, pos));
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PAINTED);
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
