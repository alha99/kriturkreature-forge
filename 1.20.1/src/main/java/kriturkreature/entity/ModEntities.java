package kriturkreature.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, "kriturkreature");

    public static final RegistryObject<EntityType<KriturEntity>> KRITUR = ENTITIES.register("kritur",
            () -> EntityType.Builder.of(KriturEntity::new, MobCategory.AXOLOTLS)
                    .sized(0.6f, 0.9f)
                    .build("kriturkreature:kritur"));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}