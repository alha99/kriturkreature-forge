package kriturkreature.client.model;

import kriturkreature.entity.KriturEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.model.GeoModel;

@OnlyIn(Dist.CLIENT)
public class KriturBabyModel extends GeoModel<KriturEntity> {

    @Override
    public ResourceLocation getModelResource(KriturEntity animatable) {
        return new ResourceLocation("kriturkreature", "geo/kriturbaby.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KriturEntity animatable) {
        return new ResourceLocation("kriturkreature", "textures/entity/babykritur.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KriturEntity animatable) {
        return new ResourceLocation("kriturkreature", "animations/babykritur.animation.json");
    }
}