package ichttt.mods.mcpaint.client.delegators;

import ichttt.mods.mcpaint.common.block.TileEntityCanvas;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class DelegatingBakedModel extends BakedModelWrapper<IBakedModel> {

    public DelegatingBakedModel(IBakedModel originalModel) {
        super(originalModel);
    }

    private static IBakedModel getModel(BlockState newState) {
        ModelManager shapes = Minecraft.getInstance().getModelManager();
        if (newState == null)
            return shapes.getMissingModel();
        else
            return shapes.getBlockModelShaper().getBlockModel(newState);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        BlockState newState = extraData.getData(TileEntityCanvas.BLOCK_STATE_PROPERTY);
        return getModel(newState).getQuads(newState == null ? state : newState, side, rand, EmptyModelData.INSTANCE);
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        BlockState newState = data.getData(TileEntityCanvas.BLOCK_STATE_PROPERTY);
        return getModel(newState).getParticleTexture(EmptyModelData.INSTANCE);
    }
}
