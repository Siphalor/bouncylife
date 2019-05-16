package de.siphalor.bouncylife.mixin.client;

import com.mojang.authlib.GameProfile;
import de.siphalor.bouncylife.Core;
import de.siphalor.bouncylife.util.IPlayerEntityModel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayerEntity extends PlayerEntity implements IPlayerEntityModel {

	MixinAbstractClientPlayerEntity(World world_1, GameProfile gameProfile_1) {
		super(world_1, gameProfile_1);
	}

	@Override
	public boolean bouncylife$isDisguisedAsSlime() {
        for(ItemStack stack : getArmorItems()) {
        	if(!Core.isSlimeArmor(stack))
        		return false;
		}
        return isInSneakingPose();
	}
}
