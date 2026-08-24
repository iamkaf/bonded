package com.iamkaf.bonded.rules;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.leveling.GearManager;
import com.iamkaf.konfig.api.v1.fieldset.FieldsetEntry;
import com.iamkaf.konfig.api.v1.fieldset.FieldsetValue;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Repairable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Resolves declarations into the immutable item rules used during one game run. */
public final class BondedRules {
    private static final Gson GSON = new Gson();
    private static final Type PACKET_TYPE = new TypeToken<List<ResolvedGearRule>>() { }.getType();
    private static final GearRule.Source DEFAULT_SOURCE =
            new GearRule.Source(GearRule.Kind.BUILTIN, "bonded:default", "Bonded", 1);
    private static final GearRule.Source API_SOURCE =
            new GearRule.Source(GearRule.Kind.API, "bonded:api", "Mod API", 1);
    private static final List<ApiPatch> API_PATCHES = new ArrayList<>();
    private static volatile Snapshot active = Snapshot.empty();
    private static volatile Snapshot client = Snapshot.empty();
    private static volatile Registry<Item> activeRegistry;

    private BondedRules() {
    }

    public static synchronized Snapshot resolve(Registry<Item> registry) {
        activeRegistry = registry;
        Snapshot next = resolveSnapshot(registry, Bonded.GEAR_RULES_CONFIG.userRules());
        active = next;
        // Integrated servers share this classloader with their client. Keep that view current
        // until the identical login packet arrives, including when opening a second world.
        client = next;
        if (!next.diagnostics().isEmpty()) {
            next.diagnostics().forEach(message -> Bonded.LOGGER.warn("Gear rule: {}", message));
        }
        Bonded.LOGGER.info("Resolved {} concrete Bonded gear rules with {} diagnostic(s)",
                next.rules().size(), next.diagnostics().size());
        return next;
    }

    /** Rejects candidates whose user declarations cannot be resolved in the active world. */
    public static synchronized boolean validCandidate(FieldsetValue candidate) {
        if (!candidate.validate().valid()) {
            return false;
        }
        Registry<Item> registry = activeRegistry;
        if (registry == null) {
            return true;
        }
        return validUserRules(registry, GearRulesConfig.userRules(candidate));
    }

    static boolean validUserRules(Registry<Item> registry, List<GearRule> userRules) {
        LinkedHashMap<String, Item> items = registryItems(registry);
        ArrayList<String> diagnostics = new ArrayList<>();
        applySource(registry, items, new LinkedHashMap<>(), userRules, true, true, diagnostics);
        return diagnostics.isEmpty();
    }

    static Optional<String> dormantReason(FieldsetEntry entry) {
        return dormantReason(entry, false);
    }

    static Optional<String> dormantReason(FieldsetEntry entry, boolean remoteRegistryAvailable) {
        if (!entry.editable()) {
            return Optional.empty();
        }
        GearRule rule = GearRulesConfig.userRule(entry);
        Registry<Item> registry = activeRegistry;
        if (registry == null && !remoteRegistryAvailable) {
            return dormantItemReason(
                    rule,
                    id -> {
                        Identifier parsed = Identifier.tryParse(id);
                        return parsed != null && BuiltInRegistries.ITEM.containsKey(parsed);
                    }
            );
        }
        Registry<Item> warningRegistry = registry == null ? BuiltInRegistries.ITEM : registry;
        RuleIssue issue = validate(
                warningRegistry,
                id -> {
                    Identifier parsed = Identifier.tryParse(id);
                    return parsed != null && warningRegistry.containsKey(parsed);
                },
                rule,
                true
        );
        return issue != null && issue.kind() == RuleIssueKind.DORMANT
                ? Optional.of(issue.message())
                : Optional.empty();
    }

