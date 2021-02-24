package de.siphalor.bouncylife.mixin;

import de.siphalor.bouncylife.BLConfig;
import de.siphalor.bouncylife.BouncyLife;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class MixinBlock {
	@SuppressWarnings("WeakerAccess")
	@Inject(method = "onEntityLand", at = @At("HEAD"), cancellable = true)
	public void onEntityLand(BlockView blockView, Entity entity, CallbackInfo callbackInfo) {
		if(entity instanceof LivingEntity && Math.abs(entity.getVelocity().getY()) > BLConfig.bounce.yBounceTolerance) {
            for(ItemStack stack : entity.getArmorItems()) {
            	if(stack.getItem() == BouncyLife.shoes) {
            		entity.setVelocity(entity.getVelocity().multiply(1.0F, entity.isSneaking() ? -BLConfig.bounce.sneakVelocityDampener : -BLConfig.bounce.velocityDampener, 1.0F));
            		entity.world.playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_SLIME_BLOCK_FALL, SoundCategory.PLAYERS, 1.0F, 0.5F);
            		callbackInfo.cancel();
            		return;
				}
			}
		}
	}
}
