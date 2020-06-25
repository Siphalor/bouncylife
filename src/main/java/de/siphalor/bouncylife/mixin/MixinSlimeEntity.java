package de.siphalor.bouncylife.mixin;

import de.siphalor.bouncylife.BouncyLife;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SlimeEntity.class)
public abstract class MixinSlimeEntity {

	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = "method_18451(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	public void isPlayerMatching(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (livingEntity instanceof PlayerEntity && BouncyLife.hasCompleteSlimeArmor((PlayerEntity) livingEntity))
			callbackInfoReturnable.setReturnValue(false);
	}
}
