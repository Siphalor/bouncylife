package de.siphalor.bouncylife;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("WeakerAccess")
public class Core implements ModInitializer {
	public static final String MOD_ID = "bouncylife";
	public static final float VELOCITY_DAMPENER = 1.0F;
	public static final float FORK_MULTIPLIER = 1.8F;
	public static final float FORK_ENTITY_MULTIPLIER = FORK_MULTIPLIER * 1.5F;
	public static final float PLAYER_REACH = 5.0F;

	public static final Identifier BOUNCY_LIFE$SLIME_SKIN = new Identifier("textures/entity/slime/slime.png");

	public static ArmorMaterial slimeMaterial;

	public static ArmorItem helmet;
	public static ArmorItem chestplate;
	public static ArmorItem leggings;
	public static ArmorItem shoes;

	public static SlimeForkItem slimeFork;

	@Override
	public void onInitialize() {
		slimeMaterial = new SlimeMaterial();

		helmet = new ArmorItem(slimeMaterial, EquipmentSlot.HEAD, new Item.Settings().itemGroup(ItemGroup.COMBAT));
		chestplate = new ArmorItem(slimeMaterial, EquipmentSlot.CHEST, new Item.Settings().itemGroup(ItemGroup.COMBAT));
		leggings = new ArmorItem(slimeMaterial, EquipmentSlot.LEGS, new Item.Settings().itemGroup(ItemGroup.COMBAT));
		shoes = new ArmorItem(slimeMaterial, EquipmentSlot.FEET, new Item.Settings().itemGroup(ItemGroup.COMBAT));
		slimeFork = new SlimeForkItem(new Item.Settings().durability(100).itemGroup(ItemGroup.MISC));

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "slime_helmet"), helmet);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "slime_chestplate"), chestplate);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "slime_leggings"), leggings);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "slime_shoes"), shoes);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "slime_fork"), slimeFork);
	}

	public static boolean isSlimeArmor(ItemStack stack) {
		Item item = stack.getItem();
		return item instanceof ArmorItem && ((ArmorItem) item).getMaterial() instanceof SlimeMaterial;
	}

	public static void applySlimeThorns(Entity attackedEntity, DamageSource damageSource, float baseDamage, float resultedDamage) {
		if(damageSource instanceof EntityDamageSource && damageSource.getAttacker() != null) {
            float slimeAmount = 0.0F;
            for(ItemStack stack : attackedEntity.getArmorItems()) {
            	if(Core.isSlimeArmor(stack)) {
					slimeAmount += (float)((ArmorItem) stack.getItem()).getProtection();
				}
			}
			damageSource.getAttacker().damage(DamageSource.thorns(attackedEntity), (baseDamage - resultedDamage) * slimeAmount / 7.0F);
		}
	}
}
