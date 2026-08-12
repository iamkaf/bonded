package com.iamkaf.bonded.augment;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.api.augment.Augment;
import com.iamkaf.bonded.api.augment.AugmentApi;
import com.iamkaf.bonded.api.augment.AugmentEvents;
import com.iamkaf.amber.api.functions.v1.PlayerFunctions;
import com.iamkaf.bonded.leveling.levelers.ArmorLeveler;
import com.iamkaf.bonded.leveling.levelers.MeleeWeaponsLeveler;
import com.iamkaf.bonded.leveling.levelers.MiningToolsLeveler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public final class Augments {
    public static final Augment CAKE_DESTROYER = new Augment(
            Bonded.resource("cake_destroyer"),
            "augment.bonded.cake_destroyer",
            100,
            Augments::supportsCakeDestroyer
    );
    public static final Augment OCEANIC = new Augment(
            Bonded.resource("oceanic"),
            "augment.bonded.oceanic",
            7500,
            Augments::supportsOceanic
    );

    private static boolean initialized;

    private Augments() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        AugmentApi.register(CAKE_DESTROYER);
        AugmentApi.register(OCEANIC);
        AugmentEvents.ACTIVATED.register((context, result) -> PlayerFunctions.sendMessage(
                context.player(),
                net.minecraft.network.chat.Component.translatable(
                        "message.bonded.augment.activated",
                        result.augment().displayName(),
                        context.stack().getDisplayName()
                )
        ));

        CakeDestroyerAugment.init();
        initialized = true;
    }

    private static boolean supportsCakeDestroyer(ItemStack stack) {
        var leveler = Bonded.GEAR.getLeveler(stack);
        return leveler instanceof MeleeWeaponsLeveler || leveler instanceof MiningToolsLeveler;
    }

    private static boolean supportsOceanic(ItemStack stack) {
        if (!(Bonded.GEAR.getLeveler(stack) instanceof ArmorLeveler)) {
            return false;
        }
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot() == EquipmentSlot.LEGS;
    }
}
