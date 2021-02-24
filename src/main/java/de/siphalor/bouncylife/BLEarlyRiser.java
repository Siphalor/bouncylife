package de.siphalor.bouncylife;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class BLEarlyRiser implements Runnable {
	@Override
	public void run() {
		MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();

		String enchantmentTarget = mappingResolver.mapClassName("intermediary", "net.minecraft.class_1886");
		ClassTinkerers.enumBuilder(enchantmentTarget, new String[0])
				.addEnumSubclass("BOUNCYLIFE_FORK", "de.siphalor.bouncylife.enchantment.ForkEnchantmentTarget").build();
	}
}