    private static Optional<String> dormantItemReason(GearRule rule, Predicate<String> itemPresent) {
        if (!rule.selector().startsWith("#")
                && GearRuleReference.itemMatching(rule.selector(), itemPresent)
                == GearRuleReference.Availability.DORMANT) {
            return Optional.of("selector item is missing: " + rule.selector());
        }
        if ("item".equals(rule.repairMode()) && rule.repair() != null
                && GearRuleReference.itemMatching(rule.repair(), itemPresent)
                == GearRuleReference.Availability.DORMANT) {
            return Optional.of("repair item is missing: " + rule.repair());
        }
        if (rule.upgradeTo() != null
                && GearRuleReference.itemMatching(rule.upgradeTo(), itemPresent)
                == GearRuleReference.Availability.DORMANT) {
            return Optional.of("upgrade target is missing: " + rule.upgradeTo());
        }
        return Optional.empty();
    }

    public static synchronized void clearServerState() {
        activeRegistry = null;
        active = Snapshot.empty();
        client = Snapshot.empty();
    }

    private static Snapshot resolveSnapshot(Registry<Item> registry, List<GearRule> userRules) {
        ArrayList<String> diagnostics = new ArrayList<>();
        LinkedHashMap<String, Item> items = registryItems(registry);
        EnumMap<GearRule.Kind, List<GearRule>> declarations = new EnumMap<>(GearRule.Kind.class);
        for (GearRule.Kind kind : GearRule.Kind.values()) {
            declarations.put(kind, new ArrayList<>());
        }

        for (BondedRulesLoader.Profile profile : BondedRulesLoader.loadProfiles()) {
            if (!profile.modPresent() || !markersPresent(profile, items.keySet())) {
                continue;
            }
            declarations.get(profile.kind()).addAll(profile.rules());
        }
        declarations.get(GearRule.Kind.USER).addAll(userRules);

        LinkedHashMap<String, ResolvedGearRule> resolved = new LinkedHashMap<>();
        applySource(registry, items, resolved, declarations.get(GearRule.Kind.BUILTIN), false, diagnostics);
        applySource(registry, items, resolved, declarations.get(GearRule.Kind.COMPAT), false, diagnostics);
        applyApiPatches(registry, items, resolved, diagnostics);
        applySource(registry, items, resolved, declarations.get(GearRule.Kind.USER), true, diagnostics);

        return new Snapshot(resolved, diagnostics);
    }

    public static Snapshot active() {
        return active;
    }

    public static Snapshot view() {
        // Dedicated and integrated servers own the active snapshot. A remote client has no
        // active server snapshot and reads the last snapshot received over the network.
        return active.rules().isEmpty() ? client : active;
    }

    public static Snapshot installClientSnapshot(String json) {
        List<ResolvedGearRule> rules = GSON.fromJson(json, PACKET_TYPE);
        LinkedHashMap<String, ResolvedGearRule> byItem = new LinkedHashMap<>();
        if (rules != null) {
            for (ResolvedGearRule rule : rules) {
                byItem.put(rule.item(), rule);
            }
        }
        Snapshot snapshot = new Snapshot(byItem, List.of());
        client = snapshot;
        return snapshot;
    }

    public static String packetJson() {
        return GSON.toJson(new ArrayList<>(active.rules().values()), PACKET_TYPE);
    }

    public static @Nullable ResolvedGearRule rule(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return id == null ? null : view().rules().get(id.toString());
    }

    public static boolean isDisabled(Item item) {
        ResolvedGearRule rule = rule(item);
        return rule != null && !rule.enabled();
    }

    public static int experienceCap(Item item) {
        ResolvedGearRule rule = rule(item);
        return rule == null || !rule.enabled()
                ? Bonded.CONFIG.defaultMaxExperienceForUnknownItems.get()
                : rule.experienceCap();
    }

    public static @Nullable Item upgrade(Item item) {
        ResolvedGearRule rule = rule(item);
        if (rule == null || !rule.enabled() || rule.upgradeTo() == null) {
            return null;
        }
        Identifier id = Identifier.tryParse(rule.upgradeTo());
        return id == null ? null : BuiltInRegistries.ITEM.getValue(id);
    }

    public static @Nullable TagKey<Item> upgradeIngredient(Item item) {
        ResolvedGearRule rule = rule(item);
        if (rule == null || !rule.enabled() || rule.upgradeIngredient() == null) {
            return null;
        }
        Identifier id = Identifier.tryParse(stripHash(rule.upgradeIngredient()));
        return id == null ? null : TagKey.create(Registries.ITEM, id);
    }

