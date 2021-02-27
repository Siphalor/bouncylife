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

import de.siphalor.bouncylife.BouncyLife;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CampfireBlockEntity.class)
public abstract class MixinCampfireBlockEntity extends BlockEntity {
	public MixinCampfireBlockEntity(BlockEntityType<?> type) {
		super(type);
	}

	@Inject(
			method = "updateItemsBeingCooked",
			at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/entity/CampfireBlockEntity;getPos()Lnet/minecraft/util/math/BlockPos;"),
			locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void onItemsCooked(CallbackInfo callbackInfo, int i, ItemStack base, Inventory craftingInventory, ItemStack result, BlockPos blockPos) {
		world.playSound(null, blockPos, BouncyLife.soundSlimePop, SoundCategory.BLOCKS, 1F, 1F);
	}
}
