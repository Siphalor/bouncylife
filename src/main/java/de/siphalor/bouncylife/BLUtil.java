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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.DyeColor;
import net.minecraft.world.World;

import java.util.List;

public class BLUtil {
	public static DyeColor mixColors(DyeColor a, DyeColor b, World world) {
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		CraftingInventory tempInventory = new CraftingInventory(new ScreenHandler(null, -1) {
			@Override
			public boolean canUse(PlayerEntity player) {
				return false;
			}
		}, 2, 1);
		tempInventory.setStack(0, new ItemStack(DyeItem.byColor(a)));
		tempInventory.setStack(1, new ItemStack(DyeItem.byColor(b)));
		List<CraftingRecipe> matches = world.getRecipeManager().getAllMatches(RecipeType.CRAFTING, tempInventory, world);
		ItemStack stack;
		for (CraftingRecipe match : matches) {
			stack = match.craft(tempInventory);
			if (stack.getItem() instanceof DyeItem) {
				return ((DyeItem) stack.getItem()).getColor();
			}
		}
		return null;
	}
}
