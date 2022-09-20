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

package de.siphalor.bouncylife.entity;

import de.siphalor.bouncylife.BLConfig;
import de.siphalor.bouncylife.BLUtil;
import de.siphalor.bouncylife.BouncyLife;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class PetSlimeEntity extends TameableEntity implements JumpingMount {
	private static final TrackedData<Integer> SLIME_SIZE;
	private static final TrackedData<Integer> COLOR;
	private static final TrackedData<Boolean> SADDLED;

	private static final ParticleEffect[] PARTICLE_EFFECTS;

	private static final Ingredient TEMPT_INGREDIENT = Ingredient.ofItems(Items.HONEY_BOTTLE, Items.HONEY_BLOCK, Items.ROTTEN_FLESH);

	private static final Identifier PLAIN_LOOT_TABLE = new Identifier(BouncyLife.MOD_ID, "entities/pet_slime/plain");

	static {
		SLIME_SIZE = DataTracker.registerData(PetSlimeEntity.class, TrackedDataHandlerRegistry.INTEGER);
		COLOR = DataTracker.registerData(PetSlimeEntity.class, TrackedDataHandlerRegistry.INTEGER);
		SADDLED = DataTracker.registerData(PetSlimeEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

		PARTICLE_EFFECTS = Arrays.stream(BouncyLife.slimeBlocks)
				.filter(Objects::nonNull)
				.map(item -> new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(item)))
				.toArray(ParticleEffect[]::new);
	}

	public float targetStretch;
	public float stretch;
	public float lastStretch;
	protected int growthRandBound;
	private boolean onGroundLastTick;
	private float playerJumpBarMultiplier = 0F;
	private boolean playerJump;

	public PetSlimeEntity(EntityType<? extends PetSlimeEntity> entityType, World world) {
		super(entityType, world);
		this.moveControl = new PetSlimeEntity.SlimeMoveControl(this);
	}

	public static PetSlimeEntity of(SlimeEntity slimeEntity) {
		PetSlimeEntity petSlimeEntity = BouncyLife.petSlimeEntityType.create(slimeEntity.world);
		assert petSlimeEntity != null;
		petSlimeEntity.copyPositionAndRotation(slimeEntity);
		petSlimeEntity.setVelocity(slimeEntity.getVelocity());
		petSlimeEntity.setCustomName(slimeEntity.getCustomName());
		petSlimeEntity.setCustomNameVisible(slimeEntity.isCustomNameVisible());
		petSlimeEntity.setInvulnerable(slimeEntity.isInvulnerable());
		petSlimeEntity.setSize(slimeEntity.getSize(), false);
		petSlimeEntity.setHealth(slimeEntity.getHealth());
		petSlimeEntity.setAiDisabled(slimeEntity.isAiDisabled());
		petSlimeEntity.setInvulnerable(slimeEntity.isInvulnerable());
		return petSlimeEntity;
	}

	protected void initGoals() {
		this.goalSelector.add(1, new SwimGoal(this));
		this.goalSelector.add(2, new CombineWithMateGoal(this));
		this.goalSelector.add(3, new TemptGoal(this, 1D, TEMPT_INGREDIENT, false));
		this.goalSelector.add(4, new AttackGoal(this, 1D, true));
		this.goalSelector.add(5, new WanderAroundGoal(this, 1D, 5));
		this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
		this.targetSelector.add(2, new AttackWithOwnerGoal(this));
		this.targetSelector.add(3, (new RevengeGoal(this)).setGroupRevenge());
		this.targetSelector.add(4, new ActiveTargetGoal<>(this, ZombieEntity.class, false));
	}

	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(SLIME_SIZE, 1);
		this.dataTracker.startTracking(COLOR, -1);
		this.dataTracker.startTracking(SADDLED, false);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE);
	}

	@SuppressWarnings("ConstantConditions")
	public void setSize(int size, boolean heal) {
		this.dataTracker.set(SLIME_SIZE, size);
		this.refreshPosition();
		this.calculateDimensions();
		this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(Math.min(BLConfig.pets.maxHealthLimit, size * size));
		this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2F + 0.1F * (float) size);
		this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(size);
		this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_KNOCKBACK).setBaseValue(0.2F * (size - 1));
		this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(2 * (size - 1));
		if (heal) {
			this.setHealth(this.getMaxHealth());
		}

		this.growthRandBound = size * size + 2;

		this.experiencePoints = size;
	}

	public int getSize() {
		return this.dataTracker.get(SLIME_SIZE);
	}

	public void setColor(DyeColor dyeColor) {
		if (dyeColor == null) {
			dataTracker.set(COLOR, -1);
		} else {
			dataTracker.set(COLOR, dyeColor.getId());
		}
	}

	public DyeColor getColor() {
		Integer colorId = dataTracker.get(COLOR);
		if (colorId >= 0) {
			return DyeColor.byId(colorId);
		}
		return null;
	}

	public boolean isSaddled() {
		return dataTracker.get(SADDLED);
	}

	public void setSaddled(boolean saddled) {
		dataTracker.set(SADDLED, saddled);
	}

	public void writeCustomDataToNbt(NbtCompound tag) {
		super.writeCustomDataToNbt(tag);
		tag.putInt("Size", this.getSize() - 1);
		if (getColor() != null)
			tag.putString("Color", getColor().getName());
		tag.putBoolean("Saddled", isSaddled());
		tag.putBoolean("wasOnGround", this.onGroundLastTick);
	}

	public void readCustomDataFromNbt(NbtCompound tag) {
		int i = tag.getInt("Size");
		if (i < 0) {
			i = 0;
		}

		setColor(DyeColor.byName(tag.getString("Color"), null));
		dataTracker.set(SADDLED, tag.getBoolean("Saddled"));
		this.setSize(i + 1, false);
		super.readCustomDataFromNbt(tag);
		this.onGroundLastTick = tag.getBoolean("wasOnGround");
	}

	public boolean isSmall() {
		return this.getSize() <= 1;
	}

	protected ParticleEffect getParticles() {
		int color = dataTracker.get(COLOR);
		if (color == -1) {
			return ParticleTypes.ITEM_SLIME;
		}
		return PARTICLE_EFFECTS[color];
	}

	public void tick() {
		this.stretch += (this.targetStretch - this.stretch) * 0.5F;
		this.lastStretch = this.stretch;
		super.tick();
		if (this.onGround && !this.onGroundLastTick) {
			int i = this.getSize();

			for (int j = 0; j < i * 8; ++j) {
				float f = this.random.nextFloat() * 6.2831855F;
				float g = this.random.nextFloat() * 0.5F + 0.5F;
				float h = MathHelper.sin(f) * (float) i * 0.5F * g;
				float k = MathHelper.cos(f) * (float) i * 0.5F * g;
				this.world.addParticle(this.getParticles(), this.getX() + (double) h, this.getY(), this.getZ() + (double) k, 0.0D, 0.0D, 0.0D);
			}

			this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) / 0.8F);
			this.targetStretch = -0.5F;

			if (playerJump) {
				performAreaAttack();
				playerJump = false;
			}
		} else if (!this.onGround && this.onGroundLastTick) {
			this.targetStretch = 1.0F;
		}

		this.onGroundLastTick = this.onGround;
		this.updateStretch();
	}

	@Override
	public void travel(Vec3d movementInput) {
		if (isAlive()) {
			Entity rider = getPrimaryPassenger();
			if (rider != null) {
				setYaw(rider.getYaw());
				prevYaw = rider.getYaw();
				setPitch(rider.getPitch());
				setRotation(getYaw(), getPitch());
				bodyYaw = rider.getYaw();
				headYaw = rider.getYaw();
			}
			super.travel(movementInput);
		}
	}

	@Override
	public void move(MovementType type, Vec3d movement) {
		if (type == MovementType.PLAYER) { // This is required because the client otherwise forces weird things
			return;
		}
		super.move(type, movement);
	}

	@Nullable
	@Override
	public Entity getPrimaryPassenger() {
		return getPassengerList().isEmpty() ? null : getPassengerList().get(0);
	}

	@Override
	public double getMountedHeightOffset() {
		return getHeight() + stretch * getSize() * 0.2 - 0.3;
	}

	protected void updateStretch() {
		this.targetStretch *= 0.6F;
	}

	protected int getTicksUntilNextJump() {
		if (hasPassengers()) {
			return 18;
		}
		return this.random.nextInt(20) + 30;
	}

	public void calculateDimensions() {
		double d = this.getX();
		double e = this.getY();
		double f = this.getZ();
		super.calculateDimensions();
		this.setPosition(d, e, f);
	}

	public void onTrackedDataSet(TrackedData<?> data) {
		if (SLIME_SIZE.equals(data)) {
			this.calculateDimensions();
			setYaw(headYaw);
			setBodyYaw(headYaw);
			if (this.isTouchingWater() && this.random.nextInt(20) == 0) {
				this.onSwimmingStart();
			}
		}

		super.onTrackedDataSet(data);
	}

	public EntityType<?> getType() {
		return super.getType();
	}

	@Override
	public void remove(RemovalReason removalReason) {
		int size = this.getSize();
		if (!this.world.isClient && size > 1 && this.getHealth() <= 0.0F) {
			if (isSaddled()) {
				ItemEntity itemEntity = new ItemEntity(world, getX(), getY(), getZ(), new ItemStack(Items.SADDLE));
				world.spawnEntity(itemEntity);
			}

			Text text = this.getCustomName();
			boolean bl = this.isAiDisabled();
			float f = (float) size / 4.0F;
			int j = size / 2;
			int k = 2 + this.random.nextInt(3);

			for (int l = 0; l < k; ++l) {
				float g = ((float) (l % 2) - 0.5F) * f;
				float h = ((float) (l / 2) - 0.5F) * f;
				PetSlimeEntity slimeEntity = (PetSlimeEntity) this.getType().create(this.world);
				assert slimeEntity != null;
				if (this.isPersistent()) {
					slimeEntity.setPersistent();
				}

				slimeEntity.setOwnerUuid(getOwnerUuid());
				slimeEntity.setCustomName(text);
				slimeEntity.setAiDisabled(bl);
				slimeEntity.setInvulnerable(this.isInvulnerable());
				slimeEntity.setSize(j, true);
				slimeEntity.setColor(getColor());
				slimeEntity.refreshPositionAndAngles(this.getX() + (double) g, this.getY() + 0.5D, this.getZ() + (double) h, this.random.nextFloat() * 360.0F, 0.0F);
				this.world.spawnEntity(slimeEntity);
			}
		}

		super.remove(removalReason);
	}

	public void pushAwayFrom(Entity entity) {
		super.pushAwayFrom(entity);
		if (getTarget() == entity) {
			damage((LivingEntity) entity);
		}
	}

	protected void damage(LivingEntity target) {
		if (this.isAlive()) {
			int i = this.getSize();
			if (this.squaredDistanceTo(target) < 0.6D * (double) i * 0.6D * (double) i && this.canSee(target) && target.damage(DamageSource.mob(this), this.getDamageAmount())) {
				this.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
				this.applyDamageEffects(this, target);
			}
		}

	}

	protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
		return 0.625F * dimensions.height;
	}

	protected float getDamageAmount() {
		return (float) this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).getValue();
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (isOwner(player)) {
			ItemStack stack = player.getStackInHand(hand);
			if (canEat() && isBreedingItem(stack) && getSize() < BLConfig.pets.breedSizeLimit) {
				eat(player, hand, stack);
				if (!world.isClient) {
					lovePlayer(player);
				}
				return ActionResult.success(world.isClient);
			}
			if (getHealth() < getMaxHealth()) {
				if (TEMPT_INGREDIENT.test(stack)) {
					eat(player, hand, stack);
					if (!world.isClient) {
						setHealth(Math.min(getMaxHealth(), getHealth() + 1F));
						world.sendEntityStatus(this, (byte) 7);
						return ActionResult.SUCCESS;
					}
					return ActionResult.CONSUME;
				}
			}
			if (
					(BLConfig.pets.enableHoneyAmassing && BouncyLife.honeyTag.contains(stack.getItem()))
							|| (BLConfig.pets.enableRottenFleshAmassing && stack.getItem() == Items.ROTTEN_FLESH)
			) {
				if (getSize() < BLConfig.pets.amassSizeLimit) {
					eat(player, hand, stack);

					world.playSoundFromEntity(player, this, BouncyLife.soundPetAmass, SoundCategory.NEUTRAL, 2F, 1F);
					if (!world.isClient) {
						growthRandBound = Math.max(0, growthRandBound);
						if (random.nextInt(growthRandBound) == 0) {
							setSize(getSize() + 1, true);
						}
						world.sendEntityStatus(this, (byte) 8);
						return ActionResult.SUCCESS;
					}
					return ActionResult.CONSUME;
				}
			}
			if (stack.getItem() instanceof DyeItem) {
				DyeColor nextColor = ((DyeItem) stack.getItem()).getColor();
				if (nextColor != getColor()) {
					if (!player.isCreative()) {
						stack.decrement(1);
					}
					if (world.isClient()) {
						return ActionResult.CONSUME;
					}
					setColor(((DyeItem) stack.getItem()).getColor());
					return ActionResult.SUCCESS;
				}
			}
			if (stack.getItem() == Items.SADDLE) {
				if (!isSaddled() && getSize() >= 2) {
					world.playSound(player, getX(), getY(), getZ(), SoundEvents.ENTITY_PIG_SADDLE, SoundCategory.NEUTRAL, 0.5F, 1F);
					if (!player.isCreative()) {
						stack.decrement(1);
					}
					if (world.isClient()) {
						return ActionResult.CONSUME;
					}
					setSaddled(true);
					return ActionResult.SUCCESS;
				}
			}
			if (isSaddled() && !hasPassengers()) {
				if (world.isClient()) {
					return ActionResult.CONSUME;
				}
				player.setYaw(getYaw());
				player.setPitch(getPitch());
				player.startRiding(this);
				return ActionResult.SUCCESS;
			}
		}
		return super.interactMob(player, hand);
	}

	protected SoundEvent getHurtSound(DamageSource source) {
		return this.isSmall() ? SoundEvents.ENTITY_SLIME_HURT_SMALL : SoundEvents.ENTITY_SLIME_HURT;
	}

	protected SoundEvent getDeathSound() {
		return this.isSmall() ? SoundEvents.ENTITY_SLIME_DEATH_SMALL : SoundEvents.ENTITY_SLIME_DEATH;
	}

	protected SoundEvent getSquishSound() {
		return this.isSmall() ? SoundEvents.ENTITY_SLIME_SQUISH_SMALL : SoundEvents.ENTITY_SLIME_SQUISH;
	}

	protected Identifier getLootTableId() {
		if (isSmall())
			return LootTables.EMPTY;
		DyeColor color = getColor();
		if (color == null) {
			return PLAIN_LOOT_TABLE;
		}
		return new Identifier(BouncyLife.MOD_ID, "entities/pet_slime/" + color.getName());
	}

	protected float getSoundVolume() {
		return 0.4F * (float) this.getSize();
	}

	public int getLookPitchSpeed() {
		return 0;
	}

	protected boolean makesJumpSound() {
		return this.getSize() > 0;
	}

	protected void jump() {
		Vec3d vec3d = this.getVelocity();
		this.setVelocity(vec3d.x, this.getJumpVelocity(), vec3d.z);
		this.velocityDirty = true;
	}

	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityTag) {
		int i = this.random.nextInt(3);
		if (i < 2 && this.random.nextFloat() < 0.5F * difficulty.getClampedLocalDifficulty()) {
			++i;
		}
		int j = 1 << i;
		this.setSize(j, true);
		return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
	}

	@Nullable
	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
		return null;
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return BLConfig.pets.enableBreeding && stack.getItem() == Items.HONEY_BOTTLE;
	}

	@Override
	protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
		boolean honeyBottle = stack.getItem() == Items.HONEY_BOTTLE;
		super.eat(player, hand, stack);
		if (honeyBottle && !player.isCreative()) {
			player.giveItemStack(new ItemStack(Items.GLASS_BOTTLE));
		}
	}

	@Override
	public boolean canBreedWith(AnimalEntity other) {
		if (super.canBreedWith(other)) {
			int size = getSize();
			if (size < BLConfig.pets.breedSizeLimit) {
				return size == ((PetSlimeEntity) other).getSize();
			}
		}
		return false;
	}

	@Override
	public boolean isBaby() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handleStatus(byte status) {
		super.handleStatus(status);
		if (status == 8) {
			showParticles(ParticleTypes.HAPPY_VILLAGER);
		} else if (status == 9) {
			showParticles(getParticles());
			playSound(SoundEvents.ENTITY_SLIME_JUMP, 1.0F, 1.0F);
		}
	}

	public void showParticles(ParticleEffect particle) {
		for (int i = 0; i < 7; ++i) {
			double d = this.random.nextGaussian() * 0.02D;
			double e = this.random.nextGaussian() * 0.02D;
			double f = this.random.nextGaussian() * 0.02D;
			this.world.addParticle(particle, this.getParticleX(1.0D), this.getRandomBodyY() + 0.5D, this.getParticleZ(1.0D), d, e, f);
		}
	}

	private float method_24353() {
		float f = this.isSmall() ? 1.4F : 0.8F;
		return ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * f;
	}

	protected SoundEvent getJumpSound() {
		return this.isSmall() ? SoundEvents.ENTITY_SLIME_JUMP_SMALL : SoundEvents.ENTITY_SLIME_JUMP;
	}

	public EntityDimensions getDimensions(EntityPose pose) {
		return super.getDimensions(pose).scaled(0.255F * (float) this.getSize());
	}

	protected void performAreaAttack() {
		if (isSmall()) {
			return;
		}

		Box box = getBoundingBox().expand(1.3);
		//noinspection unchecked
		List<LivingEntity> targets = (List<LivingEntity>)(List<?>) world.getTargets(
				HostileEntity.class,
				TargetPredicate.createAttackable(),
				this,
				box
		);
		{
			LivingEntity target = getTarget();
			if (target != null && box.contains(target.getPos())) {
				targets.add(target);
			}
		}

		float dmg = (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
		float knockback = (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);

		boolean success = false;
		for (LivingEntity target : targets) {
			if (target.damage(DamageSource.mob(this), dmg)) {
				if (knockback > 0) {
					target.takeKnockback(knockback, getX() - target.getX(), getZ() - target.getZ());
				}
				applyDamageEffects(this, target);
				onAttacking(target);

				success = true;
			}
		}

		if (success) {
			world.playSoundFromEntity(null, this, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.0F);
		}
	}

	@Override
	public boolean cannotDespawn() {
		return true;
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		fallDistance = Math.max(0, fallDistance - getSize());
		return super.handleFallDamage(fallDistance, damageMultiplier * 0.2F, damageSource);
	}

	@Override
	protected float getJumpVelocity() {
		float result = (float) (super.getJumpVelocity() * Math.sqrt(getSize() * 0.5) + playerJumpBarMultiplier * 0.25);
		playerJumpBarMultiplier = 0F;
		return result;
	}

	@Override
	public boolean canJump() {
		return isSaddled();
	}

	@Override
	public void startJumping(int height) {
		Entity rider = getPrimaryPassenger();
		if (rider instanceof LivingEntity) {
			playerJumpBarMultiplier = MathHelper.clamp(height, 0, 90) / 90F;
			setMovementSpeed((float) getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getValue());
			((SlimeMoveControl) moveControl).ticksUntilJump = 0;
			jumping = true;
			playerJump = true;
		}
	}

	@Override
	public void stopJumping() {

	}

	@Override
	@Environment(EnvType.CLIENT)
	public void setJumpStrength(int strength) {

	}

	static class AttackGoal extends MeleeAttackGoal {
		private boolean onGround;

		public AttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
			super(mob, speed, pauseWhenMobIdle);
			onGround = true;
		}

		@Override
		public boolean canStart() {
			if (mob.hasPassengers()) {
				return false;
			}
			if (((PetSlimeEntity) mob).isSmall()) {
				return false;
			}
			return super.canStart();
		}

		@Override
		public void start() {
			super.start();
			((SlimeMoveControl) mob.getMoveControl()).setJumpOften(true);
		}

		@Override
		public void tick() {
			if (mob.isOnGround()) {
				if (!onGround) {
					((PetSlimeEntity) mob).performAreaAttack();
					onGround = true;
				}
				super.tick();
			} else {
				onGround = false;
			}
		}

		@Override
		protected void attack(LivingEntity target, double squaredDistance) {
			double d = this.getSquaredMaxAttackDistance(target);
			if (squaredDistance <= d && isCooledDown()) {
				mob.getJumpControl().setActive();
			}
		}

		@Override
		protected double getSquaredMaxAttackDistance(LivingEntity entity) {
			return mob.getWidth() * mob.getWidth() * 0.3 + entity.getWidth();
		}

		@Override
		public void stop() {
			((SlimeMoveControl) mob.getMoveControl()).setJumpOften(false);
			super.stop();
		}
	}

	static class SlimeMoveControl extends MoveControl {
		private int ticksUntilJump;
		private final PetSlimeEntity slime;
		private boolean jumpOften;

		public SlimeMoveControl(PetSlimeEntity slime) {
			super(slime);
			this.slime = slime;
		}

		@Override
		public void moveTo(double x, double y, double z, double speed) {
			super.moveTo(x, y, z, speed);
			this.state = State.MOVE_TO;
		}

		public void setJumpOften(boolean jumpOften) {
			this.jumpOften = jumpOften;
		}

		public void tick() {
			if (!entity.isOnGround()) {
				return;
			}

			boolean shallMove = false;
			this.entity.headYaw = this.entity.getYaw();
			this.entity.bodyYaw = this.entity.getYaw();
			float speed = 1F;

			Entity rider = slime.getPrimaryPassenger();
			if (rider instanceof LivingEntity) {
				shallMove = ((LivingEntity) rider).forwardSpeed > 0.001 || slime.jumping;
				jumpOften = rider.isSprinting();
				speed = 1.2F;
			} else if (this.state == State.MOVE_TO) {
				shallMove = true;

				double d = this.targetX - this.entity.getX();
				double e = this.targetZ - this.entity.getZ();
				double o = this.targetY - this.entity.getY();
				double p = d * d + o * o + e * e;
				if (p * 4 < 2.5E-7) {
					this.entity.setMovementSpeed(0.0f);
					return;
				}
				float n = (float)(MathHelper.atan2(e, d) * 57.2957763671875) - 90.0f;
				this.entity.setYaw(this.wrapDegrees(this.entity.getYaw(), n, 90.0f));
			}

			if (this.ticksUntilJump-- <= 0 && shallMove) {
				this.ticksUntilJump = this.slime.getTicksUntilNextJump();
				if (this.jumpOften) {
					this.ticksUntilJump /= 3;
				}

				this.entity.setMovementSpeed(speed * (float) this.entity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getValue());
				this.slime.getJumpControl().setActive();
				if (this.slime.makesJumpSound()) {
					this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), this.slime.method_24353());
				}
				slime.jumping = false;
			} else {
				this.slime.sidewaysSpeed = 0.0F;
				this.entity.setMovementSpeed(0.0F);
				// TODO: LookControl?
				//this.entity.yaw = this.changeAngle(this.entity.yaw, slime.getLookControl()..., 30.0F);
			}
		}
	}

	public static class CombineWithMateGoal extends Goal {
		protected final PetSlimeEntity self;
		protected PetSlimeEntity mate;
		protected final TargetPredicate targetPredicate = TargetPredicate.createNonAttackable()
				.ignoreVisibility().setBaseMaxDistance(10D);

		private int timer;

		public CombineWithMateGoal(PetSlimeEntity self) {
			this.self = self;
			this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
		}

		@Override
		public boolean canStart() {
			if (!self.isInLove() || self.hasPassengers()) {
				return false;
			}

			Box boundingBox = self.getBoundingBox().expand(Math.max(self.getWidth() * 2, 10D));
			targetPredicate.setBaseMaxDistance(boundingBox.getXLength());
			List<PetSlimeEntity> targets = self.world.getTargets(PetSlimeEntity.class, targetPredicate, self, boundingBox);
			double shortestDistance = Double.MAX_VALUE;
			mate = null;
			for (PetSlimeEntity target : targets) {
				if (!self.canBreedWith(target)) {
					continue;
				}

				double distance = self.squaredDistanceTo(target);
				if (distance >= shortestDistance) {
					continue;
				}
				mate = target;
			}
			return mate != null;
		}

		@Override
		public boolean shouldContinue() {
			return mate.isAlive() && mate.isInLove() && timer < 60;
		}

		@Override
		public void stop() {
			mate = null;
			timer = 0;
		}

		@Override
		public void tick() {
			self.getLookControl().lookAt(mate, 10.0F, (float) self.getLookPitchSpeed());
			self.getNavigation().startMovingTo(mate, 1.0D);
			timer++;
			if (timer >= 60 && self.squaredDistanceTo(mate) < self.getWidth() * self.getWidth() * 4) {
				breed();
			}
		}

		protected void breed() {
			self.setPos((self.getX() + mate.getX()) * 0.5, (self.getY() + mate.getY()) * 0.5, (self.getZ() + mate.getZ()) * 0.5);
			self.setSize(self.getSize() + 1, false);
			self.setHealth((self.getHealth() + mate.getHealth()) * 1.5F);
			self.setColor(BLUtil.mixColors(self.getColor(), mate.getColor(), self.world));
			// saddle madness
			if (self.isSaddled()) {
				if (mate.isSaddled()) {
					mate.world.spawnEntity(new ItemEntity(mate.world, mate.getX(), mate.getY(), mate.getZ(), new ItemStack(Items.SADDLE)));
				}
			} else {
				if (mate.isSaddled()) {
					self.setSaddled(true);
				}
			}
			// name madness
			{
				Text selfName = self.getCustomName();
				Text mateName = mate.getCustomName();
				if (selfName == null) {
					if (mateName != null) {
						self.setCustomName(mateName);
					}
				} else {
					if (mateName != null) {
						if (!selfName.asString().equals(mateName.asString())) {
							self.setCustomName(new LiteralText(selfName.asString() + "-" + mateName.asString()));
						}
					}
				}
			}
			self.setPersistent();
			mate.setRemoved(RemovalReason.DISCARDED);
			self.world.sendEntityStatus(self, (byte) 9);
		}
	}
}
