package kriturkreature.client.render;

import kriturkreature.client.model.KriturBabyModel;
import kriturkreature.client.model.KriturModel;
import kriturkreature.entity.KriturEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@OnlyIn(Dist.CLIENT)
public class KriturRenderer extends GeoEntityRenderer<KriturEntity> {

    private final KriturModel adultModel = new KriturModel();
    private final KriturBabyModel babyModel = new KriturBabyModel();

    public KriturRenderer(EntityRendererProvider.Context context) {
        super(context, new KriturModel());
        withScale(0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(KriturEntity animatable) {
        if (animatable.isBaby()) {
            return new ResourceLocation("kriturkreature", "textures/entity/babykritur.png");
        }
        return new ResourceLocation("kriturkreature", "textures/entity/kritur.png");
    }

    @Override
    public GeoModel<KriturEntity> getGeoModel() {
        if (this.animatable.isBaby()) {
            return babyModel;
        }
        return adultModel;
    }
}