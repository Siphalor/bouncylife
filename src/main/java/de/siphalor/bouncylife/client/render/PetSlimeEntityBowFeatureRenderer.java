package de.siphalor.bouncylife.client.render;

import de.siphalor.bouncylife.BouncyLife;
import de.siphalor.bouncylife.entity.PetSlimeEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class PetSlimeEntityBowFeatureRenderer extends FeatureRenderer<PetSlimeEntity, SlimeEntityModel<PetSlimeEntity>> {
	private static final Identifier BOW_TEX = new Identifier(BouncyLife.MOD_ID, "textures/entity/pet_slime/bow.png");
	private final SlimeEntityModel<PetSlimeEntity> model = new SlimeEntityModel<>(0);

	public PetSlimeEntityBowFeatureRenderer(FeatureRendererContext<PetSlimeEntity, SlimeEntityModel<PetSlimeEntity>> context) {
		super(context);
		for (ModelPart modelPart : model.getParts()) {
			modelPart.setTextureSize(128, 64);
		}
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PetSlimeEntity entity, float limbAngle, float limbDistance, float tickDelta, float customAngle, float headYaw, float headPitch) {
		if (!entity.isInvisible()) {
			getContextModel().copyStateTo(model);
			model.animateModel(entity, limbAngle, limbDistance, tickDelta);
			model.setAngles(entity, limbAngle, limbDistance, customAngle, headYaw, headPitch);

			VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(BOW_TEX));
			model.render(matrices, vertexConsumer, light, LivingEntityRenderer.getOverlay(entity, 0F), 1F, 1F, 1F, 1F);
		}
	}
}
