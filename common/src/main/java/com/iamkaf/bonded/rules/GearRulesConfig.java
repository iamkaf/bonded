package com.iamkaf.bonded.rules;

import com.iamkaf.konfig.api.v1.ConfigBuilder;
import com.iamkaf.konfig.api.v1.ConfigValue;
import com.iamkaf.konfig.api.v1.RestartRequirement;
import com.iamkaf.konfig.api.v1.fieldset.FieldsetBuilder;
import com.iamkaf.konfig.api.v1.fieldset.FieldsetCatalog;
import com.iamkaf.konfig.api.v1.fieldset.FieldsetEntry;
import com.iamkaf.konfig.api.v1.fieldset.FieldsetField;
import com.iamkaf.konfig.api.v1.fieldset.FieldsetValue;
import com.iamkaf.bonded.Bonded;
import net.minecraft.core.registries.Registries;

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
        FieldsetBuilder fieldset = fieldsetBuilder();
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
                        .header("Applied live")
                        .inlineText("Valid changes save automatically and apply to the running server."))
                .restart(RestartRequirement.NONE)
                .sync(true)
                .remoteScreenView(this::remoteScreenValue, () -> this.remoteViewAvailable)
                .validate(BondedRules::validCandidate, "One or more gear rules cannot be resolved")
                .build();
    }

    private FieldsetBuilder fieldsetBuilder() {
        FieldsetCatalog catalog = FieldsetCatalog.create()
                .editableProfile("User Overrides")
                .newEntryLabel("New Override")
                .overrideLabel("Override")
                .duplicateLabel("Duplicate")
                .deleteLabel("Delete")
                .filter(TYPE)
                .section("Rule", SELECTOR, ENABLED)
                .section("Progression", TYPE, EXPERIENCE_CAP)
                .section("Repair", REPAIR_MODE, REPAIR)
                .section("Upgrade", UPGRADE_TO, UPGRADE_INGREDIENT)
                .warning(entry -> BondedRules.dormantReason(entry, this.remoteViewAvailable))
                .build();
        return FieldsetBuilder.create()
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
                .catalog(catalog)
                .validate(GearRulesConfig::validSelector, "Choose an existing item or enter #namespace:item_tag")
                .validate(GearRulesConfig::validRepair, "Choose an existing repair item/tag for the selected repair mode")
                .validate(GearRulesConfig::validUpgrade, "Choose an existing upgrade target and valid ingredient tag, or leave both blank");
    }

    /** Installs the server-authoritative resolved rows without touching the persisted user rules. */
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
        return userRules(value.local());
    }

    static List<GearRule> userRules(FieldsetValue candidate) {
        GearRule.Source source = new GearRule.Source(GearRule.Kind.USER, "bonded:user", "User", 1);
        ArrayList<GearRule> rules = new ArrayList<>();
        for (FieldsetEntry entry : candidate.entries()) {
            if (!entry.editable()) {
                continue;
            }
            rules.add(userRule(entry, source));
        }
        return List.copyOf(rules);
    }

    static GearRule userRule(FieldsetEntry entry) {
        return userRule(entry, new GearRule.Source(GearRule.Kind.USER, "bonded:user", "User", 1));
    }

    private static GearRule userRule(FieldsetEntry entry, GearRule.Source source) {
        return new GearRule(
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
        );
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
        return current == null ? this.value.get() : mergeRemoteView(current, this.value.get());
    }

    private static FieldsetValue mergeRemoteView(FieldsetValue remote, FieldsetValue configured) {
        if (remote.schema() != configured.schema()) {
            throw new IllegalArgumentException("Remote gear rules use a different schema");
        }
        ArrayList<FieldsetEntry> entries = new ArrayList<>(remote.entries());
        for (FieldsetEntry entry : configured.entries()) {
            if (entry.editable()) {
                entries.add(entry);
            }
        }
        return FieldsetValue.of(remote.schema(), entries);
    }

    private static boolean validRepair(FieldsetEntry entry) {
        if (!entry.editable()) {
            return true;
        }
        String mode = entry.value(REPAIR_MODE).toLowerCase(Locale.ROOT);
        String repair = entry.value(REPAIR);
        return switch (mode) {
            case "item" -> !repair.startsWith("#") && GearRuleReference.validPersistedItem(repair);
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
        return GearRuleReference.validPersistedItem(target)
                && ingredient.filter(GearRulesConfig::validTag).isPresent();
    }

    private static boolean validItemOrTag(String value) {
        return value.startsWith("#") ? validTag(value) : GearRuleReference.validPersistedItem(value);
    }

    private static boolean validSelector(FieldsetEntry entry) {
        return !entry.editable() || validItemOrTag(entry.value(SELECTOR));
    }

    private static boolean validTag(String value) {
        // Config screens can open from the title screen, before authoritative item tags exist.
        // The world-bound resolver performs existence and non-empty validation at restart.
        return GearRuleReference.validIdentifier(stripHash(value));
    }

    private static String normalized(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String stripHash(String value) {
        return value != null && value.startsWith("#") ? value.substring(1) : value;
    }
}
