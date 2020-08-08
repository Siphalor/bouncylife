package de.siphalor.bouncylife.client;

import de.siphalor.bouncylife.BouncyLife;
import de.siphalor.bouncylife.client.render.PetSlimeEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class BouncyLifeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.INSTANCE.register(BouncyLife.petSlimeEntityType, (entityRenderDispatcher, context) -> new PetSlimeEntityRenderer(entityRenderDispatcher));

		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), BouncyLife.slimeBlocks);
	}
}
