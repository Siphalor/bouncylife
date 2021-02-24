package de.siphalor.bouncylife.enchantment;

import de.siphalor.bouncylife.SlimeForkItem;
import de.siphalor.bouncylife.mixin.MixinEnchantmentTarget;
import net.minecraft.item.Item;

@SuppressWarnings("unused")
public class ForkEnchantmentTarget extends MixinEnchantmentTarget {
	@Override
	public boolean isAcceptableItem(Item item) {
		return item instanceof SlimeForkItem;
	}
}
