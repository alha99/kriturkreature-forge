package kriturkreature.client;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import kriturkreature.entity.ModEntities;
import kriturkreature.client.render.KriturRenderer;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "kriturkreature", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientMod {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntities.KRITUR.get(), KriturRenderer::new);
        });
    }
}