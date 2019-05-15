package de.siphalor.bouncylife;

import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.client.network.packet.EntityVelocityUpdateS2CPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.Packet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

public class SlimeForkItem extends Item {
	SlimeForkItem(Settings item$Settings_1) {
		super(item$Settings_1);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext usageContext) {
		/*PlayerEntity playerEntity = usageContext.getPlayer();
		if (playerEntity == null) return super.useOnBlock(usageContext);
		 Vec3d vector = playerEntity.getRotationVector().multiply(-Core.FORK_MULTIPLIER);
		 playerEntity.addVelocity(vector.x, vector.y, vector.z);
		 usageContext.getItemStack().applyDamage(1, usageContext.getPlayer(), playerEntity1 -> usageContext.getWorld().playSound(playerEntity1, playerEntity1.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F));
		playForkSound(usageContext.getWorld(), usageContext.getPlayer(), usageContext.getBlockPos());
        return ActionResult.SUCCESS;*/
		System.out.println("use");
        return super.useOnBlock(usageContext);
	}

	@Override
	public boolean interactWithEntity(ItemStack itemStack, PlayerEntity playerEntity, LivingEntity livingEntity, Hand hand) {
		if(!playerEntity.world.isClient()) {
			Vec3d vector = playerEntity.getRotationVector().multiply(Core.FORK_MULTIPLIER);
			livingEntity.addVelocity(vector.x, vector.y, vector.z);
			itemStack.applyDamage(1, playerEntity, playerEntity1 -> playerEntity.sendToolBreakStatus(hand));
			playForkSound(playerEntity.world, null, livingEntity.getBlockPos());
		}
        return true;
	}

	public static void playForkSound(World world, PlayerEntity playerEntity, BlockPos blockPos) {
		world.playSound(playerEntity, blockPos, SoundEvents.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.PLAYERS, 1.0F, 1.0F);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
		playerEntity.setCurrentHand(hand);
		world.playSound(null, playerEntity.getBlockPos(), SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE, SoundCategory.PLAYERS, 1.0F, 1.0F);
		return new TypedActionResult<>(ActionResult.SUCCESS, playerEntity.getStackInHand(hand));
	}

	@Override
	public void onItemStopUsing(ItemStack itemStack, World world, LivingEntity livingEntity, int useTime) {
		if(!world.isClient()) {
			Vec3d pos = livingEntity.getCameraPosVec(0.0F);
			BlockHitResult blockHitResult = world.rayTrace(new RayTraceContext(pos, pos.add(livingEntity.getRotationVector().multiply(4.5F)), RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, livingEntity));
            if(blockHitResult.getType() == BlockHitResult.Type.BLOCK) {
            	Vec3d velocity = livingEntity.getRotationVector().multiply(-Core.FORK_MULTIPLIER * Math.min(20, (float) getMaxUseTime(itemStack) - (float) useTime) / 20.0F);
            	livingEntity.addVelocity(velocity.x, velocity.y, velocity.z);
				Packet packet = new EntityVelocityUpdateS2CPacket(livingEntity.getEntityId(), livingEntity.getVelocity());
				PlayerStream.all(world.getServer()).forEach(serverPlayerEntity -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(serverPlayerEntity, packet));
				world.playSound(null, livingEntity.getBlockPos(), SoundEvents.ITEM_CROSSBOW_LOADING_END, SoundCategory.PLAYERS, 1.0F, 1.0F);
			} else {
            	world.playSound(null, livingEntity.getBlockPos(), SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
		}
	}

	@Override
	public int getMaxUseTime(ItemStack itemStack_1) {
		return 72000;
	}

	@Override
	public UseAction getUseAction(ItemStack itemStack_1) {
		return UseAction.BLOCK;
	}
}
