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
