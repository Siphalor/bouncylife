package de.siphalor.bouncylife.mixin;

import de.siphalor.bouncylife.BouncyLife;
import de.siphalor.bouncylife.entity.PetSlimeEntity;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
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
	public void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (world.isClient) return;
		if (getClass().equals(SlimeEntity.class)) {
			ItemStack stack = player.getStackInHand(hand);
			Item item = stack.getItem();
			if (BouncyLife.honeyTag.contains(item)) {
				if (!player.isCreative()) {
					stack.decrement(1);
					if (item == Items.HONEY_BOTTLE) {
						player.giveItemStack(new ItemStack(Items.GLASS_BOTTLE));
					}
				}

				int size = ((SlimeEntity) (Object) this).getSize();
				int randBound = size * (4 + size - BouncyLife.getArmorSliminess(player));

				if (random.nextInt(randBound) == 0) {
					PetSlimeEntity petSlimeEntity = PetSlimeEntity.of((SlimeEntity) (Object) this);
					petSlimeEntity.setOwner(player);
					petSlimeEntity.setPersistent();
					removed = true;
					world.spawnEntity(petSlimeEntity);
					world.sendEntityStatus(petSlimeEntity, (byte) 7);
				} else {
					Packet<?> packet = new ParticleS2CPacket(ParticleTypes.SMOKE, false, getX(), getY(), getZ(), getWidth(), getHeight(), getWidth(), 0.1F, size * 4);
					for (ServerPlayerEntity sp : PlayerLookup.tracking(this)) {
						sp.networkHandler.sendPacket(packet);
					}
				}
			}
		}
	}
}
