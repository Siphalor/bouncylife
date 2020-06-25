package de.siphalor.bouncylife.client.render;

import de.siphalor.bouncylife.entity.PetSlimeEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.SlimeOverlayFeatureRenderer;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PetSlimeEntityRenderer extends MobEntityRenderer<PetSlimeEntity, SlimeEntityModel<PetSlimeEntity>> {
   private static final Identifier TEXTURE = new Identifier("textures/entity/slime/slime.png");

   public PetSlimeEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      super(entityRenderDispatcher, new SlimeEntityModel<>(16), 0.25F);
      //this.addFeature(new SlimeOverlayFeatureRenderer<>(this));
      this.addFeature(new PetSlimeEntityBowFeatureRenderer(this));
   }

   @Override
   public void render(PetSlimeEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      this.shadowSize = 0.25F * (float)entity.getSize();
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
      return TEXTURE;
   }
}
