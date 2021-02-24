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

	@Shadow @Final private DefaultedList<ItemStack> equippedArmor;

	public MixinLivingEntity(EntityType<?> entityType_1, World world_1) {
		super(entityType_1, world_1);
	}

	private float bouncylife$damageAmount = 0.0F;

	@Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
	public void handleFallDamage(float float_1, float float_2, CallbackInfoReturnable<Boolean> callbackInfo) {
        for(ItemStack stack : getArmorItems()) {
        	if(stack.getItem() == BouncyLife.shoes) {
        		callbackInfo.setReturnValue(super.handleFallDamage(float_1, float_2));
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
        		if(BouncyLife.isSlimeArmor(equippedArmor.get(EquipmentSlot.HEAD.getEntitySlotId()))) {
					callbackInfoReturnable.setReturnValue(false);
				}
			}
		}
	}
}
