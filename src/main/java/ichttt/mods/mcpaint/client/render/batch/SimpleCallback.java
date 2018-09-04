package ichttt.mods.mcpaint.client.render.batch;

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
