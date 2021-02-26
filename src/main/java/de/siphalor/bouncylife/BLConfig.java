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

import com.google.common.base.CaseFormat;
import com.mojang.datafixers.util.Pair;
import de.siphalor.tweed.config.ConfigEnvironment;
import de.siphalor.tweed.config.ConfigScope;
import de.siphalor.tweed.config.annotated.AConfigConstraint;
import de.siphalor.tweed.config.annotated.AConfigEntry;
import de.siphalor.tweed.config.annotated.AConfigFixer;
import de.siphalor.tweed.config.annotated.ATweedConfig;
import de.siphalor.tweed.config.constraints.RangeConstraint;
import de.siphalor.tweed.data.DataObject;
import de.siphalor.tweed.data.DataValue;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
@ATweedConfig(file = BouncyLife.MOD_ID, casing = CaseFormat.LOWER_HYPHEN, tailors = "tweed:cloth", environment = ConfigEnvironment.UNIVERSAL, scope = ConfigScope.SMALLEST)
public class BLConfig {
	@AConfigEntry(comment = "Configs related to the slime fork or the slime armor")
	public static Bounce bounce;

	public static class Bounce {
		@AConfigEntry(comment = "Sets the factor used when determining the velocity after the fork has been used", constraints = {
				@AConfigConstraint(value = RangeConstraint.class, param = "0..100")
		})
		public float selfShootPower = 1.8F;

		@AConfigEntry(comment = "Sets the factor determining the speed of shot entities", constraints = {
				@AConfigConstraint(value = RangeConstraint.class, param = "0..100")
		})
		public float otherShootPower = 2.7F;

		@AConfigEntry(comment = "The percentage the velocity is additionally changed to when bouncing off the ground", constraints = {
				@AConfigConstraint(value = RangeConstraint.class, param = "0..1")
		})
		public float velocityDampener = 1F;

		@AConfigEntry(comment = "The percentage the velocity is additionally changed to when bouncing of the ground while sneaking", constraints = {
				@AConfigConstraint(value = RangeConstraint.class, param = "0..1")
		})
		public float sneakVelocityDampener = 0.5F;

		@AConfigEntry(comment = "Sets minimum velocity in y direction before stopping the player", constraints = {
				@AConfigConstraint(value = RangeConstraint.class, param = "0..")
		})
		public float yBounceTolerance = 0.7F;

		@AConfigEntry(comment = "Wearing slime armor implicitly works like the Thorns enchantment")
		public boolean slimeArmorThorns = true;
	}

	@AConfigEntry(comment = "Configs related to petting slimes",
			environment = ConfigEnvironment.SYNCED
	)
	public static Pet pet;

	public static class Pet {
		@AConfigEntry(comment = "Enable amassing slimes by feeding them honey.")
		public boolean enableHoneyAmassing = true;

		@AConfigEntry(comment = "Enable amassing slimes by feeding them rotten flesh.")
		public boolean enableRottenFleshAmassing = true;

		@AConfigEntry(comment = "The maximum size of a pet slime that can be reached with the amassing mechanic.",
				constraints = @AConfigConstraint(value = RangeConstraint.class, param = "0..")
		)
		public int amassSizeLimit = 5;

		@AConfigEntry(comment = "Enable breeding of pet slimes.\nPet slimes will not normally breed but combine each other to a bigger pet slime,\nif they have the same size.")
		public boolean enableBreeding = true;

		@AConfigEntry(comment = "The maximum size of a pet slime that can be reached through the breeding mechanic.",
				constraints = @AConfigConstraint(value = RangeConstraint.class, param = "0..")
		)
		public int breedSizeLimit = 10;

		@AConfigEntry(comment = "Limits the maximum health of pet slimes.\nSince the maximum health is calculate as the square of the size,\n" +
				"this could grow infinitely.\nVanilla can only display 30 hearts of a pet so this should usually be limited to 60.",
				constraints = @AConfigConstraint(value = RangeConstraint.class, param = "1..")
		)
		public int maxHealthLimit = 60;
	}

	@AConfigFixer
	public static <T> void fixMainConfig(DataObject<T> main, DataObject<T> main_) {
		// Detect the old config where there were no categories
		if (!main.has("bounce")) {
			DataObject<T> bounceObj = main.addObject("bounce");
			ArrayList<String> oldEntries = new ArrayList<>(main.size());
			for (Pair<String, DataValue<T>> pair : main) {
				switch (pair.getFirst()) {
					case "bounce":
						continue;
					case "fork-power-factor":
						bounceObj.set("self-shoot-power", pair.getSecond());
						break;
					case "fork-entity-power-factor":
						bounceObj.set("other-shoot-power", pair.getSecond());
						break;
					default:
						bounceObj.set(pair.getFirst(), pair.getSecond());
						break;
				}
				oldEntries.add(pair.getFirst());
			}
			for (String oldEntry : oldEntries) {
				main.remove(oldEntry);
			}
		}
	}
}
