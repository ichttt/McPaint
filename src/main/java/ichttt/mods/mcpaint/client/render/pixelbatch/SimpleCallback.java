package ichttt.mods.mcpaint.client.render.pixelbatch;

import ichttt.mods.mcpaint.common.block.IOptimisationCallback;

public abstract class SimpleCallback implements IOptimisationCallback {
    private volatile boolean isInvalid = false;

    public void invalidate() {
        this.isInvalid = true;
    }

    @Override
    public boolean isInvalid() {
        return isInvalid;
    }
}
