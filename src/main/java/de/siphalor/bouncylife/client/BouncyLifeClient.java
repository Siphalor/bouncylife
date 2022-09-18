/*
 * Copyright 2021 Siphalor
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.siphalor.bouncylife.client;

import de.siphalor.bouncylife.BouncyLife;
import de.siphalor.bouncylife.client.render.PetSlimeEntityModel;
import de.siphalor.bouncylife.client.render.PetSlimeEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class BouncyLifeClient implements ClientModInitializer {
	public static EntityModelLayer PET_SLIME_ACCESSOIRIES_LAYER = new EntityModelLayer(new Identifier(BouncyLife.MOD_ID, "pet_slime"), "accessories");

	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(
				PET_SLIME_ACCESSOIRIES_LAYER,
				PetSlimeEntityModel::getAccessoriesTexturedModelData
		);
		EntityRendererRegistry.register(BouncyLife.petSlimeEntityType, PetSlimeEntityRenderer::new);

		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), BouncyLife.slimeBlocks);
	}
}
