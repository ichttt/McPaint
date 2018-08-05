package ichttt.mods.mcpaint.common.capability;

public interface IPaintable extends IPaintValidator {

    boolean hasPaintData();

    byte getScaleFactor();

    int[][] getPictureData();

    short getPixelCountX();

    short getPixelCountY();

    void setData(short pixelCountX, short pixelCountY, byte scaleFactor, int[][] pictureData);
}
