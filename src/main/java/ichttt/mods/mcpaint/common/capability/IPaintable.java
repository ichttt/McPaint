package ichttt.mods.mcpaint.common.capability;

public interface IPaintable extends IPaintValidator {

    boolean hasPaintData();

    byte getScaleFactor();

    int[][] getPictureData();

    short getPixelCountX();

    short getPixelCountY();

    void setData(byte scaleFactor, int[][] pictureData);

    void copyFrom(IPaintable paint);

    @Override
    int hashCode();

    @Override
    boolean equals(Object other);
}
