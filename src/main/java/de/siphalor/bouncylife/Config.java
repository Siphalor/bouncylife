package de.siphalor.bouncylife;

import de.siphalor.tweed.config.ConfigEnvironment;
import de.siphalor.tweed.config.ConfigFile;
import de.siphalor.tweed.config.ConfigScope;
import de.siphalor.tweed.config.TweedRegistry;
import de.siphalor.tweed.config.constraints.RangeConstraint;
import de.siphalor.tweed.config.entry.FloatEntry;

@SuppressWarnings("WeakerAccess")
public class Config {
	public static final ConfigFile FILE = TweedRegistry.registerConfigFile(BouncyLife.MOD_ID).setEnvironment(ConfigEnvironment.SERVER).setScope(ConfigScope.SMALLEST);

	public static final FloatEntry FORK_FACTOR = FILE.register("fork-power-factor", new FloatEntry(1.8F))
		.setComment("Sets the factor used when determining the velocity after the fork has been used");
	public static final FloatEntry FORK_ENTITY_FACTOR = FILE.register("fork-entity-power-factor", new FloatEntry(2.7F))
		.setComment("Sets the factor determining the speed of shot entities");
	public static final FloatEntry VELOCITY_DAMPENER = FILE.register("velocity-dampener", new FloatEntry(1.0F))
		.addConstraint(new RangeConstraint<Float>().greaterThan(0.0F))
		.setComment("The percentage the velocity is additionally changed to when bouncing of the ground");
	public static final FloatEntry SNEAK_VELOCITY_DAMPENER = FILE.register("sneak-velocity-dampener", new FloatEntry(0.5F))
		.addConstraint(new RangeConstraint<Float>().greaterThan(0.0F))
		.setComment("The percentage the velocity is additionally changed to when bouncing of the ground while sneaking");
	public static final FloatEntry Y_BOUNCE_TOLERANCE = FILE.register("y-bounce-tolerance", new FloatEntry(0.7F))
		.addConstraint(new RangeConstraint<Float>().greaterThan(0.0F))
		.setComment("Sets minimum velocity in y direction before stopping the player.");

	public static void initialize() {

	}
}
