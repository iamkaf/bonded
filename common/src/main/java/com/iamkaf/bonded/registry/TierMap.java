package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TierMap {
    private static final Map<Item, Item> upgradeMap = new HashMap<>();
    private static final Map<Item, TagKey<Item>> upgradeMaterialMap = new HashMap<>();
    private static final Map<Item, Item> repairMaterialMap = new HashMap<>();
    private static final Map<Item, Integer> experienceMap = new HashMap<>();

    public static void addUpgrade(Item from, Item to, TagKey<Item> material) {
        upgradeMap.put(from, to);
        upgradeMaterialMap.put(from, material);
    }

    public static @Nullable Item getUpgrade(Item from) {
        return upgradeMap.get(from);
    }

    public static @Nullable TagKey<Item> getUpgradeMaterial(Item from) {
        return upgradeMaterialMap.get(from);
    }

    public static void addRepairMaterial(Item from, Item material) {
        repairMaterialMap.put(from, material);
    }

    public static Map<Item, Item> getRepairMaterialMap() {
        return repairMaterialMap;
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
        addUpgrade(Items.WOODEN_AXE, Items.STONE_AXE, ToolMaterial.STONE.repairItems());
        addUpgrade(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, ToolMaterial.STONE.repairItems());
        addUpgrade(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, ToolMaterial.STONE.repairItems());
        addUpgrade(Items.WOODEN_HOE, Items.STONE_HOE, ToolMaterial.STONE.repairItems());
        addUpgrade(Items.WOODEN_SWORD, Items.STONE_SWORD, ToolMaterial.STONE.repairItems());

      
        // Stone to Copper
        addUpgrade(Items.STONE_AXE, Items.COPPER_AXE, ToolMaterial.COPPER.repairItems());
        addUpgrade(Items.STONE_PICKAXE, Items.COPPER_PICKAXE, ToolMaterial.COPPER.repairItems());
        addUpgrade(Items.STONE_SHOVEL, Items.COPPER_SHOVEL, ToolMaterial.COPPER.repairItems());
        addUpgrade(Items.STONE_HOE, Items.COPPER_HOE, ToolMaterial.COPPER.repairItems());
        addUpgrade(Items.STONE_SWORD, Items.COPPER_SWORD, ToolMaterial.COPPER.repairItems());

        // Copper to Iron
        addUpgrade(Items.COPPER_AXE, Items.IRON_AXE, ToolMaterial.IRON.repairItems());
        addUpgrade(Items.COPPER_PICKAXE, Items.IRON_PICKAXE, ToolMaterial.IRON.repairItems());
        addUpgrade(Items.COPPER_SHOVEL, Items.IRON_SHOVEL, ToolMaterial.IRON.repairItems());
        addUpgrade(Items.COPPER_HOE, Items.IRON_HOE, ToolMaterial.IRON.repairItems());
        addUpgrade(Items.COPPER_SWORD, Items.IRON_SWORD, ToolMaterial.IRON.repairItems());

        // Iron to Diamond
        addUpgrade(Items.IRON_AXE, Items.DIAMOND_AXE, ToolMaterial.DIAMOND.repairItems());
        addUpgrade(Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, ToolMaterial.DIAMOND.repairItems());
        addUpgrade(Items.IRON_SHOVEL, Items.DIAMOND_SHOVEL, ToolMaterial.DIAMOND.repairItems());
        addUpgrade(Items.IRON_HOE, Items.DIAMOND_HOE, ToolMaterial.DIAMOND.repairItems());
        addUpgrade(Items.IRON_SWORD, Items.DIAMOND_SWORD, ToolMaterial.DIAMOND.repairItems());

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

        addExperienceCap(Items.COPPER_AXE, 75);
        addExperienceCap(Items.COPPER_PICKAXE, 75);
        addExperienceCap(Items.COPPER_SHOVEL, 75);
        addExperienceCap(Items.COPPER_HOE, 75);
        addExperienceCap(Items.COPPER_SWORD, 75);

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
        addUpgrade(Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, ArmorMaterials.CHAINMAIL.repairIngredient());
        addUpgrade(
                Items.LEATHER_CHESTPLATE,
                Items.CHAINMAIL_CHESTPLATE,
                ArmorMaterials.CHAINMAIL.repairIngredient()
        );
        addUpgrade(
                Items.LEATHER_LEGGINGS,
                Items.CHAINMAIL_LEGGINGS,
                ArmorMaterials.CHAINMAIL.repairIngredient()
        );
        addUpgrade(Items.LEATHER_BOOTS, Items.CHAINMAIL_BOOTS, ArmorMaterials.CHAINMAIL.repairIngredient());

        // Chainmail to Iron
        addUpgrade(Items.CHAINMAIL_HELMET, Items.IRON_HELMET, ArmorMaterials.IRON.repairIngredient());
        addUpgrade(Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, ArmorMaterials.IRON.repairIngredient());
        addUpgrade(Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, ArmorMaterials.IRON.repairIngredient());
        addUpgrade(Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, ArmorMaterials.IRON.repairIngredient());

        // Iron to Diamond
        addUpgrade(Items.IRON_HELMET, Items.DIAMOND_HELMET, ArmorMaterials.DIAMOND.repairIngredient());
        addUpgrade(
                Items.IRON_CHESTPLATE,
                Items.DIAMOND_CHESTPLATE,
                ArmorMaterials.DIAMOND.repairIngredient()
        );
        addUpgrade(Items.IRON_LEGGINGS, Items.DIAMOND_LEGGINGS, ArmorMaterials.DIAMOND.repairIngredient());
        addUpgrade(Items.IRON_BOOTS, Items.DIAMOND_BOOTS, ArmorMaterials.DIAMOND.repairIngredient());

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

        addExperienceCap(Items.ELYTRA, 2000);

        // Utility Equipment
        addExperienceCap(Items.SHEARS, 200);
        addRepairMaterial(Items.SHEARS, Items.IRON_INGOT);

        addExperienceCap(Items.FISHING_ROD, 150);
        addRepairMaterial(Items.FISHING_ROD, Items.STRING);

        addExperienceCap(Items.BRUSH, 150);
        addRepairMaterial(Items.BRUSH, Items.FEATHER);

        addExperienceCap(Items.FLINT_AND_STEEL, 150);
        addRepairMaterial(Items.FLINT_AND_STEEL, Items.IRON_INGOT);
    }
}
