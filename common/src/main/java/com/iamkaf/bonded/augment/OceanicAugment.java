package com.iamkaf.bonded.augment;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.api.augment.AugmentApi;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public final class OceanicAugment {
    private static final int PROGRESS_INTERVAL_TICKS = 10;
    private static final Identifier WATER_MOVEMENT_ID = Bonded.resource("oceanic_water_movement");
    private static final AttributeModifier WATER_MOVEMENT = new AttributeModifier(
            WATER_MOVEMENT_ID,
            1.0,
            AttributeModifier.Operation.ADD_VALUE
    );

    private OceanicAugment() {
    }

    public static void tick(ServerPlayer player) {
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        if (player.tickCount % PROGRESS_INTERVAL_TICKS == 0 && player.isUnderWater()) {
            AugmentApi.addProgress(player, leggings, Augments.OCEANIC, 1);
        }

        boolean activeWhileSwimming = player.isSwimming() && AugmentApi.isActive(leggings, Augments.OCEANIC);
        updateWaterMovement(player, activeWhileSwimming);
    }

    private static void updateWaterMovement(ServerPlayer player, boolean enabled) {
        AttributeInstance attribute = player.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY);
        if (attribute == null) {
            return;
        }
        if (enabled && !attribute.hasModifier(WATER_MOVEMENT_ID)) {
            attribute.addTransientModifier(WATER_MOVEMENT);
        } else if (!enabled && attribute.hasModifier(WATER_MOVEMENT_ID)) {
            attribute.removeModifier(WATER_MOVEMENT_ID);
        }
    }
}
