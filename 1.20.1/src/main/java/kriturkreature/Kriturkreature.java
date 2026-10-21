package kriturkreature;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kriturkreature.entity.KriturEntity;
import kriturkreature.entity.ModEntities;
import kriturkreature.entity.ModEntitySpawns;
import kriturkreature.item.ModItems;

@Mod("kriturkreature")
public class Kriturkreature {
    public static final String MOD_ID = "kriturkreature";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Kriturkreature() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.register(modBus);
        ModEntitySpawns.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModItems.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerAttributes);
        modBus.addListener(this::addCreative);
    }

    private void commonSetup(FMLCommonSetupEvent event) {}

    private void clientSetup(FMLClientSetupEvent event) {}

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.KRITUR.get(), KriturEntity.createAttributes().build());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.KRITUR_SPAWN_EGG);
        }
    }
}