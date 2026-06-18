package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.amber.api.registry.v1.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public class Sounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Bonded.MOD_ID, Registries.SOUND_EVENT);

    // item_level: plays when an item levels up
    public static final Supplier<SoundEvent> ITEM_LEVEL = SOUNDS.register(
            Bonded.resource("item_level"),
            () -> SoundEvent.createVariableRangeEvent(Bonded.resource("item_level"))
    );

    // item_max_level: plays when an item reaches max level
    public static final Supplier<SoundEvent> ITEM_MAX_LEVEL = SOUNDS.register(
            Bonded.resource("item_max_level"),
            () -> SoundEvent.createVariableRangeEvent(Bonded.resource("item_max_level"))
    );

    public static void init() {
        SOUNDS.register();
    }
}
