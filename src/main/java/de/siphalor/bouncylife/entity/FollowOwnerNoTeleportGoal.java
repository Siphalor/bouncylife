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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.passive.TameableEntity;

import java.util.EnumSet;

public class FollowOwnerNoTeleportGoal extends Goal {
	private final TameableEntity tameable;
	private LivingEntity owner;
	private final double speed;
	private final EntityNavigation navigation;
	private int updateCountdownTicks;
	private final float maxDistance;
	private final float minDistance;
	private float oldWaterPathfindingPenalty;

	public FollowOwnerNoTeleportGoal(TameableEntity tameable, double speed, float minDistance, float maxDistance) {
		this.tameable = tameable;
		this.speed = speed;
		this.navigation = tameable.getNavigation();
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
		this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
		if (!(tameable.getNavigation() instanceof MobNavigation) && !(tameable.getNavigation() instanceof BirdNavigation)) {
			throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
		}
	}

	public boolean canStart() {
		LivingEntity livingEntity = this.tameable.getOwner();
		if (livingEntity == null) {
			return false;
		} else if (livingEntity.isSpectator()) {
			return false;
		} else if (this.tameable.isSitting()) {
			return false;
		} else if (this.tameable.squaredDistanceTo(livingEntity) < (double)(this.minDistance * this.minDistance)) {
			return false;
		} else {
			this.owner = livingEntity;
			return true;
		}
	}

	public boolean shouldContinue() {
		if (this.navigation.isIdle()) {
			return false;
		} else if (this.tameable.isSitting()) {
			return false;
		} else {
			return this.tameable.squaredDistanceTo(this.owner) > (double)(this.maxDistance * this.maxDistance);
		}
	}

	public void start() {
		this.updateCountdownTicks = 0;
		this.oldWaterPathfindingPenalty = this.tameable.getPathfindingPenalty(PathNodeType.WATER);
		this.tameable.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
	}

	public void stop() {
		this.owner = null;
		this.navigation.stop();
		this.tameable.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathfindingPenalty);
	}

	public void tick() {
		this.tameable.getLookControl().lookAt(this.owner, 10.0F, (float)this.tameable.getMaxLookPitchChange());
		if (--this.updateCountdownTicks <= 0) {
			this.updateCountdownTicks = 10;
			if (!this.tameable.isLeashed() && !this.tameable.hasVehicle()) {
				this.navigation.startMovingTo(this.owner, this.speed);
			}
		}
	}
}
