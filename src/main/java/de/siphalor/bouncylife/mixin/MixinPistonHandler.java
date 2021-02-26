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
