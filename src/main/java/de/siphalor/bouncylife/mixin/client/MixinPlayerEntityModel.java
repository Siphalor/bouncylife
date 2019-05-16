package de.siphalor.bouncylife.mixin.client;

import de.siphalor.bouncylife.Core;
import de.siphalor.bouncylife.util.IPlayerEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public class MixinPlayerEntityModel implements IPlayerEntityModel {
	protected boolean bouncylife$slimeDisguise;

	@Inject(method = "method_17088", at = @At("HEAD"), cancellable = true)
	public void render(LivingEntity livingEntity, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6, CallbackInfo callbackInfo) {
		if(bouncylife$slimeDisguise) {
			Core.BOUNCYLIFE$SLIME_ENTITY_MODEL.render(livingEntity, float_1, float_2, float_3, float_4, float_5, float_6);
			callbackInfo.cancel();
		}
	}

	@Override
	public boolean bouncylife$isDisguisedAsSlime() {
		return bouncylife$slimeDisguise;
	}

	@Override
	public void bouncylife$setSlimeDisguise(boolean visible) {
		bouncylife$slimeDisguise = visible;
	}
}
