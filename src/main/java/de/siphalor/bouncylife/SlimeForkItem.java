package de.siphalor.bouncylife;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class SlimeForkItem extends Item {
	SlimeForkItem(Settings settings) {
		super(settings);
	}

	@SuppressWarnings("WeakerAccess")
	public static void playForkSound(World world, PlayerEntity player, BlockPos blockPos) {
		world.playSound(player, blockPos, SoundEvents.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.PLAYERS, 1.0F, 1.0F);
		world.playSound(player, blockPos, SoundEvents.ITEM_CROSSBOW_LOADING_END, SoundCategory.PLAYERS, 1.0F, 1.0F);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		player.setCurrentHand(hand);
		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE, SoundCategory.PLAYERS, 1.0F, 1.0F);
		return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity livingEntity, int useTime) {
		if(!world.isClient()) {
			Vec3d pos = livingEntity.getCameraPosVec(0.0F);
			Vec3d ray = pos.add(livingEntity.getRotationVector().multiply(BouncyLife.PLAYER_REACH));

			EntityHitResult entityHitResult = ProjectileUtil.getEntityCollision(world, livingEntity, pos, ray, livingEntity.getBoundingBox().expand(BouncyLife.PLAYER_REACH), entity -> true);
			if (entityHitResult != null) {
				Entity entity = entityHitResult.getEntity();
				shootEntity(livingEntity, entity, stack, useTime, BLConfig.forkEntityFactor + EnchantmentHelper.getLevel(BouncyLife.pushBackEnchantment, stack));
			} else {
				BlockHitResult blockHitResult = world.raycast(new RaycastContext(pos, ray, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, livingEntity));
				if (blockHitResult.getType() == BlockHitResult.Type.BLOCK) {
					shootEntity(livingEntity, livingEntity, stack, useTime, -BLConfig.forkFactor - EnchantmentHelper.getLevel(BouncyLife.dauntlessShotEnchantment, stack));
				} else {
					world.playSound(null, livingEntity.getBlockPos(), SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.PLAYERS, 1.0F, 1.0F);
				}
			}
		}
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 72000;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override
	public int getEnchantability() {
		return 2;
	}

	private void shootEntity(LivingEntity shooter, Entity entity, ItemStack stack, int useTime, float multiplier) {
		multiplier *= Math.min(20F, getMaxUseTime(stack) - useTime) / 20F;
		Vec3d velocity = shooter.getRotationVector().multiply(multiplier);
		entity.addVelocity(velocity.x, velocity.y, velocity.z);
		Packet<?> packet = new EntityVelocityUpdateS2CPacket(entity.getEntityId(), entity.getVelocity());
		for (ServerPlayerEntity player : PlayerLookup.tracking(entity)) {
			player.networkHandler.sendPacket(packet);
		}
		playForkSound(entity.world, null, entity.getBlockPos());
	}
}
