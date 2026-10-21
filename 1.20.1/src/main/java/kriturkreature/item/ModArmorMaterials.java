package kriturkreature.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.function.Supplier;

public enum ModArmorMaterials implements ArmorMaterial {
    PROPELLER_HAT("kriturkreature:propeller_hat", 15, 15,
            SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(Items.IRON_NUGGET));

    private final String name;
    private final int durability;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final Supplier<Ingredient> repair;

    ModArmorMaterials(String name, int durability, int enchantability,
                      SoundEvent equipSound, Supplier<Ingredient> repair) {
        this.name = name;
        this.durability = durability;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.repair = repair;
    }

    @Override public int getDurabilityForType(ArmorItem.Type type) { return durability; }
    @Override public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 2;
            case CHESTPLATE -> 5;
            case LEGGINGS -> 4;
            case BOOTS -> 1;
            default -> 0;
        };
    }
    @Override public int getEnchantmentValue() { return enchantability; }
    @Override public SoundEvent getEquipSound() { return equipSound; }
    @Override public Ingredient getRepairIngredient() { return repair.get(); }
    @Override public String getName() { return name; }
    @Override public float getToughness() { return 0f; }
    @Override public float getKnockbackResistance() { return 0f; }
}