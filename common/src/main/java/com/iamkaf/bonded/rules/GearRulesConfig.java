package com.iamkaf.bonded.rules;

import com.iamkaf.konfig.api.v1.ConfigBuilder;
import com.iamkaf.konfig.api.v1.ConfigValue;
import com.iamkaf.konfig.api.v1.RestartRequirement;
import com.iamkaf.konfig.api.v1.fieldset.FieldsetBuilder;
import com.iamkaf.konfig.api.v1.fieldset.FieldsetEntry;
import com.iamkaf.konfig.api.v1.fieldset.FieldsetField;
import com.iamkaf.konfig.api.v1.fieldset.FieldsetValue;
import com.iamkaf.bonded.Bonded;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** The user-owned gear rule Fieldset and its conversion into rule declarations. */
public final class GearRulesConfig {
    private static final FieldsetField<String> SELECTOR =
            FieldsetField.registryString("selector", "minecraft:iron_sword", Registries.ITEM);
    private static final FieldsetField<String> TYPE = FieldsetField.dropdown(
            "type",
            "inherit",
            List.of("inherit", "armor", "melee_weapon", "ranged_weapon", "mining_tool", "utility")
    );
    private static final FieldsetField<Integer> EXPERIENCE_CAP =
            FieldsetField.intRange("experience_cap", 1000, 1, 1_000_000);
    private static final FieldsetField<String> REPAIR_MODE = FieldsetField.dropdown(
            "repair_mode",
            "inherit",
            List.of("inherit", "none", "item", "tag")
    );
    private static final FieldsetField<String> REPAIR =
            FieldsetField.registryString("repair", "minecraft:iron_ingot", Registries.ITEM);
    private static final FieldsetField<String> UPGRADE_TO =
            FieldsetField.registryString("upgrade_to", "", Registries.ITEM);
    private static final FieldsetField<Optional<String>> UPGRADE_INGREDIENT =
            FieldsetField.optionalString("upgrade_ingredient");
    private static final FieldsetField<Boolean> ENABLED = FieldsetField.bool("enabled", true);

    private final ConfigValue<FieldsetValue> value;
    private volatile FieldsetValue remoteView;
    private volatile boolean remoteViewAvailable;

    public GearRulesConfig(ConfigBuilder builder) {
        FieldsetBuilder fieldset = FieldsetBuilder.create()
                .field(SELECTOR)
                .field(TYPE)
                .field(EXPERIENCE_CAP)
                .field(REPAIR_MODE)
                .field(REPAIR)
                .field(UPGRADE_TO)
                .field(UPGRADE_INGREDIENT)
                .field(ENABLED)
                .title(SELECTOR)
                .icon(SELECTOR)
                .key(SELECTOR)
                .summary(TYPE, EXPERIENCE_CAP)
                .validate(GearRulesConfig::validSelector, "Choose an existing item or enter #namespace:item_tag")
                .validate(GearRulesConfig::validRepair, "Choose an existing repair item/tag for the selected repair mode")
                .validate(GearRulesConfig::validUpgrade, "Choose an existing upgrade target and valid ingredient tag, or leave both blank");

        for (GearRule rule : BondedRulesLoader.effectiveRules(
                BondedRulesLoader.visibleProfiles(),
                Bonded.CONFIG.defaultMaxExperienceForUnknownItems.get()
        )) {
            fieldset.entry(toBuiltinEntry(rule, rule.source().label()));
        }

        value = builder.fieldset("rules", fieldset.build())
                .comment("Effective gear rules. Built-in and compatibility rows are read-only; copied and added rows are saved here.")
                .tooltip("Configure Bonded gear types, experience, repair materials, and one-step upgrades.")
                .info(info -> info
                        .header("Restart required")
                        .inlineText("Changes save automatically and apply after restarting the game or dedicated server."))
                .restart(RestartRequirement.GAME)
                .sync(false)
                .remoteScreenView(this::remoteScreenValue, () -> this.remoteViewAvailable)
                .build();
    }

    /** Installs the server-authoritative read-only view without touching the local config file. */
    public void installRemoteView(Collection<ResolvedGearRule> rules) {
        ArrayList<FieldsetEntry> entries = new ArrayList<>();
        for (ResolvedGearRule rule : rules) {
            entries.add(toRemoteEntry(rule));
        }
        this.remoteView = FieldsetValue.of(this.value.get().schema(), entries);
        this.remoteViewAvailable = true;
    }

