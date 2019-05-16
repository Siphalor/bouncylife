package de.siphalor.bouncylife;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class SlimeMaterial implements ArmorMaterial {
	public static final int[] BASE_DURABILITY = new int[]{13, 15, 16, 11};
	protected static final Ingredient REPAIR_INGREDIENT = Ingredient.ofItems(Items.SLIME_BLOCK);

	@Override
	public int getDurability(EquipmentSlot equipmentSlot) {
		return BASE_DURABILITY[equipmentSlot.getEntitySlotId()] * 10;
	}

	@Override
	public int getProtectionAmount(EquipmentSlot equipmentSlot) {
        switch (equipmentSlot) {
			case HEAD:
			case FEET:
				return 1;
			case CHEST:
				return 3;
			case LEGS:
				return 2;
		}
		return 0;
	}

	@Override
	public int getEnchantability() {
		return 0;
	}

	@Override
	public SoundEvent getEquipSound() {
		return SoundEvents.BLOCK_SLIME_BLOCK_PLACE;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return REPAIR_INGREDIENT;
	}

	@Override
	public String getName() {
		return Core.MOD_ID + "_slime";
	}

	@Override
	public float getToughness() {
		return 0;
	}
}
