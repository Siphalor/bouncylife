package de.siphalor.bouncylife;

import com.google.common.base.CaseFormat;
import de.siphalor.tweed.config.ConfigEnvironment;
import de.siphalor.tweed.config.ConfigScope;
import de.siphalor.tweed.config.annotated.AConfigConstraint;
import de.siphalor.tweed.config.annotated.AConfigEntry;
import de.siphalor.tweed.config.annotated.ATweedConfig;
import de.siphalor.tweed.config.constraints.RangeConstraint;

@SuppressWarnings("WeakerAccess")
@ATweedConfig(file = BouncyLife.MOD_ID, casing = CaseFormat.LOWER_HYPHEN, tailors = "tweed:cloth", environment = ConfigEnvironment.UNIVERSAL, scope = ConfigScope.SMALLEST)
public class BLConfig {

	@AConfigEntry(name = "fork-power-factor", comment = "Sets the factor used when determining the velocity after the fork has been used")
	public static float forkFactor = 1.8F;

	@AConfigEntry(name = "fork-entity-power-factor", comment = "Sets the factor determining the speed of shot entities")
	public static float forkEntityFactor = 2.7F;

	@AConfigEntry(comment = "The percentage the velocity is additionally changed to when bouncing off the ground", constraints = {
			@AConfigConstraint(value = RangeConstraint.class, param = "0..")
	})
	public static float velocityDampener = 1F;

	@AConfigEntry(comment = "The percentage the velocity is additionally changed to when bouncing of the ground while sneaking", constraints = {
			@AConfigConstraint(value = RangeConstraint.class, param = "0..")
	})
	public static float sneakVelocityDampener = 0.5F;

	@AConfigEntry(comment = "Sets minimum velocity in y direction before stopping the player", constraints = {
			@AConfigConstraint(value = RangeConstraint.class, param = "0..")
	})
	public static float yBounceTolerance = 0.7F;
}
