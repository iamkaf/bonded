package com.iamkaf.bonded.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.world.entity.player.Player;

public interface GameEvents {
    Event<ShieldBlock> SHIELD_BLOCK = EventFactory.createLoop();

    interface ShieldBlock {
        void block(Player player, Float damage);
    }
}
