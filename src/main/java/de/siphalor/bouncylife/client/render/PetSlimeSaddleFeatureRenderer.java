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

package de.siphalor.bouncylife.client.render;

import de.siphalor.bouncylife.BouncyLife;
import de.siphalor.bouncylife.client.BouncyLifeClient;
import de.siphalor.bouncylife.entity.PetSlimeEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class PetSlimeSaddleFeatureRenderer extends FeatureRenderer<PetSlimeEntity, PetSlimeEntityModel<PetSlimeEntity>> {
	private static final Identifier BOW_TEX = new Identifier(BouncyLife.MOD_ID, "textures/entity/pet_slime/saddle.png");
	private final PetSlimeEntityModel<PetSlimeEntity> model;

	public PetSlimeSaddleFeatureRenderer(FeatureRendererContext<PetSlimeEntity, PetSlimeEntityModel<PetSlimeEntity>> context, EntityModelLoader loader) {
		super(context);
		model = new PetSlimeEntityModel<>(loader.getModelPart(BouncyLifeClient.PET_SLIME_ACCESSOIRIES_LAYER));
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PetSlimeEntity entity, float limbAngle, float limbDistance, float tickDelta, float customAngle, float headYaw, float headPitch) {
		if (!entity.isInvisible() && entity.isSaddled()) {
			getContextModel().copyStateTo(model);
			model.animateModel(entity, limbAngle, limbDistance, tickDelta);
			model.setAngles(entity, limbAngle, limbDistance, customAngle, headYaw, headPitch);

			VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(getTexture(entity)));
			model.render(matrices, vertexConsumer, light, LivingEntityRenderer.getOverlay(entity, 0F), 1F, 1F, 1F, 1F);
		}
	}

	@Override
	protected Identifier getTexture(PetSlimeEntity entity) {
		return BOW_TEX;
	}
}
