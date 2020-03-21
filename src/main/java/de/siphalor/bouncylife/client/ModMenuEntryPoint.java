package de.siphalor.bouncylife.client;

import de.siphalor.bouncylife.Config;
import de.siphalor.bouncylife.BouncyLife;
import de.siphalor.tweed.client.TweedClothBridge;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public class ModMenuEntryPoint implements ModMenuApi {
	private static final TweedClothBridge TWEED_CLOTH_BRIDGE = new TweedClothBridge(Config.FILE);

	@Override
	public String getModId() {
		return BouncyLife.MOD_ID;
	}

	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return (parent) -> TWEED_CLOTH_BRIDGE.buildScreen();
	}
}
