package ichttt.mods.mcpaint.common.capability;

import com.google.common.primitives.Shorts;
import ichttt.mods.mcpaint.client.ClientHooks;
import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.Arrays;

public class Paint implements IPaintable {
    private static final IPaintValidator TRUE_VALIDATOR = (pixelCountX, pixelCountY) -> true;

    private int[][] pictureData = null;
    private byte scaleFactor;
    private short pixelCountX;
    private short pixelCountY;
    private final IPaintValidator validator;
    private int hashCode = 0;
    private int[] palette;

    public Paint() {
        this(TRUE_VALIDATOR);
    }

    public Paint(IPaintValidator validator) {
        this.validator = validator;
    }

    @Override
    public boolean hasPaintData() {
        return pictureData != null;
    }

    @Override
    public void setData(byte scaleFactor, int[][] pictureData, @Nullable TileEntityCanvas canvas, @Nullable Direction facing) {
        IntSet set = null;
        if (pictureData != null) {
            set = new IntLinkedOpenHashSet();
            outerLoop: for (int[] pictureDatum : pictureData) {
                for (int color : pictureDatum) {
                    if (set.add(color) && set.size() > Byte.MAX_VALUE) {
                        set = null;
                        break outerLoop;
                    }
                }
            }
        }
        setDataWithPalette(scaleFactor, pictureData, set == null ? null : set.toArray((int[]) null), canvas, facing);
    }

    @Override
    public void setDataWithPalette(byte scaleFactor, int[][] pictureData, int[] palette, @Nullable TileEntityCanvas canvas, @Nullable Direction facing) {
        short pixelCountX = Shorts.checkedCast(pictureData == null ? 0 : (pictureData.length * scaleFactor));
        short pixelCountY = Shorts.checkedCast(pictureData == null ? 0 : (pictureData[0].length * scaleFactor));
        if (!this.isValidPixelCount(pixelCountX, pixelCountY))
            throw new IllegalArgumentException("Invalid pixel count: x:" + pixelCountX + " y:" + pixelCountY);
        if (canvas == null || canvas.getLevel() == null || canvas.getLevel().isClientSide) {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
                if (!Arrays.deepEquals(this.pictureData, pictureData))
                    ClientHooks.invalidateCache(this, canvas, facing);
            });
        }
        this.pixelCountX = pixelCountX;
        this.pixelCountY = pixelCountY;
        this.scaleFactor = scaleFactor;
        this.pictureData = pictureData;
        this.hashCode = 0;
        this.palette = palette;
    }

    @Override
    public byte getScaleFactor() {
        return this.scaleFactor;
    }

    @Override
    public int[][] getPictureData(boolean immutable) {
        if (immutable)
            return this.pictureData;
        else
            return MCPaintUtil.copyOf(this.pictureData);
    }

    @Override
    public short getPixelCountX() {
        return this.pixelCountX;
    }

    @Override
    public short getPixelCountY() {
        return this.pixelCountY;
    }

    @Nullable
    @Override
    public int[] getPalette() {
        return this.palette;
    }

    @Override
    public final boolean isValidPixelCount(short pixelCountX, short pixelCountY) {
        return this.validator.isValidPixelCount(pixelCountX, pixelCountY);
    }

    @Override
    public void copyFrom(IPaintable paint, @Nullable TileEntityCanvas canvas, @Nullable Direction facing) {
        this.setDataWithPalette(paint.getScaleFactor(), paint.getPictureData(true), paint.getPalette(), canvas, facing);
    }

    @Override
    public int hashCode() {
        if (this.hashCode == 0)
            this.hashCode = Arrays.deepHashCode(this.pictureData);
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof Paint) {
            Paint paint = (Paint) obj;
            return Arrays.deepEquals(paint.pictureData, this.pictureData);
        }
        return false;
    }
}
