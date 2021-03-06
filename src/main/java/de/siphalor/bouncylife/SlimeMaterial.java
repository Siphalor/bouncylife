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
		return BouncyLife.MOD_ID + "_slime";
	}

	@Override
	public float getToughness() {
		return 0f;
	}

	@Override
	public float getKnockbackResistance() {
		return -0.1F;
	}
}
