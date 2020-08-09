package de.siphalor.bouncylife.client.render;

import de.siphalor.bouncylife.BouncyLife;
import de.siphalor.bouncylife.entity.PetSlimeEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class PetSlimeSaddleFeatureRenderer extends FeatureRenderer<PetSlimeEntity, PetSlimeEntityModel<PetSlimeEntity>> {
	private static final Identifier BOW_TEX = new Identifier(BouncyLife.MOD_ID, "textures/entity/pet_slime/saddle.png");
	private final PetSlimeEntityModel<PetSlimeEntity> model = new PetSlimeEntityModel<>(0);

	public PetSlimeSaddleFeatureRenderer(FeatureRendererContext<PetSlimeEntity, PetSlimeEntityModel<PetSlimeEntity>> context) {
		super(context);
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
