package ichttt.mods.mcpaint.client.render.batch;

import ichttt.mods.mcpaint.client.render.buffer.BufferManager;

public interface IOptimisationCallback {

    boolean isInvalid();

    void provideFinishedBuffer(BufferManager builder);

    default void optimizationFailed() {}
}
