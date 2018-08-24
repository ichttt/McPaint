package ichttt.mods.mcpaint.common.block;

import ichttt.mods.mcpaint.client.render.CachedBufferBuilder;

public interface IOptimisationCallback {

    boolean isInvalid();

    void provideFinishedBuffer(CachedBufferBuilder builder);

    default void optimizationFailed() {}
}
