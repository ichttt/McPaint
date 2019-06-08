package ichttt.mods.mcpaint.common.capability;

import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public interface IPaintable extends IPaintValidator {

    boolean hasPaintData();

    byte getScaleFactor();

    /**
     * If you mutate the array, call setData again to rehash
     */
    int[][] getPictureData();

    short getPixelCountX();

    short getPixelCountY();

    /**
     * @param canvas The canvas the cache should be cleared for
     */
    void setData(byte scaleFactor, int[][] pictureData, @Nullable TileEntityCanvas canvas, @Nullable Direction facing);

    void copyFrom(IPaintable paint, @Nullable TileEntityCanvas canvas, @Nullable Direction facing);

    @Override
    int hashCode();

    @Override
    boolean equals(Object other);

    default void clear(@Nullable TileEntityCanvas canvas, @Nullable Direction facing) {
        setData((byte) 0, null, canvas, facing);
    }
}
