package ichttt.mods.mcpaint.client.gui.drawutil;

import ichttt.mods.mcpaint.common.MCPaintUtil;
import ichttt.mods.mcpaint.common.capability.IPaintable;

import java.util.Arrays;

public class PictureState {
    public final int[][] picture;
    public final byte scaleFactor;

    public PictureState(int[][] picture, byte scaleFactor) {
        this.picture = MCPaintUtil.copyOf(picture);
        this.scaleFactor = scaleFactor;
    }

    public PictureState(PictureState state) {
        this(state.picture, state.scaleFactor);
    }

    public PictureState(IPaintable paint) {
        this.picture = paint.getPictureData(false);
        this.scaleFactor = paint.getScaleFactor();
    }

    public boolean isSame(PictureState other) {
        if (other == null) return false;
        return Arrays.deepEquals(other.picture, this.picture);
    }
}
