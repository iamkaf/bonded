package com.iamkaf.bonded.api.event;

import com.iamkaf.bonded.component.ItemLevelContainer;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface BondEvent {
    Event<ExperienceGained> ITEM_EXPERIENCE_GAINED = EventFactory.createEventResult();
    Event<LeveledUp> ITEM_LEVELED_UP = EventFactory.createLoop();
    Event<Repaired> ITEM_REPAIRED = EventFactory.createLoop();
    Event<Upgraded> ITEM_UPGRADED = EventFactory.createLoop();

    interface ExperienceGained {
        EventResult experience(ItemStack gear, Player player, ItemLevelContainer component, Integer experienceAmount);
    }

    interface LeveledUp {
        void level(ItemStack gear, Player player, ItemLevelContainer component, Integer newLevel);
    }

    interface Repaired {
        void repair(ItemStack gear, Player player, ItemLevelContainer component, ItemStack material);
    }

    interface Upgraded {
        void upgrade(ItemStack oldGear, ItemStack newGear, Player player, ItemLevelContainer component, ItemStack material);
    }
}
