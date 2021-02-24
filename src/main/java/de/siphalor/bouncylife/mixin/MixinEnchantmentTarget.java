package de.siphalor.bouncylife.mixin;

import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings({"UnusedMixin", "unused"})
@Mixin(EnchantmentTarget.class)
public abstract class MixinEnchantmentTarget {
	@Shadow
	public abstract boolean isAcceptableItem(Item item);
}
