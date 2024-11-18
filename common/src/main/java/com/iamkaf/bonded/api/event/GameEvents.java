package com.iamkaf.bonded.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface GameEvents {
    Event<ShieldBlock> SHIELD_BLOCK = EventFactory.createLoop();
    Event<ModifySmithingResult> MODIFY_SMITHING_RESULT = EventFactory.createLoop();

    interface ShieldBlock {
        void block(Player player, Float damage);
    }

    interface ModifySmithingResult {
        void smith(ItemStack stack, List<ItemStack> relevantItems);
    }
}
