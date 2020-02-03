package ichttt.mods.mcpaint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class MyCustomData extends JsonReloadListener {
    public MyCustomData(Gson p_i51536_1_, String p_i51536_2_) {
        super(p_i51536_1_, p_i51536_2_);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> splashList, IResourceManager resourceManagerIn, IProfiler profilerIn) {

    }
}
