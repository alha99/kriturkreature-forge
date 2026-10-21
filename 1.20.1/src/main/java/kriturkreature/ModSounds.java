package kriturkreature;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Kriturkreature.MOD_ID);

    public static final RegistryObject<SoundEvent> HAPPY_1 = reg("kritur_happy_1");
    public static final RegistryObject<SoundEvent> HAPPY_2 = reg("kritur_happy_2");
    public static final RegistryObject<SoundEvent> HAPPY_3 = reg("kritur_happy_3");
    public static final RegistryObject<SoundEvent> HAPPY_4 = reg("kritur_happy_4");
    public static final RegistryObject<SoundEvent> SAD_1 = reg("kritur_sad_1");
    public static final RegistryObject<SoundEvent> SAD_2 = reg("kritur_sad_2");
    public static final RegistryObject<SoundEvent> SAD_3 = reg("kritur_sad_3");
    public static final RegistryObject<SoundEvent> HURT_1 = reg("kritur_hurt_1");
    public static final RegistryObject<SoundEvent> HURT_2 = reg("kritur_hurt_2");
    public static final RegistryObject<SoundEvent> DEATH = reg("kritur_death_1");
    public static final RegistryObject<SoundEvent> MAD = reg("kritur_mad");
    public static final RegistryObject<SoundEvent> IDLE = reg("kritur_idle");

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name, () ->
                SoundEvent.createVariableRangeEvent(new ResourceLocation(Kriturkreature.MOD_ID, name)));
    }
}