package ichttt.mods.mcpaint.client.gui.drawutil;

import java.awt.*;

public enum EnumPaintColor {
    BLACK(Color.BLACK),
    WHITE(Color.WHITE),
    GRAY(new Color(64, 64, 64)),
    RED(Color.RED),
    ORANGE(new Color(255, 106, 0)),
    YELLOW(Color.YELLOW),
    LIME(new Color(182, 255, 0)),
    GREEN(Color.GREEN),
    LIGHT_BLUE(new Color(0, 148, 255)),
    DARK_BLUE(new Color(0, 38, 255)),
    PURPLE(new Color(178, 0, 255)),
    PINK(new Color(255, 0, 220));

    public static final EnumPaintColor[] VALUES = values();
    public final Color color;

    EnumPaintColor(Color color) {
        this.color = color;
    }
}