    /** Uses native repair data unless the active rule explicitly replaces or disables it. */
    public static @Nullable Repairable repairable(ItemStack stack) {
        ResolvedGearRule rule = rule(stack.getItem());
        if (rule == null || !rule.enabled() || rule.repairMode() == ResolvedGearRule.RepairMode.INHERIT) {
            return stack.get(net.minecraft.core.component.DataComponents.REPAIRABLE);
        }
        if (rule.repairMode() == ResolvedGearRule.RepairMode.NONE || rule.repair() == null) {
            return null;
        }
        Identifier id = Identifier.tryParse(stripHash(rule.repair()));
        if (id == null) {
            return null;
        }
        if (rule.repairMode() == ResolvedGearRule.RepairMode.ITEM) {
            Item item = BuiltInRegistries.ITEM.getValue(id);
            return item == null ? null : new Repairable(HolderSet.direct(item.builtInRegistryHolder()));
        }
        Optional<HolderSet.Named<Item>> holders = BuiltInRegistries.ITEM.get(TagKey.create(Registries.ITEM, id));
        return holders.<Repairable>map(Repairable::new).orElse(null);
    }

    public static synchronized void addApiUpgrade(Item from, Item to, TagKey<Item> ingredient) {
        addApiUpgrade(() -> from, () -> to, ingredient);
    }

    public static synchronized void addApiUpgrade(
            Supplier<Item> from,
            Supplier<Item> to,
            TagKey<Item> ingredient
    ) {
        ApiPatch patch = new ApiPatch(from);
        patch.upgradeTo = to;
        patch.upgradeIngredient = ingredient;
        API_PATCHES.add(patch);
    }

    public static synchronized void addApiRepair(Item from, Item material) {
        ApiPatch patch = new ApiPatch(() -> from);
        patch.repair = () -> material;
        API_PATCHES.add(patch);
    }

    public static synchronized void addApiExperienceCap(Item item, int cap) {
        addApiExperienceCap(() -> item, cap);
    }

    public static synchronized void addApiExperienceCap(Supplier<Item> item, int cap) {
        if (cap <= 0) {
            throw new IllegalArgumentException("Bonded experience caps must be positive");
        }
        ApiPatch patch = new ApiPatch(item);
        patch.experienceCap = cap;
        API_PATCHES.add(patch);
    }

    private static void applySource(
            Registry<Item> registry,
            Map<String, Item> items,
            Map<String, ResolvedGearRule> resolved,
            List<GearRule> declarations,
            boolean fullReplacement,
            List<String> diagnostics
    ) {
        applySource(registry, items, resolved, declarations, fullReplacement, false, diagnostics);
    }

