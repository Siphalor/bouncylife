package de.siphalor.bouncylife.entity;

import de.siphalor.bouncylife.BouncyLife;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.OptionalInt;

public class PetSlimeEntity extends TameableEntity {
	private static final TrackedData<Integer> SLIME_SIZE;
	private static final TrackedData<OptionalInt> COLOR;
	private static final TrackedData<Boolean> SADDLED;
	private static final Ingredient TEMPT_INGREDIENT = Ingredient.ofItems(Items.HONEY_BOTTLE, Items.ROTTEN_FLESH);

	private static final Identifier PLAIN_LOOT_TABLE = new Identifier(BouncyLife.MOD_ID, "entities/pet_slime/plain");

	public float targetStretch;
	public float stretch;
	public float lastStretch;
	protected int growthRandBound;
	private boolean onGroundLastTick;

	public PetSlimeEntity(EntityType<? extends PetSlimeEntity> entityType, World world) {
		super(entityType, world);
		this.moveControl = new PetSlimeEntity.SlimeMoveControl(this);
	}

	public static PetSlimeEntity of(SlimeEntity slimeEntity) {
		PetSlimeEntity petSlimeEntity = BouncyLife.petSlimeEntityType.create(slimeEntity.world);
		petSlimeEntity.copyPositionAndRotation(slimeEntity);
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
		this.goalSelector.add(1, new SwimmingGoal(this));
		this.goalSelector.add(2, new FaceTowardTargetGoal(this));
		this.goalSelector.add(3, new FollowOwnerGoal(this, 1D, 10F, 2F, false));
		this.goalSelector.add(4, new TemptGoal(this, 1D, TEMPT_INGREDIENT, false));
		this.goalSelector.add(5, new MoveGoal(this));
		this.goalSelector.add(5, new RandomLookGoal(this));
		this.goalSelector.add(10, new CombineWithMateGoal(this, 1F));
		this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
		this.targetSelector.add(2, new AttackWithOwnerGoal(this));
		this.targetSelector.add(3, (new RevengeGoal(this)).setGroupRevenge());
		this.targetSelector.add(4, new FollowTargetGoal<>(this, ZombieEntity.class, false));
	}

	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(SLIME_SIZE, 1);
		this.dataTracker.startTracking(COLOR, OptionalInt.empty());
		this.dataTracker.startTracking(SADDLED, false);
	}

	protected void initAttributes() {
		super.initAttributes();
		this.getAttributes().register(EntityAttributes.ATTACK_DAMAGE);
	}

	public void setSize(int size, boolean heal) {
		this.dataTracker.set(SLIME_SIZE, size);
		this.refreshPosition();
		this.calculateDimensions();
		this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(size * size);
		this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.2F + 0.1F * (float)size);
		this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(size);
		if (heal) {
			this.setHealth(this.getMaximumHealth());
		}

		this.growthRandBound = size * size + 2;

		this.experiencePoints = size;
	}

	public int getSize() {
		return this.dataTracker.get(SLIME_SIZE);
	}

	public void setColor(DyeColor dyeColor) {
		if (dyeColor == null) {
			dataTracker.set(COLOR, OptionalInt.empty());
		} else {
			dataTracker.set(COLOR, OptionalInt.of(dyeColor.getId()));
		}
	}

	public DyeColor getColor() {
		OptionalInt colorId = dataTracker.get(COLOR);
		if (colorId.isPresent()) {
			return DyeColor.byId(colorId.getAsInt());
		}
		return null;
	}

	public boolean isSaddled() {
		return dataTracker.get(SADDLED);
	}

	public void writeCustomDataToTag(CompoundTag tag) {
		super.writeCustomDataToTag(tag);
		tag.putInt("Size", this.getSize() - 1);
		if (getColor() != null)
			tag.putString("Color", getColor().getName());
		tag.putBoolean("Saddled", isSaddled());
		tag.putBoolean("wasOnGround", this.onGroundLastTick);
	}

	public void readCustomDataFromTag(CompoundTag tag) {
		int i = tag.getInt("Size");
		if (i < 0) {
			i = 0;
		}

		setColor(DyeColor.byName(tag.getString("Color"), null));
		dataTracker.set(SADDLED, tag.getBoolean("Saddled"));
		this.setSize(i + 1, false);
		super.readCustomDataFromTag(tag);
		this.onGroundLastTick = tag.getBoolean("wasOnGround");
	}

	public boolean isSmall() {
		return this.getSize() <= 1;
	}

	protected ParticleEffect getParticles() {
		return ParticleTypes.ITEM_SLIME;
	}

	public void tick() {
		this.stretch += (this.targetStretch - this.stretch) * 0.5F;
		this.lastStretch = this.stretch;
		super.tick();
		if (this.onGround && !this.onGroundLastTick) {
			int i = this.getSize();

			for(int j = 0; j < i * 8; ++j) {
				float f = this.random.nextFloat() * 6.2831855F;
				float g = this.random.nextFloat() * 0.5F + 0.5F;
				float h = MathHelper.sin(f) * (float)i * 0.5F * g;
				float k = MathHelper.cos(f) * (float)i * 0.5F * g;
				this.world.addParticle(this.getParticles(), this.getX() + (double)h, this.getY(), this.getZ() + (double)k, 0.0D, 0.0D, 0.0D);
			}

			this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) / 0.8F);
			this.targetStretch = -0.5F;
		} else if (!this.onGround && this.onGroundLastTick) {
			this.targetStretch = 1.0F;
		}

		this.onGroundLastTick = this.onGround;
		this.updateStretch();
	}

	@Override
	public void travel(Vec3d movementInput) {
		if (hasPassengers()) {
			((SlimeMoveControl) moveControl).look(getPassengerList().get(0).yaw, true);
			((SlimeMoveControl) moveControl).move(1D);
			super.travel(movementInput);
		}
	}

	protected void updateStretch() {
		this.targetStretch *= 0.6F;
	}

	protected int getTicksUntilNextJump() {
		return this.random.nextInt(20) + 10;
	}

	public void calculateDimensions() {
		double d = this.getX();
		double e = this.getY();
		double f = this.getZ();
		super.calculateDimensions();
		this.updatePosition(d, e, f);
	}

	public void onTrackedDataSet(TrackedData<?> data) {
		if (SLIME_SIZE.equals(data)) {
			this.calculateDimensions();
			this.yaw = this.headYaw;
			this.bodyYaw = this.headYaw;
			if (this.isTouchingWater() && this.random.nextInt(20) == 0) {
				this.onSwimmingStart();
			}
		}

		super.onTrackedDataSet(data);
	}

	public EntityType<?> getType() {
		return super.getType();
	}

	public void remove() {
		int size = this.getSize();
		if (!this.world.isClient && size > 1 && this.getHealth() <= 0.0F) {
			if (isSaddled()) {
				ItemEntity itemEntity = new ItemEntity(world, getX(), getY(), getZ(), new ItemStack(Items.SADDLE));
				world.spawnEntity(itemEntity);
			}

			Text text = this.getCustomName();
			boolean bl = this.isAiDisabled();
			float f = (float)size / 4.0F;
			int j = size / 2;
			int k = 2 + this.random.nextInt(3);

			for(int l = 0; l < k; ++l) {
				float g = ((float)(l % 2) - 0.5F) * f;
				float h = ((float)(l / 2) - 0.5F) * f;
				PetSlimeEntity slimeEntity = (PetSlimeEntity)this.getType().create(this.world);
				if (this.isPersistent()) {
					slimeEntity.setPersistent();
				}

				slimeEntity.setOwnerUuid(getOwnerUuid());
				slimeEntity.setCustomName(text);
				slimeEntity.setAiDisabled(bl);
				slimeEntity.setInvulnerable(this.isInvulnerable());
				slimeEntity.setSize(j, true);
				slimeEntity.setColor(getColor());
				slimeEntity.refreshPositionAndAngles(this.getX() + (double)g, this.getY() + 0.5D, this.getZ() + (double)h, this.random.nextFloat() * 360.0F, 0.0F);
				this.world.spawnEntity(slimeEntity);
			}
		}

		super.remove();
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
			if (this.squaredDistanceTo(target) < 0.6D * (double)i * 0.6D * (double)i && this.canSee(target) && target.damage(DamageSource.mob(this), this.getDamageAmount())) {
				this.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
				this.dealDamage(this, target);
			}
		}

	}

	protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
		return 0.625F * dimensions.height;
	}

	protected boolean canAttack() {
		return !this.isSmall() && this.canMoveVoluntarily();
	}

	protected float getDamageAmount() {
		return (float) this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).getValue();
	}

	@Override
	public boolean interactMob(PlayerEntity player, Hand hand) {
		if (isOwner(player)) {
			ItemStack stack = player.getStackInHand(hand);
			if (TEMPT_INGREDIENT.test(stack) && isOwner(player)) {
				if (!world.isClient()) {
					eat(player, stack);

					growthRandBound = Math.max(0, growthRandBound);

					if (getHealth() < getMaximumHealth()) {
						setHealth(Math.min(getMaximumHealth(), getHealth() + 1F));
						world.sendEntityStatus(this, (byte) 7);
					} else if (random.nextInt(growthRandBound) == 0) {
						setSize(getSize() + 1, true);
						world.sendEntityStatus(this, (byte) 8);
					} else {
						world.sendEntityStatus(this, (byte) 8);
					}
				}
				return true;
			} else if (stack.getItem() instanceof DyeItem) {
				DyeColor nextColor = ((DyeItem) stack.getItem()).getColor();
				if (nextColor != getColor()) {
					if (!world.isClient()) {
						setColor(((DyeItem) stack.getItem()).getColor());
					}
					if (!player.isCreative())
						stack.decrement(1);
					return true;
				}
			} else if (stack.getItem() == Items.SADDLE) {
				if (!isSaddled() && getSize() >= 2) {
					if (!world.isClient()) {
						dataTracker.set(SADDLED, true);
					}
					world.playSound(player, getX(), getY(), getZ(), SoundEvents.ENTITY_PIG_SADDLE, SoundCategory.NEUTRAL, 0.5F, 1F);
					if (!player.isCreative())
						stack.decrement(1);
					return true;
				}
			}
			if (isSaddled() && !hasPassengers() && isOwner(player)) {
				if (!world.isClient()) {
					player.yaw = yaw;
					player.pitch = pitch;
					player.startRiding(this);
				}
				return true;
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
		if (getSize() != 1)
			return LootTables.EMPTY;
		DyeColor color = getColor();
		if (color == null) {
			return PLAIN_LOOT_TABLE;
		}
		return new Identifier(BouncyLife.MOD_ID, "entities/pet_slime/" + color.getName());
	}

	protected float getSoundVolume() {
		return 0.4F * (float)this.getSize();
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
	public net.minecraft.entity.EntityData initialize(IWorld world, LocalDifficulty difficulty, SpawnType spawnType, net.minecraft.entity.EntityData entityData, CompoundTag entityTag) {
		int i = this.random.nextInt(3);
		if (i < 2 && this.random.nextFloat() < 0.5F * difficulty.getClampedLocalDifficulty()) {
			++i;
		}
		int j = 1 << i;
		this.setSize(j, true);
		return super.initialize(world, difficulty, spawnType, entityData, entityTag);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return stack.getItem() == Items.HONEY_BOTTLE;
	}

	@Override
	protected void eat(PlayerEntity player, ItemStack stack) {
		boolean honeyBottle = stack.getItem() == Items.HONEY_BOTTLE;
		super.eat(player, stack);
		if (honeyBottle) {
			player.giveItemStack(new ItemStack(Items.GLASS_BOTTLE));
		}
	}

	@Override
	public boolean canBreedWith(AnimalEntity other) {
		if (super.canBreedWith(other)) {
			return getSize() == ((PetSlimeEntity) other).getSize();
		}
		return false;
	}

	@Override
	public boolean isBaby() {
		return false;
	}

	@Override
	public PassiveEntity createChild(PassiveEntity mate) {
		return null;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handleStatus(byte status) {
		super.handleStatus(status);
		if (status == 8) {
			showGrowthParticles();
		}
	}

	public void showGrowthParticles() {
		for(int i = 0; i < 7; ++i) {
			double d = this.random.nextGaussian() * 0.02D;
			double e = this.random.nextGaussian() * 0.02D;
			double f = this.random.nextGaussian() * 0.02D;
			this.world.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getParticleX(1.0D), this.getRandomBodyY() + 0.5D, this.getParticleZ(1.0D), d, e, f);
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
		return super.getDimensions(pose).scaled(0.255F * (float)this.getSize());
	}

	static {
		SLIME_SIZE = DataTracker.registerData(PetSlimeEntity.class, TrackedDataHandlerRegistry.INTEGER);
		COLOR = DataTracker.registerData(PetSlimeEntity.class, TrackedDataHandlerRegistry.field_17910);
		SADDLED = DataTracker.registerData(PetSlimeEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	}

	static class MoveGoal extends Goal {
		private final PetSlimeEntity slime;

		public MoveGoal(PetSlimeEntity slime) {
			this.slime = slime;
			this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
		}

		public boolean canStart() {
			return !this.slime.hasVehicle();
		}

		public void tick() {
			((PetSlimeEntity.SlimeMoveControl)this.slime.getMoveControl()).move(1.0D);
		}
	}

	static class SwimmingGoal extends Goal {
		private final PetSlimeEntity slime;

		public SwimmingGoal(PetSlimeEntity slime) {
			this.slime = slime;
			this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
			slime.getNavigation().setCanSwim(true);
		}

		public boolean canStart() {
			return (this.slime.isTouchingWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof PetSlimeEntity.SlimeMoveControl;
		}

		public void tick() {
			if (this.slime.getRandom().nextFloat() < 0.8F) {
				this.slime.getJumpControl().setActive();
			}

			((PetSlimeEntity.SlimeMoveControl)this.slime.getMoveControl()).move(1.2D);
		}
	}

	static class RandomLookGoal extends Goal {
		private final PetSlimeEntity slime;
		private float targetYaw;
		private int timer;

		public RandomLookGoal(PetSlimeEntity slime) {
			this.slime = slime;
			this.setControls(EnumSet.of(Control.LOOK));
		}

		public boolean canStart() {
			return this.slime.getTarget() == null && (this.slime.onGround || this.slime.isTouchingWater() || this.slime.isInLava() || this.slime.hasStatusEffect(StatusEffects.LEVITATION)) && this.slime.getMoveControl() instanceof PetSlimeEntity.SlimeMoveControl;
		}

		public void tick() {
			if (--this.timer <= 0) {
				this.timer = 40 + this.slime.getRandom().nextInt(60);
				this.targetYaw = (float)this.slime.getRandom().nextInt(360);
			}

			((PetSlimeEntity.SlimeMoveControl)this.slime.getMoveControl()).look(this.targetYaw, false);
		}
	}

	static class FaceTowardTargetGoal extends Goal {
		private final PetSlimeEntity slime;
		private int ticksLeft;

		public FaceTowardTargetGoal(PetSlimeEntity slime) {
			this.slime = slime;
			this.setControls(EnumSet.of(Control.LOOK));
		}

		public boolean canStart() {
			LivingEntity livingEntity = this.slime.getTarget();
			if (livingEntity == null) {
				return false;
			} else if (!livingEntity.isAlive()) {
				return false;
			} else {
				return (!(livingEntity instanceof PlayerEntity) || !((PlayerEntity) livingEntity).abilities.invulnerable) && this.slime.getMoveControl() instanceof SlimeMoveControl;
			}
		}

		public void start() {
			this.ticksLeft = 300;
			super.start();
		}

		public boolean shouldContinue() {
			LivingEntity livingEntity = this.slime.getTarget();
			if (livingEntity == null) {
				return false;
			} else if (!livingEntity.isAlive()) {
				return false;
			} else if (livingEntity instanceof PlayerEntity && ((PlayerEntity)livingEntity).abilities.invulnerable) {
				return false;
			} else {
				return --this.ticksLeft > 0;
			}
		}

		public void tick() {
			this.slime.lookAtEntity(this.slime.getTarget(), 10.0F, 10.0F);
			((PetSlimeEntity.SlimeMoveControl)this.slime.getMoveControl()).look(this.slime.yaw, this.slime.canAttack());
		}
	}

	static class SlimeMoveControl extends MoveControl {
		private float targetYaw;
		private int ticksUntilJump;
		private final PetSlimeEntity slime;
		private boolean jumpOften;

		public SlimeMoveControl(PetSlimeEntity slime) {
			super(slime);
			this.slime = slime;
			this.targetYaw = 180.0F * slime.yaw / 3.1415927F;
		}

		public void look(float targetYaw, boolean jumpOften) {
			this.targetYaw = targetYaw;
			this.jumpOften = jumpOften;
		}

		public void move(double speed) {
			this.speed = speed;
			this.state = State.MOVE_TO;
		}

		public void tick() {
			this.entity.yaw = this.changeAngle(this.entity.yaw, this.targetYaw, 90.0F);
			this.entity.headYaw = this.entity.yaw;
			this.entity.bodyYaw = this.entity.yaw;
			if (this.state != State.MOVE_TO) {
				this.entity.setForwardSpeed(0.0F);
			} else {
				this.state = State.WAIT;
				if (this.entity.onGround) {
					this.entity.setMovementSpeed((float)(this.speed * this.entity.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).getValue()));
					if (this.ticksUntilJump-- <= 0) {
						this.ticksUntilJump = this.slime.getTicksUntilNextJump();
						if (this.jumpOften) {
							this.ticksUntilJump /= 3;
						}

						this.slime.getJumpControl().setActive();
						if (this.slime.makesJumpSound()) {
							this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), this.slime.method_24353());
						}
					} else {
						this.slime.sidewaysSpeed = 0.0F;
						this.slime.forwardSpeed = 0.0F;
						this.entity.setMovementSpeed(0.0F);
					}
				} else {
					this.entity.setMovementSpeed((float)(this.speed * this.entity.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).getValue()));
				}

			}
		}
	}

	public static class CombineWithMateGoal extends AnimalMateGoal {
		public CombineWithMateGoal(AnimalEntity animal, double chance) {
			super(animal, chance);
		}

		@Override
		protected void breed() {
			if (animal instanceof PetSlimeEntity) {
				((PetSlimeEntity) animal).setSize(((PetSlimeEntity) animal).getSize(), false);
				mate.removed = true;
			}
		}
	}
}
