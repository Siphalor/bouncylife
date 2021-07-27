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

import com.chocohead.mm.api.ClassTinkerers;
import de.siphalor.bouncylife.enchantment.ForkPowerEnchantment;
import de.siphalor.bouncylife.entity.PetSlimeEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.tag.FabricTagBuilder;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlimeBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("WeakerAccess")
public class BouncyLife implements ModInitializer {
	public static final String MOD_ID = "bouncylife";
	public static final float PLAYER_REACH = 5.0F;

	public static Tag<Item> honeyTag;

	public static EnchantmentTarget forkEnchantmentTarget;
	public static ForkPowerEnchantment dauntlessShotEnchantment;
	public static ForkPowerEnchantment pushBackEnchantment;

	public static ItemGroup itemGroup;

	public static ArmorMaterial slimeMaterial;

	public static ArmorItem helmet;
	public static ArmorItem chestplate;
	public static ArmorItem leggings;
	public static ArmorItem shoes;

	public static SlimeForkItem slimeFork;

	public static Item slimeOnAStick;
	public static Item poppedSlime;

	public static Block[] slimeBlocks;

	public static EntityType<PetSlimeEntity> petSlimeEntityType;

	public static SoundEvent soundForkShoot;
	public static SoundEvent soundForkSnap;
	public static SoundEvent soundForkStretch;
	public static SoundEvent soundPetAmass;
	public static SoundEvent soundSlimePop;

	@Override
	public void onInitialize() {
		honeyTag = TagRegistry.item(new Identifier(MOD_ID, "honey"));

		forkEnchantmentTarget = ClassTinkerers.getEnum(EnchantmentTarget.class, "BOUNCYLIFE_FORK");

		dauntlessShotEnchantment = register(Registry.ENCHANTMENT, "dauntless_shot", new ForkPowerEnchantment(Enchantment.Rarity.UNCOMMON, 5));
		pushBackEnchantment      = register(Registry.ENCHANTMENT, "push_back",      new ForkPowerEnchantment(Enchantment.Rarity.COMMON,   5));

		itemGroup = FabricItemGroupBuilder.build(new Identifier(MOD_ID, "main"), () -> new ItemStack(slimeFork))
				.setEnchantments(forkEnchantmentTarget);

		slimeMaterial = new SlimeMaterial();

		helmet        = register(Registry.ITEM, "slime_helmet",     new ArmorItem(slimeMaterial, EquipmentSlot.HEAD,  new Item.Settings().group(itemGroup)));
		chestplate    = register(Registry.ITEM, "slime_chestplate", new ArmorItem(slimeMaterial, EquipmentSlot.CHEST, new Item.Settings().group(itemGroup)));
		leggings      = register(Registry.ITEM, "slime_leggings",   new ArmorItem(slimeMaterial, EquipmentSlot.LEGS,  new Item.Settings().group(itemGroup)));
		shoes         = register(Registry.ITEM, "slime_shoes",      new ArmorItem(slimeMaterial, EquipmentSlot.FEET,  new Item.Settings().group(itemGroup)));
		slimeFork     = register(Registry.ITEM, "slime_fork",       new SlimeForkItem(new Item.Settings().maxDamage(100).group(itemGroup)));
		slimeOnAStick = register(Registry.ITEM, "slime_on_a_stick", new Item(new Item.Settings().maxCount(1).group(itemGroup)));
		poppedSlime   = register(Registry.ITEM, "popped_slime",     new PoppedSlimeItem(
				new Item.Settings().maxCount(1).group(itemGroup).food(new FoodComponent.Builder().hunger(3).saturationModifier(0.8F).build()))
		);

		slimeBlocks = new Block[DyeColor.values().length];
		for (DyeColor color : DyeColor.values()) {
			Block block = new SlimeBlock(FabricBlockSettings.copyOf(Blocks.SLIME_BLOCK).materialColor(color.getMapColor()));
			Identifier identifier = new Identifier(MOD_ID, color.getName() + "_slime_block");
			Registry.register(Registry.BLOCK, identifier, block);
			BlockItem item = new BlockItem(block, new Item.Settings().group(itemGroup));
			slimeBlocks[color.getId()] = block;
			Registry.register(Registry.ITEM, identifier, item);
			item.appendBlocks(Item.BLOCK_ITEMS, item);

			Registry.register(Registry.ITEM, new Identifier(MOD_ID, color.getName() + "_slime_ball"), new Item(new Item.Settings().group(itemGroup)));
		}

		petSlimeEntityType = register(Registry.ENTITY_TYPE, "pet_slime",
				FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, PetSlimeEntity::new)
						.dimensions(EntityDimensions.changing(2.04F, 2.04F))
						.build()
		);
		FabricDefaultAttributeRegistry.register(petSlimeEntityType, PetSlimeEntity.createAttributes());