    private static void applySource(
            Registry<Item> registry,
            Map<String, Item> items,
            Map<String, ResolvedGearRule> resolved,
            List<GearRule> declarations,
            boolean fullReplacement,
            boolean tolerateDormant,
            List<String> diagnostics
    ) {
        ArrayList<GearRule> valid = new ArrayList<>();
        for (GearRule declaration : declarations) {
            RuleIssue issue = validate(registry, items::containsKey, declaration, fullReplacement);
            if (issue == null) {
                valid.add(declaration);
            } else if (!tolerateDormant || issue.kind() != RuleIssueKind.DORMANT) {
                diagnostics.add(declaration.identity() + ": " + issue.message());
            }
        }

        for (Map.Entry<String, Item> itemEntry : items.entrySet()) {
            ArrayList<GearRule> exact = new ArrayList<>();
            ArrayList<GearRule> tagged = new ArrayList<>();
            for (GearRule declaration : valid) {
                if (declaration.selector().startsWith("#")) {
                    if (matchesTag(itemEntry.getValue(), declaration.selector())) {
                        tagged.add(declaration);
                    }
                } else if (declaration.selector().equals(itemEntry.getKey())) {
                    exact.add(declaration);
                }
            }

            List<GearRule> candidates = exact.isEmpty() ? tagged : exact;
            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() > 1) {
                diagnostics.add("Conflicting " + candidates.get(0).source().kind().name().toLowerCase(Locale.ROOT)
                        + " rules for " + itemEntry.getKey() + ": "
                        + candidates.stream().map(GearRule::identity).toList());
                continue;
            }
            GearRule selected = candidates.get(0);
            if (selected.type() != null && !selected.type().equals("inherit")
                    && GearManager.gearTypeLevelerRegistry.gearTypeLevelers().stream()
                    .filter(leveler -> leveler.id().equals(selected.type()))
                    .noneMatch(leveler -> leveler.supports(itemEntry.getValue().getDefaultInstance()))) {
                diagnostics.add(selected.identity() + ": type " + selected.type()
                        + " does not support " + itemEntry.getKey());
                continue;
            }
            ResolvedGearRule lower = resolved.get(itemEntry.getKey());
            resolved.put(itemEntry.getKey(), apply(itemEntry.getKey(), lower, selected, fullReplacement));
        }
    }

    private static ResolvedGearRule apply(
            String item,
            @Nullable ResolvedGearRule lower,
            GearRule declaration,
            boolean fullReplacement
    ) {
        ResolvedGearRule base = lower == null || fullReplacement
                ? defaults(item, declaration.source())
                : lower;
        if (!declaration.enabled()) {
            return new ResolvedGearRule(
                    item,
                    declaration.type() == null ? base.type() : declaration.type(),
                    declaration.experienceCap() == null ? base.experienceCap() : declaration.experienceCap(),
                    declaration.repairMode() == null ? base.repairMode() : ResolvedGearRule.RepairMode.parse(declaration.repairMode()),
                    declaration.repair(),
                    null,
                    null,
                    false,
                    declaration.source()
            );
        }
        boolean upgradeDeclared = declaration.upgradeTo() != null || declaration.upgradeIngredient() != null;
        return new ResolvedGearRule(
                item,
                declaration.type() == null ? base.type() : declaration.type(),
                declaration.experienceCap() == null ? base.experienceCap() : declaration.experienceCap(),
                declaration.repairMode() == null
                        ? base.repairMode()
                        : ResolvedGearRule.RepairMode.parse(declaration.repairMode()),
                declaration.repairMode() == null ? base.repair() : declaration.repair(),
                upgradeDeclared || fullReplacement ? declaration.upgradeTo() : base.upgradeTo(),
                upgradeDeclared || fullReplacement ? declaration.upgradeIngredient() : base.upgradeIngredient(),
                true,
                declaration.source()
        );
    }

    private static ResolvedGearRule defaults(String item, GearRule.Source source) {
        return new ResolvedGearRule(
                item,
                "inherit",
                Bonded.CONFIG.defaultMaxExperienceForUnknownItems.get(),
                ResolvedGearRule.RepairMode.INHERIT,
                null,
                null,
                null,
                true,
                source == null ? DEFAULT_SOURCE : source
        );
    }

    private static void applyApiPatches(
            Registry<Item> registry,
            Map<String, Item> items,
            Map<String, ResolvedGearRule> resolved,
            List<String> diagnostics
    ) {
        for (ApiPatch patch : API_PATCHES) {
            Item item;
            try {
                item = patch.item.get();
            } catch (RuntimeException exception) {
                diagnostics.add("API rule item supplier failed: " + exception.getMessage());
                continue;
            }
            String itemId = registryId(registry, item);
            if (itemId == null || !items.containsKey(itemId)) {
                diagnostics.add("API rule references an unregistered item");
                continue;
            }
            ResolvedGearRule base = resolved.getOrDefault(itemId, defaults(itemId, API_SOURCE));
            String upgradeTo = base.upgradeTo();
            String upgradeIngredient = base.upgradeIngredient();
            if (patch.upgradeTo != null && patch.upgradeIngredient != null) {
                Item targetItem;
                try {
                    targetItem = patch.upgradeTo.get();
                } catch (RuntimeException exception) {
                    diagnostics.add("API upgrade supplier failed for " + itemId + ": " + exception.getMessage());
                    continue;
                }
                String target = registryId(registry, targetItem);
                Optional<HolderSet.Named<Item>> holders = registry.get(patch.upgradeIngredient);
                if (target == null || !items.containsKey(target) || holders.isEmpty() || holders.get().size() == 0) {
                    diagnostics.add("Invalid API upgrade rule for " + itemId);
                } else {
                    upgradeTo = target;
                    upgradeIngredient = patch.upgradeIngredient.location().toString();
                }
            }
            ResolvedGearRule.RepairMode repairMode = base.repairMode();
            String repair = base.repair();
            if (patch.repair != null) {
                Item repairItem;
                try {
                    repairItem = patch.repair.get();
                } catch (RuntimeException exception) {
                    diagnostics.add("API repair supplier failed for " + itemId + ": " + exception.getMessage());
                    continue;
                }
                String repairId = registryId(registry, repairItem);
                if (repairId == null || !items.containsKey(repairId)) {
                    diagnostics.add("Invalid API repair rule for " + itemId);
                    continue;
                }
                repairMode = ResolvedGearRule.RepairMode.ITEM;
                repair = repairId;
            }
            resolved.put(itemId, new ResolvedGearRule(
                    itemId,
                    base.type(),
                    patch.experienceCap == null ? base.experienceCap() : patch.experienceCap,
                    repairMode,
                    repair,
                    upgradeTo,
                    upgradeIngredient,
                    true,
                    API_SOURCE
            ));
        }
    }

    private static @Nullable RuleIssue validate(
            Registry<Item> registry,
            Predicate<String> itemPresent,
            GearRule rule,
            boolean fullReplacement
    ) {
        if (rule.selector() == null || rule.selector().isBlank()) {
            return invalid("selector is blank");
        }
        Identifier selector = Identifier.tryParse(stripHash(rule.selector()));
        if (selector == null) {
            return invalid("selector is not a valid identifier: " + rule.selector());
        }
        if (rule.selector().startsWith("#")) {
            Optional<HolderSet.Named<Item>> holders = registry.get(TagKey.create(Registries.ITEM, selector));
            if (holders.isEmpty() || holders.get().size() == 0) {
                return dormant("selector tag is missing or empty: " + rule.selector());
            }
        } else {
            GearRuleReference.Availability availability = GearRuleReference.itemMatching(rule.selector(), itemPresent);
            if (availability == GearRuleReference.Availability.INVALID) {
                return invalid("selector is not a valid identifier: " + rule.selector());
            }
            if (availability == GearRuleReference.Availability.DORMANT) {
                return dormant("selector item is missing: " + rule.selector());
            }
        }
        if (rule.experienceCap() != null && rule.experienceCap() <= 0) {
            return invalid("experience cap must be positive");
        }
        if (fullReplacement && (rule.type() == null || rule.experienceCap() == null || rule.repairMode() == null)) {
            return invalid("user replacements must specify type, experience cap, and repair mode");
        }
        if (rule.type() != null && !Set.of(
                "inherit", "armor", "melee_weapon", "ranged_weapon", "mining_tool", "utility"
        ).contains(rule.type())) {
            return invalid("unknown gear type: " + rule.type());
        }
        if (rule.repairMode() != null) {
            ResolvedGearRule.RepairMode mode;
            try {
                mode = ResolvedGearRule.RepairMode.parse(rule.repairMode());
            } catch (IllegalArgumentException exception) {
                return invalid("unknown repair mode: " + rule.repairMode());
            }
            if ((mode == ResolvedGearRule.RepairMode.ITEM || mode == ResolvedGearRule.RepairMode.TAG)
                    && (rule.repair() == null || rule.repair().isBlank())) {
                return invalid("repair selector is required for " + rule.repairMode());
            }
            if (mode == ResolvedGearRule.RepairMode.ITEM && rule.repair() != null) {
                GearRuleReference.Availability availability = GearRuleReference.itemMatching(rule.repair(), itemPresent);
                if (availability == GearRuleReference.Availability.INVALID) {
                    return invalid("repair item is not a valid identifier: " + rule.repair());
                }
                if (availability == GearRuleReference.Availability.DORMANT) {
                    return dormant("repair item is missing: " + rule.repair());
                }
            }
            if (mode == ResolvedGearRule.RepairMode.TAG && rule.repair() != null) {
                Identifier repair = Identifier.tryParse(stripHash(rule.repair()));
                if (repair == null) {
                    return invalid("repair tag is not a valid identifier: " + rule.repair());
                }
                Optional<HolderSet.Named<Item>> holders = registry.get(TagKey.create(Registries.ITEM, repair));
                if (holders.isEmpty() || holders.get().size() == 0) {
                    return dormant("repair tag is missing or empty: " + rule.repair());
                }
            }
        }
        boolean target = rule.upgradeTo() != null && !rule.upgradeTo().isBlank();
        boolean ingredient = rule.upgradeIngredient() != null && !rule.upgradeIngredient().isBlank();
        if (target != ingredient) {
            return invalid("upgrade target and ingredient must both be set or both be blank");
        }
        if (target) {
            GearRuleReference.Availability targetAvailability =
                    GearRuleReference.itemMatching(rule.upgradeTo(), itemPresent);
            Identifier ingredientId = Identifier.tryParse(stripHash(rule.upgradeIngredient()));
            if (targetAvailability == GearRuleReference.Availability.INVALID) {
                return invalid("upgrade target is not a valid identifier: " + rule.upgradeTo());
            }
            if (ingredientId == null) {
                return invalid("upgrade ingredient is not a valid identifier: " + rule.upgradeIngredient());
            }
            Optional<HolderSet.Named<Item>> holders = registry.get(TagKey.create(Registries.ITEM, ingredientId));
            if (targetAvailability == GearRuleReference.Availability.DORMANT) {
                return dormant("upgrade target is missing: " + rule.upgradeTo());
            }
            if (holders.isEmpty() || holders.get().size() == 0) {
                return dormant("upgrade ingredient tag is missing or empty: " + rule.upgradeIngredient());
            }
        }
        return null;
    }

    private static RuleIssue invalid(String message) {
        return new RuleIssue(RuleIssueKind.INVALID, message);
    }

    private static RuleIssue dormant(String message) {
        return new RuleIssue(RuleIssueKind.DORMANT, message);
    }

    private static LinkedHashMap<String, Item> registryItems(Registry<Item> registry) {
        LinkedHashMap<String, Item> items = new LinkedHashMap<>();
        for (Item item : registry) {
            Identifier id = registry.getKey(item);
            if (id != null) {
                items.put(id.toString(), item);
            }
        }
        return items;
    }

    private static boolean markersPresent(BondedRulesLoader.Profile profile, Set<String> items) {
        return profile.markers().stream().allMatch(items::contains);
    }

    private static boolean matchesTag(Item item, String selector) {
        Identifier id = Identifier.tryParse(stripHash(selector));
        return id != null && item.builtInRegistryHolder().is(TagKey.create(Registries.ITEM, id));
    }

    private static String stripHash(String value) {
        return value != null && value.startsWith("#") ? value.substring(1) : value;
    }

    private static @Nullable String registryId(Registry<Item> registry, @Nullable Item item) {
        Identifier id = item == null ? null : registry.getKey(item);
        return id == null ? null : id.toString();
    }

    public record Snapshot(Map<String, ResolvedGearRule> rules, List<String> diagnostics) {
        public Snapshot(Map<String, ResolvedGearRule> rules, List<String> diagnostics) {
            this.rules = Collections.unmodifiableMap(new LinkedHashMap<>(rules));
            this.diagnostics = Collections.unmodifiableList(new ArrayList<>(diagnostics));
        }

        public static Snapshot empty() {
            return new Snapshot(Map.of(), List.of());
        }
    }

    private enum RuleIssueKind {
        INVALID,
        DORMANT
    }

    private record RuleIssue(RuleIssueKind kind, String message) {
    }

    private static final class ApiPatch {
        private final Supplier<Item> item;
        private Integer experienceCap;
        private Supplier<Item> repair;
        private Supplier<Item> upgradeTo;
        private TagKey<Item> upgradeIngredient;

        private ApiPatch(Supplier<Item> item) {
            this.item = item;
        }
    }
}
