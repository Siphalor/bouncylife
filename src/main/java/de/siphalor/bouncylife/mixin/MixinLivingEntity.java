/*
 * Copyright 2021 Siphalor
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.siphalor.bouncylife.mixin;

import de.siphalor.bouncylife.BouncyLife;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
	@Shadow public abstract Iterable<ItemStack> getArmorItems();

	@Shadow @Final private DefaultedList<ItemStack> syncedArmorStacks;

	public MixinLivingEntity(EntityType<?> entityType_1, World world_1) {
		super(entityType_1, world_1);
	}

	private float bouncylife$damageAmount = 0.0F;

	@Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
	public void handleFallDamage(float fallDistance, float fallMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> callbackInfo) {
        for(ItemStack stack : getArmorItems()) {
        	if(stack.getItem() == BouncyLife.shoes) {
        		callbackInfo.setReturnValue(super.handleFallDamage(fallDistance, fallMultiplier, damageSource));
        		return;
			}
		}
	}

	@Inject(method = "applyDamage", at = @At("HEAD"))
	public void onApplyDamageHead(DamageSource damageSource, float amount, CallbackInfo callbackInfo) {
		bouncylife$damageAmount = amount;
	}

	@Inject(method = "applyDamage", at = @At(value = "TAIL", target = "Lnet/minecraft/entity/LivingEntity;getHealth()F"))
	public void onApplyDamageTail(DamageSource damageSource, float amount, CallbackInfo callbackInfo) {
		BouncyLife.applySlimeThorns(this, damageSource, bouncylife$damageAmount, amount);
	}

	@Inject(method = "damage", at = @At("HEAD"), cancellable = true)
	public void damage(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if(!world.isClient()) {
        	if(damageSource == DamageSource.FLY_INTO_WALL) {
        		if(BouncyLife.isSlimeArmor(syncedArmorStacks.get(EquipmentSlot.HEAD.getEntitySlotId()))) {
					callbackInfoReturnable.setReturnValue(false);
				}
			}
		}
	}
}
