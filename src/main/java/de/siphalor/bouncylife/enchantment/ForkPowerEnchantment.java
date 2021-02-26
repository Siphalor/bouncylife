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

package de.siphalor.bouncylife.enchantment;

import de.siphalor.bouncylife.BouncyLife;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;

public class ForkPowerEnchantment extends Enchantment {
	private final int maxLevel;

	public ForkPowerEnchantment(Rarity weight, int maxLevel) {
		super(weight, BouncyLife.forkEnchantmentTarget, new EquipmentSlot[]{ EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND });
		this.maxLevel = maxLevel;
	}

	@Override
	public int getMinPower(int level) {
		return 1 + 10 * (level - 1);
	}

	@Override
	public int getMaxPower(int level) {
		return getMinPower(level) + 50;
	}

	@Override
	public int getMaxLevel() {
		return maxLevel;
	}
}
