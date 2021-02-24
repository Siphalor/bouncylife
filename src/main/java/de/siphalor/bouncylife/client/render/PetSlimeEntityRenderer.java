package de.siphalor.bouncylife.client.render;

import de.siphalor.bouncylife.BouncyLife;
import de.siphalor.bouncylife.entity.PetSlimeEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PetSlimeEntityRenderer extends MobEntityRenderer<PetSlimeEntity, PetSlimeEntityModel<PetSlimeEntity>> {
   private static final Identifier TEXTURE = new Identifier("textures/entity/slime/slime.png");
   private static final Identifier TINTABLE_TEXTURE = new Identifier(BouncyLife.MOD_ID, "textures/entity/pet_slime/tintable.png");

   public PetSlimeEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      super(entityRenderDispatcher, new PetSlimeEntityModel<>(16), 0.25F);
      this.addFeature(new PetSlimeOverlayFeatureRenderer<>(this));
      this.addFeature(new PetSlimeBowFeatureRenderer(this));
      this.addFeature(new PetSlimeSaddleFeatureRenderer(this));
   }

   @Override
   public void render(PetSlimeEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      this.shadowRadius = 0.25F * (float)entity.getSize();
      DyeColor dyeColor = entity.getColor();
      if (dyeColor == null) {
         model.setColorMultiplier(1F, 1F, 1F);
      } else {
         float[] color = dyeColor.getColorComponents();
         model.setColorMultiplier(color[0], color[1], color[2]);
      }
      super.render(entity, f, g, matrixStack, vertexConsumerProvider, i);
   }

   protected void scale(PetSlimeEntity entity, MatrixStack matrixStack, float f) {
      float g = 0.999F;
      matrixStack.scale(0.999F, 0.999F, 0.999F);
      matrixStack.translate(0.0D, 0.0010000000474974513D, 0.0D);
      float h = (float)entity.getSize();
      float i = MathHelper.lerp(f, entity.lastStretch, entity.stretch) / (h * 0.5F + 1.0F);
      float j = 1.0F / (i + 1.0F);
      matrixStack.scale(j * h, 1.0F / j * h, j * h);
   }

   public Identifier getTexture(PetSlimeEntity entity) {
      return entity.getColor() == null ? TEXTURE : TINTABLE_TEXTURE;
   }
}
