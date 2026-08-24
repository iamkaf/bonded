package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.rules.BondedRules;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

@Deprecated(forRemoval = false)
public class TierMap {

    public static void addUpgrade(Item from, Item to, TagKey<Item> material) {
        addUpgrade(from, () -> to, material);
    }

    public static void addUpgrade(Item from, Supplier<Item> to, TagKey<Item> material) {
        BondedRules.addApiUpgrade(() -> from, to, material);
    }

    public static @Nullable Item getUpgrade(Item from) {
        return BondedRules.upgrade(from);
    }

    public static @Nullable TagKey<Item> getUpgradeMaterial(Item from) {
        return BondedRules.upgradeIngredient(from);
    }

    public static void addRepairMaterial(Item from, Item material) {
        BondedRules.addApiRepair(from, material);
    }

    public static Map<Item, Item> getRepairMaterialMap() {
        Map<Item, Item> repairs = new LinkedHashMap<>();
        BondedRules.active().rules().values().stream()
                .filter(rule -> rule.enabled()
                        && rule.repairMode() == com.iamkaf.bonded.rules.ResolvedGearRule.RepairMode.ITEM
                        && rule.repair() != null)
                .forEach(rule -> {
                    var itemId = net.minecraft.resources.Identifier.tryParse(rule.item());
                    var repairId = net.minecraft.resources.Identifier.tryParse(rule.repair());
                    if (itemId != null && repairId != null) {
                        repairs.put(
                                net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(itemId),
                                net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(repairId)
                        );
                    }
                });
        return Map.copyOf(repairs);
    }

    public static void addExperienceCap(Item gear, Integer maxExperience) {
        BondedRules.addApiExperienceCap(gear, maxExperience);
    }

    public static void addExperienceCap(Supplier<Item> gear, Integer maxExperience) {
        BondedRules.addApiExperienceCap(gear, maxExperience);
    }

    public static int getExperienceCap(Item gear) {
        return BondedRules.experienceCap(gear);
    }

    public static void init() {
        // Shipped rules are loaded from data/bonded/gear_rules at first world load.
    }
}
