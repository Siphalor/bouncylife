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
