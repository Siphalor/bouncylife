package de.siphalor.bouncylife.mixin;

import de.siphalor.bouncylife.Config;
import de.siphalor.bouncylife.Core;
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
		if(entity instanceof LivingEntity && Math.abs(entity.getVelocity().getY()) > Config.Y_BOUNCE_TOLERANCE.value) {
            for(ItemStack stack : entity.getArmorItems()) {
            	if(stack.getItem() == Core.shoes) {
            		entity.setVelocity(entity.getVelocity().multiply(1.0F, entity.isSneaking() ? -Config.SNEAK_VELOCITY_DAMPENER.value : -Config.VELOCITY_DAMPENER.value, 1.0F));
            		entity.world.playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_SLIME_BLOCK_FALL, SoundCategory.PLAYERS, 1.0F, 0.5F);
            		callbackInfo.cancel();
            		return;
				}
			}
		}
	}
}
