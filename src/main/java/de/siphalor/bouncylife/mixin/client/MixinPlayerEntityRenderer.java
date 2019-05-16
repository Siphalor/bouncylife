package de.siphalor.bouncylife.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager;
import de.siphalor.bouncylife.Core;
import de.siphalor.bouncylife.util.IPlayerEntity;
import de.siphalor.bouncylife.util.IPlayerEntityModel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
	MixinPlayerEntityRenderer(EntityRenderDispatcher entityRenderDispatcher_1, PlayerEntityModel<AbstractClientPlayerEntity> entityModel_1, float float_1) {
		super(entityRenderDispatcher_1, entityModel_1, float_1);
	}

	@Inject(method = "method_4215", at = @At("HEAD"))
	public void onRender(AbstractClientPlayerEntity clientPlayerEntity, double double_1, double double_2, double double_3, float float_1, float float_2, CallbackInfo callbackInfo) {
		if(model instanceof IPlayerEntityModel)
			((IPlayerEntityModel) model).bouncylife$setSlimeDisguise(clientPlayerEntity instanceof IPlayerEntity && ((IPlayerEntity) clientPlayerEntity).bouncylife$isDisguisedAsSlime());
	}

	@Inject(method = "method_4216", at = @At("HEAD"), cancellable = true)
	public void getTextureIdentifier(AbstractClientPlayerEntity clientPlayerEntity, CallbackInfoReturnable<Identifier> callbackInfoReturnable) {
		if(model instanceof IPlayerEntityModel && ((IPlayerEntityModel) model).bouncylife$isDisguisedAsSlime())
			callbackInfoReturnable.setReturnValue(Core.BOUNCY_LIFE$SLIME_SKIN);
	}

	@Inject(method = "method_4212", at = @At("HEAD"), cancellable = true)
	public void setupTransforms(AbstractClientPlayerEntity clientPlayerEntity, float float_1, float float_2, float float_3, CallbackInfo callbackInfo) {
		if(model instanceof IPlayerEntityModel && ((IPlayerEntityModel) model).bouncylife$isDisguisedAsSlime()) {
			super.setupTransforms(clientPlayerEntity, float_1, float_2, float_3);
			GlStateManager.scalef(3.5F, 3.5F, 3.5F);
			callbackInfo.cancel();
		}
	}

}
