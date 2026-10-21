package kriturkreature.entity;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = "kriturkreature", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntitySpawns {

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {}

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> SpawnPlacements.register(
                ModEntities.KRITUR.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) -> {
                    if (!level.getBiome(pos).is(Biomes.LUSH_CAVES)) return false;
                    return level.getBlockState(pos.below()).isSolid() || level.getBlockState(pos).is(Blocks.WATER);
                }
        ));
    }
}