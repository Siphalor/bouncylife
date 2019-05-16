package de.siphalor.bouncylife.mixin.client;

import de.siphalor.bouncylife.client.ClientCore;
import de.siphalor.bouncylife.util.IPlayerEntityModel;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {

	@Shadow protected EntityModel model;

	@Inject(method = "renderFeatures", at = @At("HEAD"), cancellable = true)
	public void renderFeatures(LivingEntity livingEntity, float f1, float f2, float f3, float f4, float f5, float f6, float f7, CallbackInfo callbackInfo) {
		if(model instanceof IPlayerEntityModel && ((IPlayerEntityModel) model).bouncylife$isDisguisedAsSlime()) {
			ClientCore.SLIME_OVERLAY_FEATURE_RENDERER.render(livingEntity, f1, f2, f3, f4, f5, f6, f7);
			callbackInfo.cancel();
		}
	}
}
