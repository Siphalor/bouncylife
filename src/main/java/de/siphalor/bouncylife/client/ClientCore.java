package de.siphalor.bouncylife.client;

import de.siphalor.bouncylife.util.SlimeFeatureRendererContext;
import net.minecraft.client.render.entity.feature.SlimeOverlayFeatureRenderer;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.entity.LivingEntity;

public class ClientCore {
	public static final SlimeEntityModel BOUNCYLIFE$SLIME_ENTITY_MODEL = new SlimeEntityModel(1);
	public static final SlimeOverlayFeatureRenderer<LivingEntity> SLIME_OVERLAY_FEATURE_RENDERER = new SlimeOverlayFeatureRenderer<>(new SlimeFeatureRendererContext(BOUNCYLIFE$SLIME_ENTITY_MODEL));
}
