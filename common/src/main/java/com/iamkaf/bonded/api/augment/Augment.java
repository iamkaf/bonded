package com.iamkaf.bonded.api.augment;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A registered item augment with its own bounded progression track.
 *
 * @param id                 stable persisted identifier
 * @param translationKey     translation key used for player-facing presentation
 * @param activationProgress progress required to activate the augment
 * @param supports           item eligibility rule
 */
public record Augment(
        Identifier id,
        String translationKey,
        int activationProgress,
        Predicate<ItemStack> supports
) {
    public Augment {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(translationKey, "translationKey");
        Objects.requireNonNull(supports, "supports");
        if (translationKey.isBlank()) {
            throw new IllegalArgumentException("Augment translation key cannot be blank: " + id);
        }
        if (activationProgress <= 0) {
            throw new IllegalArgumentException("Augment activation progress must be positive: " + id);
        }
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }
}
