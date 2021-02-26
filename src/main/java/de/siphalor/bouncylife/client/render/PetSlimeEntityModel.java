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

package de.siphalor.bouncylife.client.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.TintableCompositeModel;
import net.minecraft.entity.Entity;

public class PetSlimeEntityModel<T extends Entity> extends TintableCompositeModel<T> {
	private final ModelPart innerCube;
	private final ModelPart rightEye;
	private final ModelPart leftEye;
	private final ModelPart mouth;

	public PetSlimeEntityModel(int i) {
		this.innerCube = new ModelPart(this, 0, i);
		this.rightEye = new ModelPart(this, 32, 0);
		this.leftEye = new ModelPart(this, 32, 4);
		this.mouth = new ModelPart(this, 32, 8);
		if (i > 0) {
			this.innerCube.addCuboid(-3.0F, 17.0F, -3.0F, 6.0F, 6.0F, 6.0F);
			this.rightEye.addCuboid(-3.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F);
			this.leftEye.addCuboid(1.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F);
			this.mouth.addCuboid(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F);
		} else if (i == -1) {
			this.innerCube.addCuboid(-5.0F, 14.0F, -4.001F, 10.0F, 10.0F, 8.0F);
		} else {
			this.innerCube.addCuboid(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F);
		}

	}

	public void setAngles(T entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
	}

	public Iterable<ModelPart> getParts() {
		return ImmutableList.of(this.innerCube, this.rightEye, this.leftEye, this.mouth);
	}
}
