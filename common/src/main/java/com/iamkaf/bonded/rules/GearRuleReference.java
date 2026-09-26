package com.iamkaf.bonded.rules;

import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/** Classifies persisted item references without requiring their owning mod to be loaded. */
final class GearRuleReference {
    private static final Pattern IDENTIFIER = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    private GearRuleReference() {
    }

    static Availability item(String value, Set<String> availableItems) {
        return itemMatching(value, availableItems::contains);
    }

    static Availability itemMatching(String value, Predicate<String> itemPresent) {
        if (!validIdentifier(value)) {
            return Availability.INVALID;
        }
        return itemPresent.test(value) ? Availability.PRESENT : Availability.DORMANT;
    }

    static boolean validIdentifier(String value) {
        return value != null && IDENTIFIER.matcher(value).matches();
    }

    static boolean validPersistedItem(String value) {
        // A missing owner mod makes this dormant, not invalid. The resolver checks presence.
        return validIdentifier(value);
    }

    static boolean validUpgradePair(String target, String ingredient) {
        boolean hasTarget = target != null && !target.isBlank();
        boolean hasIngredient = ingredient != null && !ingredient.isBlank();
        if (hasTarget != hasIngredient) {
            return false;
        }
        if (!hasTarget) {
            return true;
        }
        String ingredientId = ingredient.startsWith("#") ? ingredient.substring(1) : ingredient;
        return validPersistedItem(target) && validIdentifier(ingredientId);
    }

    static UpgradeMaterial upgradeMaterial(String value, boolean tagPresent, boolean itemPresent) {
        String id = value != null && value.startsWith("#") ? value.substring(1) : value;
        if (!validIdentifier(id)) {
            return UpgradeMaterial.INVALID;
        }
        if (tagPresent) {
            return UpgradeMaterial.TAG;
        }
        if (!value.startsWith("#") && itemPresent) {
            if (id.equals("minecraft:air")) {
                return UpgradeMaterial.INVALID;
            }
            return UpgradeMaterial.ITEM;
        }
        return UpgradeMaterial.DORMANT;
    }

    enum UpgradeMaterial {
        ITEM,
        TAG,
        DORMANT,
        INVALID
    }

    enum Availability {
        INVALID,
        DORMANT,
        PRESENT
    }
}
