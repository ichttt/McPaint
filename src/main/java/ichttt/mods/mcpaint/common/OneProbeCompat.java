package ichttt.mods.mcpaint.common;

import com.google.common.base.Function;
import ichttt.mods.mcpaint.MCPaint;
import ichttt.mods.mcpaint.common.block.BlockCanvas;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.providers.HarvestInfoTools;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//Hey look at this. Isn't this another big hack?
//Should probably just ask him to provide a function to remap a blockstate
public class OneProbeCompat implements Function<ITheOneProbe, Void>, IProbeConfigProvider, IProbeInfoProvider {
    private static final Method showHarvestInfoMethod;

    static {
        Method method;
        try {
            method = HarvestInfoTools.class.getDeclaredMethod("showHarvestInfo", IProbeInfo.class, World.class, BlockPos.class, Block.class, IBlockState.class, EntityPlayer.class);
            method.setAccessible(true);
        } catch (ReflectiveOperationException | LinkageError e) {
            MCPaint.LOGGER.error("Couldn't hack into the one probe! Harvest info will be missing from painted blocks!", e);
            method = null;
        }
        showHarvestInfoMethod = method;
    }

    @Nullable
    @Override
    public Void apply(@Nullable ITheOneProbe input) {
        if (input != null && showHarvestInfoMethod == null) {
            input.registerProvider(this);
            input.registerProbeConfigProvider(this);
        }
        return null;
    }

    private static IBlockState getReal(IBlockState state, IBlockAccess world, IProbeHitData data) {
        if (state.getBlock() instanceof BlockCanvas && world != null ) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (te instanceof TileEntityCanvas) {
                return ((TileEntityCanvas) te).getContainedState();
            }
        }
        return null;
    }

    @Override
    public void getProbeConfig(IProbeConfig config, EntityPlayer player, World world, Entity entity, IProbeHitEntityData data) {}

    @Override
    public void getProbeConfig(IProbeConfig config, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        IBlockState real = getReal(blockState, world, data);
        if (real != null) {
            config.showHarvestLevel(IProbeConfig.ConfigMode.NOT);
            config.showCanBeHarvested(IProbeConfig.ConfigMode.NOT);
        }
    }

    @Override
    public String getID() {
        return MCPaint.MODID;
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        IBlockState real = getReal(blockState, world, data);
        if (real != null) {
            try {
                showHarvestInfoMethod.invoke(null, probeInfo, world, data.getPos(), real.getBlock(), real, player);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Impossible reflection failure: ", e);
            } catch (InvocationTargetException e) {
                MCPaint.LOGGER.error("Error invoking showHarvestInfo: ", e);
            }
        }
    }
}
