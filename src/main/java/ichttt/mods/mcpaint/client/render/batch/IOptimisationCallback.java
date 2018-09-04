package ichttt.mods.mcpaint.client.render.batch;

import ichttt.mods.mcpaint.client.render.CachedBufferBuilder;

public interface IOptimisationCallback {

    boolean isInvalid();

    void provideFinishedBuffer(CachedBufferBuilder builder);

    default void optimizationFailed() {}
}
