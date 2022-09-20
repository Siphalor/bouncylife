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
import de.siphalor.bouncylife.entity.PetSlimeEntity;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
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
			at = @At("TAIL")
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
				double randBound = 1D / (1.667 - 0.25 * BouncyLife.getArmorSliminess(player));

				if (random.nextDouble() < randBound) {
					PetSlimeEntity petSlimeEntity = PetSlimeEntity.of((SlimeEntity) (Object) this);
					petSlimeEntity.setOwner(player);
					petSlimeEntity.setPersistent();
					removed = true;
					world.spawnEntity(petSlimeEntity);
					world.sendEntityStatus(petSlimeEntity, (byte) 7);
				} else {
					Packet<?> packet = new ParticleS2CPacket(ParticleTypes.SMOKE, false, getX(), getY(), getZ(), getWidth() * 0.5F, getHeight() * 0.5F, getWidth() * 0.5F, 0.03F, size * 4);
					for (ServerPlayerEntity sp : PlayerLookup.tracking(this)) {
						sp.networkHandler.sendPacket(packet);
					}
				}
			}
		}
	}
}
