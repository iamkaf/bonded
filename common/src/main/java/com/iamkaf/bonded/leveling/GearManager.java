package com.iamkaf.bonded.leveling;

import com.iamkaf.amber.api.inventory.ItemHelper;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.registry.*;
import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;

public class GearManager {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static GearTypeLevelerRegistry gearTypeLevelerRegistry = new GearTypeLevelerRegistry();
    public static BondBonusRegistry bondBonusRegistry = new BondBonusRegistry();
    public static BlockExperienceRegistry blockExperienceRegistry = new BlockExperienceRegistry();
    private static boolean READY = false;

    public GearManager() {
        LifecycleEvent.SERVER_LEVEL_LOAD.register(GearManager::loadGearRegistries);
    }

    private static void loadGearRegistries(ServerLevel serverLevel) {
        if (READY) return;
        LOGGER.info("Now loading leveling registries...");
        READY = true;

        Registry<Block> blockRegistry = serverLevel.registryAccess().registryOrThrow(Registries.BLOCK);
        Optional<HolderSet.Named<Block>> ores = blockRegistry.getTag(Tags.ORES);
        ores.ifPresent(holders -> {
            LOGGER.info("Found {} ores [{}]", holders.size(), Tags.ORES.location());
            holders.stream()
                    .forEach(blockHolder -> blockExperienceRegistry.blocks.add(blockHolder.value(),
                            Bonded.CONFIG.experienceForMiningOres.get()
                    ));
        });

        Registry<Item> itemRegistry = serverLevel.registryAccess().registryOrThrow(Registries.ITEM);

        for (var type : gearTypeLevelerRegistry.gearTypeLevelers()) {
            TagKey<Item> tag = type.tag();
            Optional<HolderSet.Named<Item>> items = itemRegistry.getTag(tag);
            items.ifPresent(holders -> {
                LOGGER.info("Found {} {} [{}]", holders.size(), type.name(), tag.location());
                holders.stream()
                        .forEach(itemHolder -> gearTypeLevelerRegistry.add(itemHolder.value()
                                .arch$registryName(), type));
            });
        }
    }

    public ItemStack initComponent(ItemStack gear) {
        if (gear.isEmpty()) {
            return gear;
        }

        if (!isGear(gear)) {
            return gear;
        }

        ItemLevelContainer container = gear.get(DataComponents.ITEM_LEVEL_CONTAINER.get());

        if (container != null) {
            bondBonusRegistry.applyBonuses(gear, getLeveler(gear), container);
            return gear;
        }

        int maxExperience = getMaxExperienceForItemType(gear);

        ItemLevelContainer newContainer = ItemLevelContainer.make(maxExperience);
        gear.set(DataComponents.ITEM_LEVEL_CONTAINER.get(), newContainer);
        bondBonusRegistry.applyBonuses(gear, getLeveler(gear), newContainer);

        return gear;
    }

    public boolean hasEnoughLevelsToUpgrade(ItemStack tool) {
        var levelingComponent = DataComponents.ITEM_LEVEL_CONTAINER.get();

        var leveling = tool.get(levelingComponent);
        if (leveling == null) {
            return false;
        }

        return leveling.getLevel() >= Bonded.CONFIG.levelsToUpgrade.get();
    }

    public boolean hasEnoughExpToLevel(ItemLevelContainer container) {
        return container.getExperience() >= container.getMaxExperience();
    }

    private int getMaxExperienceForItemType(ItemStack gear) {
        GearTypeLeveler leveler = getLeveler(gear);

        if (leveler != null) {
            return leveler.getMaxExperience(gear);
        }

        return Bonded.CONFIG.defaultMaxExperienceForUnknownItems.get();
    }

    public @Nullable GearTypeLeveler getLeveler(ItemStack gear) {
        return gearTypeLevelerRegistry.get(gear);
    }

    public boolean isGear(ItemStack stack) {
        var leveler = Bonded.GEAR.getLeveler(stack);
        return leveler != null;
    }

    public Integer getExperienceForBlock(Block block) {
        var experience = blockExperienceRegistry.blocks.get(block);
        return experience == null ? 1 : experience;
    }

    public boolean giveItemExperience(ItemStack item, int amount) {
        if (item == null || item.isEmpty() || amount == 0) {
            return false;
        }

        var levelingComponent = DataComponents.ITEM_LEVEL_CONTAINER.get();
        if (!item.has(levelingComponent)) {
            item.set(levelingComponent, ItemLevelContainer.make(getMaxExperienceForItemType(item)));
        }

        ItemLevelContainer currentComponent = item.get(levelingComponent);

        assert currentComponent != null;

        boolean itemIsDoneLeveling =
                currentComponent.getLevel() == Bonded.CONFIG.levelsToUpgrade.get() && currentComponent.getExperience() < currentComponent.getMaxExperience();
        if (itemIsDoneLeveling) {
            return false;
        }

        var newComponent = Objects.requireNonNull(currentComponent).addExperience(amount).addBond(amount);
        if (hasEnoughExpToLevel(newComponent)) {
            item.set(levelingComponent, newComponent.addLevel(getMaxExperienceForItemType(item)));
            return true;
        } else {
            item.set(levelingComponent, newComponent);
        }
        return false;
    }
}
