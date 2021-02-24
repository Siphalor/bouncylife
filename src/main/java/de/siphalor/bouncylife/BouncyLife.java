package de.siphalor.bouncylife;

import com.chocohead.mm.api.ClassTinkerers;
import de.siphalor.bouncylife.enchantment.ForkPowerEnchantment;
import de.siphalor.bouncylife.entity.PetSlimeEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
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
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("WeakerAccess")
public class BouncyLife implements ModInitializer {
	public static final String MOD_ID = "bouncylife";
	public static final float PLAYER_REACH = 5.0F;

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

	public static EnchantmentTarget forkEnchantmentTarget;
	public static ForkPowerEnchantment dauntlessShotEnchantment;
	public static ForkPowerEnchantment pushBackEnchantment;

	@Override
	public void onInitialize() {
		slimeMaterial = new SlimeMaterial();

		helmet = new ArmorItem(slimeMaterial, EquipmentSlot.HEAD, new Item.Settings().group(ItemGroup.COMBAT));
		chestplate = new ArmorItem(slimeMaterial, EquipmentSlot.CHEST, new Item.Settings().group(ItemGroup.COMBAT));
		leggings = new ArmorItem(slimeMaterial, EquipmentSlot.LEGS, new Item.Settings().group(ItemGroup.COMBAT));
		shoes = new ArmorItem(slimeMaterial, EquipmentSlot.FEET, new Item.Settings().group(ItemGroup.COMBAT));
		slimeFork = new SlimeForkItem(new Item.Settings().maxDamage(100).group(ItemGroup.MISC));
		slimeOnAStick = new Item(new Item.Settings().maxCount(1));
		poppedSlime = new PoppedSlimeItem(new Item.Settings().maxCount(1).group(ItemGroup.FOOD).food(new FoodComponent.Builder().hunger(3).saturationModifier(0.8F).build()));

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "slime_helmet"), helmet);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "slime_chestplate"), chestplate);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "slime_leggings"), leggings);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "slime_shoes"), shoes);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "slime_fork"), slimeFork);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "slime_on_a_stick"), slimeOnAStick);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "popped_slime"), poppedSlime);

		slimeBlocks = new Block[DyeColor.values().length];
		for (DyeColor color : DyeColor.values()) {
			Block block = new SlimeBlock(FabricBlockSettings.copyOf(Blocks.SLIME_BLOCK).materialColor(color.getMaterialColor()));
			Identifier identifier = new Identifier(MOD_ID, color.getName() + "_slime_block");
			Registry.register(Registry.BLOCK, identifier, block);
			BlockItem item = new BlockItem(block, new Item.Settings().group(ItemGroup.DECORATIONS));
			slimeBlocks[color.getId()] = block;
			Registry.register(Registry.ITEM, identifier, item);
			item.appendBlocks(Item.BLOCK_ITEMS, item);

			Registry.register(Registry.ITEM, new Identifier(MOD_ID, color.getName() + "_slime_ball"), new Item(new Item.Settings().group(ItemGroup.MISC)));
		}

		petSlimeEntityType = FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, PetSlimeEntity::new)
				.dimensions(EntityDimensions.changing(2.04F, 2.04F))
				.build();
		Registry.register(Registry.ENTITY_TYPE, new Identifier(MOD_ID, "pet_slime"), petSlimeEntityType);
		FabricDefaultAttributeRegistry.register(petSlimeEntityType, PetSlimeEntity.createAttributes());

		forkEnchantmentTarget = ClassTinkerers.getEnum(EnchantmentTarget.class, "BOUNCYLIFE_FORK");
		dauntlessShotEnchantment = register(Registry.ENCHANTMENT, "dauntless_shot", new ForkPowerEnchantment(Enchantment.Rarity.UNCOMMON, 5));
		pushBackEnchantment      = register(Registry.ENCHANTMENT, "push_back",      new ForkPowerEnchantment(Enchantment.Rarity.COMMON,   5));

		AttackEntityCallback.EVENT.register((playerEntity, world, hand, entity, entityHitResult) -> {
			if (!world.isClient()) {
				if (
						(entity instanceof SlimeEntity && ((SlimeEntity) entity).getSize() == 1) ||
								(entity instanceof PetSlimeEntity && ((PetSlimeEntity) entity).getSize() == 1)
				) {
					if (playerEntity.getStackInHand(hand).getItem() == Items.STICK) {
						world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 1.0F);
						entity.remove();
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
