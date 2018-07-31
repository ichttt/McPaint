package ichttt.mods.mcpaint.common.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityCanvas extends TileEntity {
    //DO NOT CHANGE - USED FOR NBT
    public static final int CANVAS_PIXEL_COUNT = 112;
    private int[][] pictureData = null;
    private byte scaleFactor;

    public boolean hasData() {
        return pictureData != null;
    }

    public void storeData(byte scaleFactor, int[][] pictureData) {
        this.scaleFactor = scaleFactor;
        this.pictureData = pictureData;
    }

    public byte getScaleFactor() {
        return this.scaleFactor;
    }

    public int[][] getPicture() {
        return this.pictureData;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagCompound compound = super.writeToNBT(tag);
        if (hasData()) {
            compound.setByte("scale", scaleFactor);
            NBTTagCompound pictureInfo = new NBTTagCompound();
            for (int i = 0; i < (CANVAS_PIXEL_COUNT / this.scaleFactor); i++) {
                pictureInfo.setIntArray("" + i, this.pictureData[i]);
            }
            compound.setTag("picture", pictureInfo);
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("scale")) {
            this.scaleFactor = tag.getByte("scale");
            NBTTagCompound pictureInfo = tag.getCompoundTag("picture");
            int arraySize = CANVAS_PIXEL_COUNT / this.scaleFactor;
            this.pictureData = new int[arraySize][];
            for (int i = 0; i < (arraySize); i++) {
                this.pictureData[i] = pictureInfo.getIntArray("" + i);
            }
        }
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.readFromNBT(tag);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }
}
