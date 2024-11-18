package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TierMap {
    private static final Map<Item, Item> upgradeMap = new HashMap<>();
    private static final Map<Item, Ingredient> upgradeMaterialMap = new HashMap<>();
    private static final Map<Item, Ingredient> repairMaterialMap = new HashMap<>();
    private static final Map<Item, Integer> experienceMap = new HashMap<>();

    public static void addUpgrade(Item from, Item to, Ingredient material) {
        upgradeMap.put(from, to);
        upgradeMaterialMap.put(from, material);
    }

    public static @Nullable Item getUpgrade(Item from) {
        return upgradeMap.get(from);
    }

    public static @Nullable Ingredient getUpgradeMaterial(Item from) {
        return upgradeMaterialMap.get(from);
    }

    public static void addRepairMaterial(Item from, Ingredient material) {
        repairMaterialMap.put(from, material);
    }

    public static @Nullable Ingredient getRepairMaterial(Item from) {
        return repairMaterialMap.get(from);
    }

    public static void addExperienceCap(Item gear, Integer maxExperience) {
        experienceMap.put(gear, maxExperience);
    }

    public static int getExperienceCap(Item gear) {
        return experienceMap.getOrDefault(gear, Bonded.CONFIG.defaultMaxExperienceForUnknownItems.get());
    }

    public static void init() {
        // -- Tools

        // Wooden to Stone
        addUpgrade(Items.WOODEN_AXE, Items.STONE_AXE, Tiers.STONE.getRepairIngredient());
        addUpgrade(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Tiers.STONE.getRepairIngredient());
        addUpgrade(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Tiers.STONE.getRepairIngredient());
        addUpgrade(Items.WOODEN_HOE, Items.STONE_HOE, Tiers.STONE.getRepairIngredient());
        addUpgrade(Items.WOODEN_SWORD, Items.STONE_SWORD, Tiers.STONE.getRepairIngredient());

        // Stone to Iron
        addUpgrade(Items.STONE_AXE, Items.IRON_AXE, Tiers.IRON.getRepairIngredient());
        addUpgrade(Items.STONE_PICKAXE, Items.IRON_PICKAXE, Tiers.IRON.getRepairIngredient());
        addUpgrade(Items.STONE_SHOVEL, Items.IRON_SHOVEL, Tiers.IRON.getRepairIngredient());
        addUpgrade(Items.STONE_HOE, Items.IRON_HOE, Tiers.IRON.getRepairIngredient());
        addUpgrade(Items.STONE_SWORD, Items.IRON_SWORD, Tiers.IRON.getRepairIngredient());

        // Iron to Diamond
        addUpgrade(Items.IRON_AXE, Items.DIAMOND_AXE, Tiers.DIAMOND.getRepairIngredient());
        addUpgrade(Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Tiers.DIAMOND.getRepairIngredient());
        addUpgrade(Items.IRON_SHOVEL, Items.DIAMOND_SHOVEL, Tiers.DIAMOND.getRepairIngredient());
        addUpgrade(Items.IRON_HOE, Items.DIAMOND_HOE, Tiers.DIAMOND.getRepairIngredient());
        addUpgrade(Items.IRON_SWORD, Items.DIAMOND_SWORD, Tiers.DIAMOND.getRepairIngredient());

        // Experience
        addExperienceCap(Items.WOODEN_AXE, 30);
        addExperienceCap(Items.WOODEN_PICKAXE, 30);
        addExperienceCap(Items.WOODEN_SHOVEL, 30);
        addExperienceCap(Items.WOODEN_HOE, 30);
        addExperienceCap(Items.WOODEN_SWORD, 30);

        addExperienceCap(Items.STONE_AXE, 50);
        addExperienceCap(Items.STONE_PICKAXE, 50);
        addExperienceCap(Items.STONE_SHOVEL, 50);
        addExperienceCap(Items.STONE_HOE, 50);
        addExperienceCap(Items.STONE_SWORD, 50);

        addExperienceCap(Items.IRON_AXE, 100);
        addExperienceCap(Items.IRON_PICKAXE, 100);
        addExperienceCap(Items.IRON_SHOVEL, 100);
        addExperienceCap(Items.IRON_HOE, 100);
        addExperienceCap(Items.IRON_SWORD, 100);

        addExperienceCap(Items.DIAMOND_AXE, 500);
        addExperienceCap(Items.DIAMOND_PICKAXE, 500);
        addExperienceCap(Items.DIAMOND_SHOVEL, 500);
        addExperienceCap(Items.DIAMOND_HOE, 500);
        addExperienceCap(Items.DIAMOND_SWORD, 500);

        addExperienceCap(Items.NETHERITE_AXE, 1000);
        addExperienceCap(Items.NETHERITE_PICKAXE, 1000);
        addExperienceCap(Items.NETHERITE_SHOVEL, 1000);
        addExperienceCap(Items.NETHERITE_HOE, 1000);
        addExperienceCap(Items.NETHERITE_SWORD, 1000);

        addExperienceCap(Items.GOLDEN_AXE, 300);
        addExperienceCap(Items.GOLDEN_PICKAXE, 300);
        addExperienceCap(Items.GOLDEN_SHOVEL, 300);
        addExperienceCap(Items.GOLDEN_HOE, 300);
        addExperienceCap(Items.GOLDEN_SWORD, 300);

        // -- Armor

        // Leather to Chainmail
        addUpgrade(Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, ArmorMaterials.CHAIN.value().repairIngredient().get());
        addUpgrade(Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, ArmorMaterials.CHAIN.value().repairIngredient().get());
        addUpgrade(Items.LEATHER_LEGGINGS, Items.CHAINMAIL_LEGGINGS, ArmorMaterials.CHAIN.value().repairIngredient().get());
        addUpgrade(Items.LEATHER_BOOTS, Items.CHAINMAIL_BOOTS, ArmorMaterials.CHAIN.value().repairIngredient().get());

        // Chainmail to Iron
        addUpgrade(Items.CHAINMAIL_HELMET, Items.IRON_HELMET, ArmorMaterials.IRON.value().repairIngredient().get());
        addUpgrade(Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, ArmorMaterials.IRON.value().repairIngredient().get());
        addUpgrade(Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, ArmorMaterials.IRON.value().repairIngredient().get());
        addUpgrade(Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, ArmorMaterials.IRON.value().repairIngredient().get());

        // Iron to Diamond
        addUpgrade(Items.IRON_HELMET, Items.DIAMOND_HELMET, ArmorMaterials.DIAMOND.value().repairIngredient().get());
        addUpgrade(Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, ArmorMaterials.DIAMOND.value().repairIngredient().get());
        addUpgrade(Items.IRON_LEGGINGS, Items.DIAMOND_LEGGINGS, ArmorMaterials.DIAMOND.value().repairIngredient().get());
        addUpgrade(Items.IRON_BOOTS, Items.DIAMOND_BOOTS, ArmorMaterials.DIAMOND.value().repairIngredient().get());

        // Experience
        addExperienceCap(Items.LEATHER_HELMET, 90);
        addExperienceCap(Items.LEATHER_CHESTPLATE, 90);
        addExperienceCap(Items.LEATHER_LEGGINGS, 90);
        addExperienceCap(Items.LEATHER_BOOTS, 90);

        addExperienceCap(Items.CHAINMAIL_HELMET, 150);
        addExperienceCap(Items.CHAINMAIL_CHESTPLATE, 150);
        addExperienceCap(Items.CHAINMAIL_LEGGINGS, 150);
        addExperienceCap(Items.CHAINMAIL_BOOTS, 150);

        addExperienceCap(Items.IRON_HELMET, 300);
        addExperienceCap(Items.IRON_CHESTPLATE, 300);
        addExperienceCap(Items.IRON_LEGGINGS, 300);
        addExperienceCap(Items.IRON_BOOTS, 300);

        addExperienceCap(Items.DIAMOND_HELMET, 1500);
        addExperienceCap(Items.DIAMOND_CHESTPLATE, 1500);
        addExperienceCap(Items.DIAMOND_LEGGINGS, 1500);
        addExperienceCap(Items.DIAMOND_BOOTS, 1500);

        addExperienceCap(Items.NETHERITE_HELMET, 3000);
        addExperienceCap(Items.NETHERITE_CHESTPLATE, 3000);
        addExperienceCap(Items.NETHERITE_LEGGINGS, 3000);
        addExperienceCap(Items.NETHERITE_BOOTS, 3000);

        addExperienceCap(Items.GOLDEN_HELMET, 500);
        addExperienceCap(Items.GOLDEN_CHESTPLATE, 500);
        addExperienceCap(Items.GOLDEN_LEGGINGS, 500);
        addExperienceCap(Items.GOLDEN_BOOTS, 500);

        addExperienceCap(Items.TURTLE_HELMET, 900);
    }
}
