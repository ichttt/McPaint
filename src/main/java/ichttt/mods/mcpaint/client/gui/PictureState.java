package ichttt.mods.mcpaint.client.gui;

import ichttt.mods.mcpaint.client.ClientProxy;

import java.awt.*;
import java.util.Arrays;

public class PictureState {
    public final int[][] picture;

    public PictureState(int[][] picture) {
        this.picture = ClientProxy.copyOf(picture);
    }

    public PictureState(PictureState state) {
        this.picture = ClientProxy.copyOf(state.picture);
    }

    public boolean isSame(PictureState other) {
        return Arrays.deepEquals(other.picture, this.picture);
    }
}