    /** Keeps integrated-server owners on their local authoritative editor. */
    public void clearRemoteView() {
        this.remoteViewAvailable = false;
        this.remoteView = null;
    }

    public List<GearRule> userRules() {
        GearRule.Source source = new GearRule.Source(GearRule.Kind.USER, "bonded:user", "User", 1);
        ArrayList<GearRule> rules = new ArrayList<>();
        for (FieldsetEntry entry : value.get().entries()) {
            if (!entry.editable()) {
                continue;
            }
            rules.add(new GearRule(
                    entry.identity(),
                    entry.value(SELECTOR).trim(),
                    entry.value(TYPE),
                    entry.value(EXPERIENCE_CAP),
                    entry.value(REPAIR_MODE),
                    normalized(entry.value(REPAIR)),
                    normalized(entry.value(UPGRADE_TO)),
                    entry.value(UPGRADE_INGREDIENT).map(GearRulesConfig::normalized).orElse(null),
                    entry.value(ENABLED),
                    source
            ));
        }
        return List.copyOf(rules);
    }

    private static FieldsetEntry toBuiltinEntry(GearRule rule, String source) {
        String type = rule.type() == null ? "inherit" : rule.type();
        int cap = rule.experienceCap() == null ? 1000 : rule.experienceCap();
        String repairMode = rule.repairMode() == null ? "inherit" : rule.repairMode();
        return FieldsetEntry.builtin(rule.identity(), source)
                .with(SELECTOR, rule.selector())
                .with(TYPE, type)
                .with(EXPERIENCE_CAP, cap)
                .with(REPAIR_MODE, repairMode)
                .with(REPAIR, rule.repair() == null ? "" : rule.repair())
                .with(UPGRADE_TO, rule.upgradeTo() == null ? "" : rule.upgradeTo())
                .with(UPGRADE_INGREDIENT, Optional.ofNullable(rule.upgradeIngredient()))
                .with(ENABLED, rule.enabled());
    }

    private static FieldsetEntry toRemoteEntry(ResolvedGearRule rule) {
        return FieldsetEntry.builtin("remote/" + rule.item(), rule.source().label())
                .with(SELECTOR, rule.item())
                .with(TYPE, rule.type())
                .with(EXPERIENCE_CAP, rule.experienceCap())
                .with(REPAIR_MODE, rule.repairMode().name().toLowerCase(Locale.ROOT))
                .with(REPAIR, rule.repair() == null ? "" : rule.repair())
                .with(UPGRADE_TO, rule.upgradeTo() == null ? "" : rule.upgradeTo())
                .with(UPGRADE_INGREDIENT, Optional.ofNullable(rule.upgradeIngredient()))
                .with(ENABLED, rule.enabled());
    }

    private FieldsetValue remoteScreenValue() {
        FieldsetValue current = this.remoteView;
        return current == null ? this.value.get() : current;
    }

    private static boolean validRepair(FieldsetEntry entry) {
        if (!entry.editable()) {
            return true;
        }
        String mode = entry.value(REPAIR_MODE).toLowerCase(Locale.ROOT);
        String repair = entry.value(REPAIR);
        return switch (mode) {
            case "item" -> !repair.startsWith("#") && validItem(repair);
            case "tag" -> validTag(repair);
            default -> true;
        };
    }

    private static boolean validUpgrade(FieldsetEntry entry) {
        if (!entry.editable()) {
            return true;
        }
        String target = entry.value(UPGRADE_TO);
        Optional<String> ingredient = entry.value(UPGRADE_INGREDIENT).filter(value -> !value.isBlank());
        if (target.isBlank() && ingredient.isEmpty()) {
            return true;
        }
        return validItem(target) && ingredient.filter(GearRulesConfig::validTag).isPresent();
    }

    private static boolean validItemOrTag(String value) {
        return value.startsWith("#") ? validTag(value) : validItem(value);
    }

    private static boolean validSelector(FieldsetEntry entry) {
        return !entry.editable() || validItemOrTag(entry.value(SELECTOR));
    }

    private static boolean validItem(String value) {
        Identifier id = Identifier.tryParse(stripHash(value));
        return id != null && BuiltInRegistries.ITEM.containsKey(id);
    }

    private static boolean validTag(String value) {
        Identifier id = Identifier.tryParse(stripHash(value));
        // Config screens can open from the title screen, before authoritative item tags exist.
        // The world-bound resolver performs existence and non-empty validation at restart.
        return id != null;
    }

    private static String normalized(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String stripHash(String value) {
        return value != null && value.startsWith("#") ? value.substring(1) : value;
    }
}
