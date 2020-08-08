package de.siphalor.bouncylife;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class PoppedSlimeItem extends Item {
	public PoppedSlimeItem(Settings settings) {
		super(settings);
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		super.onStoppedUsing(stack, world, user, remainingUseTicks);
		if (!world.isClient()) {
			if (user instanceof PlayerEntity) {
				user.setStackInHand(user.getActiveHand(), new ItemStack(Items.STICK));
			}
		}
	}
}
