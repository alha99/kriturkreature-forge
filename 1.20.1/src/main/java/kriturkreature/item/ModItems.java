package kriturkreature.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeSpawnEggItem;
import kriturkreature.entity.ModEntities;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Consumer;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "kriturkreature");

    public static final RegistryObject<Item> PROPELLER_HAT = ITEMS.register("propeller_hat",
            () -> new PropellerHatItem(new Item.Properties())
    );

    public static final RegistryObject<Item> KRITUR_SPAWN_EGG = ITEMS.register("kritur_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.KRITUR, 0xFFFFFF, 0xFFFFFF, new Item.Properties())
    );

    public static void register(net.minecraftforge.eventbus.api.IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static class PropellerHatItem extends ArmorItem implements GeoItem {
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        public PropellerHatItem(Item.Properties properties) {
            super(ModArmorMaterials.PROPELLER_HAT, ArmorItem.Type.HELMET, properties);
        }

        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            consumer.accept(new IClientItemExtensions() {
                private GeoArmorRenderer<PropellerHatItem> renderer;

                @Override
                public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                        EquipmentSlot slot, HumanoidModel<?> original) {
                    if (this.renderer == null) this.renderer = new PropellerHatRenderer();
                    this.renderer.prepForRender(entity, stack, slot, original);
                    return this.renderer;
                }
            });
        }

        @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            controllers.add(new AnimationController<>(this, "propeller_controller", 0, state -> {
                state.getController().setAnimationSpeed(10.0f);
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.player.propellerspin"));
            }));
        }

        @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

        @OnlyIn(Dist.CLIENT)
        public static class PropellerHatRenderer extends GeoArmorRenderer<PropellerHatItem> {
            public PropellerHatRenderer() {
                super(new PropellerHatModel());
            }
        }

        @OnlyIn(Dist.CLIENT)
        public static class PropellerHatModel extends GeoModel<PropellerHatItem> {
            @Override public ResourceLocation getModelResource(PropellerHatItem object) {
                return new ResourceLocation("kriturkreature", "geo/propeller_hat.geo.json");
            }
            @Override public ResourceLocation getTextureResource(PropellerHatItem object) {
                return new ResourceLocation("kriturkreature", "textures/armor/propeller_hat.png");
            }
            @Override public ResourceLocation getAnimationResource(PropellerHatItem object) {
                return new ResourceLocation("kriturkreature", "animations/propeller_hat.animation.json");
            }
        }
    }
}