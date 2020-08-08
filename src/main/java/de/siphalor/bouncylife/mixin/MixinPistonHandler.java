package de.siphalor.bouncylife.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlimeBlock;
import net.minecraft.block.piston.PistonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonHandler.class)
public class MixinPistonHandler {
	@Inject(method = "isBlockSticky", cancellable = true, at = @At("TAIL"))
	private static void isBlockSticky(Block block, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (block instanceof SlimeBlock)
			callbackInfoReturnable.setReturnValue(true);
	}

	@Inject(method = "isAdjacentBlockStuck", cancellable = true, at = @At("TAIL"))
	private static void areStickingTogether(Block block1, Block block2, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (block1 == Blocks.HONEY_BLOCK && block2 instanceof SlimeBlock)
			callbackInfoReturnable.setReturnValue(false);
		if (block2 == Blocks.HONEY_BLOCK && block1 instanceof SlimeBlock)
			callbackInfoReturnable.setReturnValue(false);
		if (block1 instanceof SlimeBlock && block2 instanceof SlimeBlock)
			callbackInfoReturnable.setReturnValue(block1 == block2);
	}
}