		soundForkShoot   = registerSound("slime_fork.shoot");
		soundForkSnap    = registerSound("slime_fork.snap");
		soundForkStretch = registerSound("slime_fork.stretch");
		soundPetAmass    = registerSound("pet_slime.amass");
		soundSlimePop    = registerSound("popped_slime.pop");

		AttackEntityCallback.EVENT.register((playerEntity, world, hand, entity, entityHitResult) -> {
			if (!world.isClient()) {
				if (
						(entity instanceof SlimeEntity && ((SlimeEntity) entity).getSize() == 1) ||
								(entity instanceof PetSlimeEntity && ((PetSlimeEntity) entity).getSize() == 1)
				) {
					if (playerEntity.getStackInHand(hand).getItem() == Items.STICK) {
						world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 1.0F);
						entity.remove(Entity.RemovalReason.KILLED);
						if (!playerEntity.isCreative()) {
							playerEntity.getStackInHand(hand).decrement(1);
						}
						playerEntity.giveItemStack(new ItemStack(slimeOnAStick));
						return ActionResult.SUCCESS;
					}
				}
			}
			return ActionResult.PASS;
		});
	}

	private static <T, S extends T> S register(Registry<T> registry, String id, S val) {
		return Registry.register(registry, new Identifier(MOD_ID, id), val);
	}

	private static SoundEvent registerSound(String id) {
		Identifier identifier = new Identifier(MOD_ID, id);
		return Registry.register(Registry.SOUND_EVENT, identifier, new SoundEvent(identifier));
	}

	public static boolean isSlimeArmor(ItemStack stack) {
		Item item = stack.getItem();
		return item instanceof ArmorItem && ((ArmorItem) item).getMaterial() instanceof SlimeMaterial;
	}

	public static boolean hasCompleteSlimeArmor(PlayerEntity playerEntity) {
		for (ItemStack armorStack : playerEntity.getArmorItems()) {
			if (!isSlimeArmor(armorStack))
				return false;
		}
		return true;
	}

	public static int getArmorSliminess(PlayerEntity playerEntity) {
		int result = 0;
		for (ItemStack armorStack : playerEntity.getArmorItems()) {
			if (isSlimeArmor(armorStack))
				result++;
		}
		return result;
	}

	public static void applySlimeThorns(Entity attackedEntity, DamageSource damageSource, float baseDamage, float resultedDamage) {
		if (!BLConfig.bounce.slimeArmorThorns) {
			return;
		}
		if(damageSource instanceof EntityDamageSource && damageSource.getAttacker() != null) {
            float slimeAmount = 0.0F;
            for(ItemStack stack : attackedEntity.getArmorItems()) {
            	if(BouncyLife.isSlimeArmor(stack)) {
					slimeAmount += (float)((ArmorItem) stack.getItem()).getProtection();
				}
			}
            if(slimeAmount > 0.0F)
				damageSource.getAttacker().damage(DamageSource.thorns(attackedEntity), (baseDamage - resultedDamage) * slimeAmount / 7.0F);
		}
	}
}
