package ichttt.mods.mcpaint.client.delegators;

import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DelegatingBakedModel extends BakedModelWrapper<BakedModel> {

    public DelegatingBakedModel(BakedModel originalModel) {
        super(originalModel);
    }

    private static BakedModel getModel(BlockState newState) {
        ModelManager shapes = Minecraft.getInstance().getModelManager();
        if (newState == null)
            return shapes.getMissingModel();
        else
            return shapes.getBlockModelShaper().getBlockModel(newState);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        BlockState newState = extraData.get(TileEntityCanvas.BLOCK_STATE_PROPERTY);
        return getModel(newState).getQuads(newState == null ? state : newState, side, rand, ModelData.EMPTY, renderType);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData extraData) {
        BlockState newState = extraData.get(TileEntityCanvas.BLOCK_STATE_PROPERTY);
        return getModel(newState).getRenderTypes(newState == null ? state : newState, rand, ModelData.EMPTY);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        BlockState newState = data.get(TileEntityCanvas.BLOCK_STATE_PROPERTY);
        return getModel(newState).getParticleIcon(ModelData.EMPTY);
    }
}
