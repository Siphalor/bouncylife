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

import de.siphalor.bouncylife.entity.PetSlimeEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;

public class PetSlimeOverlayFeatureRenderer<T extends PetSlimeEntity> extends FeatureRenderer<T, PetSlimeEntityModel<T>> {
	protected PetSlimeEntityModel<T> model = new PetSlimeEntityModel<>(0);

	public PetSlimeOverlayFeatureRenderer(FeatureRendererContext<T, PetSlimeEntityModel<T>> context) {
		super(context);
	}

	@Override
	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		if (!livingEntity.isInvisible()) {
			this.getContextModel().copyStateTo(this.model);
			this.model.animateModel(livingEntity, f, g, h);
			this.model.setAngles(livingEntity, f, g, j, k, l);
			VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(this.getTexture(livingEntity)));
			DyeColor dyeColor = livingEntity.getColor();
			if (dyeColor == null) {
				this.model.render(matrixStack, vertexConsumer, i, LivingEntityRenderer.getOverlay(livingEntity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
			} else {
				float[] color = dyeColor.getColorComponents();
				this.model.render(matrixStack, vertexConsumer, i, LivingEntityRenderer.getOverlay(livingEntity, 0.0F), color[0], color[1], color[2], 1.0F);
			}
		}
	}
}
