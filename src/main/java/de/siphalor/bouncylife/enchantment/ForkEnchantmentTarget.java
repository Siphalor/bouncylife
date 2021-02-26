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

package de.siphalor.bouncylife.enchantment;

import de.siphalor.bouncylife.SlimeForkItem;
import de.siphalor.bouncylife.mixin.MixinEnchantmentTarget;
import net.minecraft.item.Item;

@SuppressWarnings("unused")
public class ForkEnchantmentTarget extends MixinEnchantmentTarget {
	@Override
	public boolean isAcceptableItem(Item item) {
		return item instanceof SlimeForkItem;
	}
}
