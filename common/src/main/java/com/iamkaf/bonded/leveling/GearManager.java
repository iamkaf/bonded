package com.iamkaf.bonded.leveling;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.types.GearTypeLeveler;
import com.iamkaf.bonded.registry.BlockExperienceRegistry;
import com.iamkaf.bonded.registry.BondBonusRegistry;
import com.iamkaf.bonded.registry.DataComponents;
import com.iamkaf.bonded.registry.GearTypeLevelerRegistry;
import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;

public class GearManager {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static GearTypeLevelerRegistry gearTypeLevelerRegistry = new GearTypeLevelerRegistry();
    public static BondBonusRegistry bondBonusRegistry = new BondBonusRegistry();
    public static BlockExperienceRegistry blockExperienceRegistry = new BlockExperienceRegistry();

    public GearManager() {
        LifecycleEvent.SERVER_LEVEL_LOAD.register(GearManager::loadGearRegistries);
    }

    private static void loadGearRegistries(ServerLevel serverLevel) {
        LOGGER.info("Now loading leveling registries...");
        Registry<Block> registry = serverLevel.registryAccess().registryOrThrow(Registries.BLOCK);
        Optional<HolderSet.Named<Block>> ores = registry.getTag(TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath("c", "ores")
        ));
        ores.ifPresent(holders -> {
            LOGGER.info("Found {} ores", holders.size());
            holders.stream().forEach(blockHolder -> blockExperienceRegistry.blocks.add(
                    blockHolder.value(),
                    Bonded.CONFIG.experienceForMiningOres.get()
            ));
        });

        // TODO: add gear types using tags
        // TODO: add gear bonuses
        throw new NotImplementedException("implement me kaf!!!!!!!! ♥");
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
            return gear;
        }

        int maxExperience = getMaxExperienceForItemType(gear);

        gear.set(DataComponents.ITEM_LEVEL_CONTAINER.get(), ItemLevelContainer.make(maxExperience));

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

    public boolean isUpgradable(ItemStack gear) {
        GearTypeLeveler leveler = getLeveler(gear);

        if (leveler != null) {
            return leveler.isUpgradable(gear);
        }

        return false;
    }

    private int getMaxExperienceForItemType(ItemStack gear) {
        GearTypeLeveler leveler = getLeveler(gear);

        if (leveler != null) {
            return leveler.getMaxExperience(gear);
        }

        return Bonded.CONFIG.defaultMaxExperienceForUnknownItems.get();
    }

    public ItemStack upgrade(ItemStack gear) {
        GearTypeLeveler leveler = getLeveler(gear);

        if (leveler != null) {
            return leveler.transmuteUpgrade(gear);
        }
        return gear;
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
