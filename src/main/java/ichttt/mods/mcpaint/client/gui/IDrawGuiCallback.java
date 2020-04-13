package ichttt.mods.mcpaint.client.gui;

public interface IDrawGuiCallback {

    boolean isInPicture(int offsetMouseX, int offsetMouseY);

    void updateSliders();

    void updateUndoRedoButton(boolean hasUndo, boolean hasRedo);

    int offsetMouseX(int mouseX);

    int offsetMouseY(int mouseY);
}
