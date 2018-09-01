package ichttt.mods.mcpaint.client.gui;

import ichttt.mods.mcpaint.common.MCPaintUtil;

import java.util.Arrays;

public class PictureState {
    public final int[][] picture;

    public PictureState(int[][] picture) {
        this.picture = MCPaintUtil.copyOf(picture);
    }

    public PictureState(PictureState state) {
        this(state.picture);
    }

    public boolean isSame(PictureState other) {
        if (other == null) return false;
        return Arrays.deepEquals(other.picture, this.picture);
    }
}
