package de.siphalor.bouncylife.mixin;

import de.siphalor.bouncylife.BouncyLife;
import de.siphalor.bouncylife.entity.PetSlimeEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MixinMobEntity extends LivingEntity {
	protected MixinMobEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(
			method = "interactMob",
			at = @At("TAIL"),
			cancellable = true
	)
	public void onInteract(PlayerEntity playerEntity, Hand hand, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (world.isClient) return;
		if (getClass().equals(SlimeEntity.class)) {
			ItemStack stack = playerEntity.getStackInHand(hand);
			Item item = stack.getItem();
			if (item == Items.HONEY_BOTTLE) {
				stack.decrement(1);
				playerEntity.giveItemStack(new ItemStack(Items.GLASS_BOTTLE));

				int randBound = 4 + ((SlimeEntity) (Object) this).getSize() - BouncyLife.getArmorSliminess(playerEntity);

				if (random.nextInt(randBound) == 0) {
					PetSlimeEntity petSlimeEntity = PetSlimeEntity.of((SlimeEntity) (Object) this);
					petSlimeEntity.setOwner(playerEntity);
					petSlimeEntity.setPersistent();
					removed = true;
					world.spawnEntity(petSlimeEntity);
					world.sendEntityStatus(petSlimeEntity, (byte) 7);
				} else {
					for (int i = 0; i < 7; i++) {
						double d = random.nextGaussian() * 0.02D;
						double e = random.nextGaussian() * 0.02D;
						double f = random.nextGaussian() * 0.02D;
						world.addParticle(ParticleTypes.SMOKE, getParticleX(1.0D), getRandomBodyY() + 0.5D, getParticleZ(1.0D), d, e, f);
					}
				}
			}
		}
	}
}
