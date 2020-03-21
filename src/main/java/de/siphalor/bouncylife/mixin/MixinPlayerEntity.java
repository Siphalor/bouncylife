package de.siphalor.bouncylife.mixin;

import de.siphalor.bouncylife.BouncyLife;
import de.siphalor.bouncylife.util.IPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity implements IPlayerEntity {
	@Shadow public abstract Iterable<ItemStack> getArmorItems();

	private float bouncylife$damageAmount = 0.0F;

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType_1, World world_1) {
		super(entityType_1, world_1);
	}

	@Inject(method = "applyDamage", at = @At("HEAD"))
	public void onApplyDamageHead(DamageSource damageSource, float amount, CallbackInfo callbackInfo) {
		bouncylife$damageAmount = amount;
	}

	@Inject(method = "applyDamage", at = @At("TAIL"))
	public void onApplyDamageTail(DamageSource damageSource, float amount, CallbackInfo callbackInfo) {
		BouncyLife.applySlimeThorns(this, damageSource, bouncylife$damageAmount, amount);
	}
}
