package de.siphalor.bouncylife.util;

import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class SlimeFeatureRendererContext implements FeatureRendererContext<LivingEntity, SlimeEntityModel<LivingEntity>> {
	private SlimeEntityModel model;

	public SlimeFeatureRendererContext(SlimeEntityModel model) {
		this.model = model;
	}

	@Override
	public SlimeEntityModel getModel() {
		return model;
	}

	@Override
	public void bindTexture(Identifier var1) {

	}

	@Override
	public void applyLightmapCoordinates(LivingEntity var1) {

	}
}
