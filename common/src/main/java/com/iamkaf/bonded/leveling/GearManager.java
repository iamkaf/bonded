package com.iamkaf.bonded.leveling;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.ItemLevelContainer;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.network.BondedNetworking;
import com.iamkaf.bonded.registry.*;
import com.iamkaf.bonded.rules.BondedRules;
import com.mojang.logging.LogUtils;
import com.iamkaf.amber.api.event.v1.events.common.WorldEvents;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;

public class GearManager {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static GearTypeLevelerRegistry gearTypeLevelerRegistry = new GearTypeLevelerRegistry();
    public static BondBonusRegistry bondBonusRegistry = new BondBonusRegistry();
    public static BlockExperienceRegistry blockExperienceRegistry = new BlockExperienceRegistry();
    private static volatile MinecraftServer loadedServer;

    public GearManager() {
        LOGGER.info("Registering WorldEvents.WORLD_LOAD");
        WorldEvents.WORLD_LOAD.register((server, level) -> {
            LOGGER.info("WORLD_LOAD event received for level: {}", level.dimensionType().toString());
            if (level instanceof ServerLevel serverLevel) {
                GearManager.loadGearRegistries(serverLevel);
            }
        });
        WorldEvents.WORLD_UNLOAD.register((server, level) -> {
            if (level instanceof ServerLevel serverLevel
                    && serverLevel.dimension() == Level.OVERWORLD
                    && loadedServer == server) {
                loadedServer = null;
                BondedRules.clearServerState();
            }
        });
    }

    private static void loadGearRegistries(ServerLevel serverLevel) {
        MinecraftServer server = serverLevel.getServer();
        if (loadedServer == server) return;
        LOGGER.info("Now loading leveling registries...");

        Registry<Item> itemRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.ITEM);
        BondedRules.resolve(itemRegistry);
        gearTypeLevelerRegistry.clear();

        Registry<Block> blockRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.BLOCK);
        Optional<HolderSet.Named<Block>> ores = blockRegistry.get(Tags.ORES);
        ores.ifPresent(holders -> {
            LOGGER.info("Found {} ores [{}]", holders.size(), Tags.ORES.location());
            holders.stream()
                    .forEach(blockHolder -> blockExperienceRegistry.blocks.put(blockHolder.value(),
                            Bonded.CONFIG.experienceForMiningOres.get()
                    ));
        });

        LOGGER.info("Processing {} gear type levelers", gearTypeLevelerRegistry.gearTypeLevelers().size());
        for (var type : gearTypeLevelerRegistry.gearTypeLevelers()) {
            TagKey<Item> tag = type.tag();
            LOGGER.info("Processing leveler: {} with tag: {}", type.name(), tag.location());
            Optional<HolderSet.Named<Item>> items = itemRegistry.get(tag);
            items.ifPresent(holders -> {
                LOGGER.info("Found {} {} [{}]", holders.size(), type.name(), tag.location());
                holders.stream()
                        .filter(itemHolder -> type.supports(itemHolder.value().getDefaultInstance()))
                        .forEach(itemHolder -> gearTypeLevelerRegistry.add(itemRegistry.getKey(itemHolder.value()), type));
            });
            if (items.isEmpty()) {
                LOGGER.warn("No items found for tag: {} [{}]", type.name(), tag.location());
            }
        }
        loadedServer = server;
    }

    /** Re-resolves persisted rules on the server thread and refreshes every connected client. */
    public static void reloadGearRules() {
        MinecraftServer server = loadedServer;
        if (server == null) {
            return;
        }
        Runnable reload = () -> {
            if (loadedServer != server) {
                return;
            }
            Registry<Item> itemRegistry = server.registryAccess().lookupOrThrow(Registries.ITEM);
            BondedRules.resolve(itemRegistry);
            BondedNetworking.broadcastGearRules(server);
        };
        if (server.isSameThread()) {
            reload.run();
        } else {
            server.execute(reload);
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
            if (needsMigration(gear)) {
                return migrateLegacyItem(gear, container);
            }
            bondBonusRegistry.applyBonuses(gear, getLeveler(gear), container);
            return gear;
        }

        int maxExperience = getMaxExperienceForItemType(gear);

        ItemLevelContainer newContainer = ItemLevelContainer.make(maxExperience);
        gear.set(DataComponents.ITEM_LEVEL_CONTAINER.get(), newContainer);
        bondBonusRegistry.applyBonuses(gear, getLeveler(gear), newContainer);

        return gear;
    }

    public ItemStack initComponentWithBond(ItemStack gear, int bond) {
        if (bond <= 0 || gear.isEmpty() || !isGear(gear)) {
            return gear;
        }

        if (gear.has(DataComponents.ITEM_LEVEL_CONTAINER.get())) {
            return gear;
        }

        int maxExperience = getMaxExperienceForItemType(gear);
        ItemLevelContainer container = ItemLevelContainer.make(maxExperience).addBond(bond);
        gear.set(DataComponents.ITEM_LEVEL_CONTAINER.get(), container);
        bondBonusRegistry.applyBonuses(gear, getLeveler(gear), container);
        return gear;
    }

    private boolean needsMigration(ItemStack gear) {
        return bondBonusRegistry.hasLegacyManagedAttributeModifiers(gear);
    }

    private ItemStack migrateLegacyItem(ItemStack gear, ItemLevelContainer container) {
        LOGGER.info("Migrating legacy Bonded attribute modifiers for {}", gear.getItem());
        bondBonusRegistry.applyBonuses(gear, getLeveler(gear), container);
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
        LOGGER.debug("isGear: item={}, isGear={}", stack.getItem().toString(), leveler != null);
        return leveler != null;
    }

    public Integer getExperienceForBlock(Block block) {
        var experience = blockExperienceRegistry.blocks.get(block);
        return experience == null ? 1 : experience;
    }

    public int getExperienceForBlock(BlockState state) {
        Integer experience = blockExperienceRegistry.blocks.get(state.getBlock());
        if (experience != null) {
            return experience;
        }
        return state.is(Tags.ORES) ? Bonded.CONFIG.experienceForMiningOres.get() : 1;
    }

    public boolean giveItemExperience(ItemStack item, int amount) {
        LOGGER.debug("giveItemExperience: item={}, amount={}", item.getItem().toString(), amount);
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
