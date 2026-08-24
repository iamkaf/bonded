package com.iamkaf.bonded.rules;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.iamkaf.amber.api.platform.v1.Platform;
import com.iamkaf.bonded.Bonded;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Loads the rule declarations shipped in Bonded's jar. */
public final class BondedRulesLoader {
    private static final Gson GSON = new Gson();
    private static final String ROOT = "/data/bonded/gear_rules/";

    private BondedRulesLoader() {
    }

    public static List<Profile> loadProfiles() {
        Index index = read("index.json", Index.class);
        if (index.profiles == null) {
            throw new IllegalStateException("Bonded gear rule index has no profiles");
        }

        ArrayList<Profile> profiles = new ArrayList<>();
        for (String file : index.profiles) {
            ProfileDocument document = read(file, ProfileDocument.class);
            profiles.add(document.toProfile());
        }
        return Collections.unmodifiableList(profiles);
    }

    public static List<Profile> visibleProfiles() {
        return loadProfiles().stream().filter(Profile::modPresent).toList();
    }

    /** Completes profile patches into the one-row-per-selector view used by the config screen. */
    static List<GearRule> effectiveRules(List<Profile> profiles, int defaultCap) {
        Map<String, GearRule> effective = new LinkedHashMap<>();
        for (Profile profile : profiles) {
            for (GearRule declaration : profile.rules()) {
                GearRule lower = effective.get(declaration.selector());
                String type = declaration.type() != null
                        ? declaration.type()
                        : lower == null ? "inherit" : lower.type();
                int cap = declaration.experienceCap() != null
                        ? declaration.experienceCap()
                        : lower == null ? defaultCap : lower.experienceCap();
                String repairMode = declaration.repairMode() != null
                        ? declaration.repairMode()
                        : lower == null ? "inherit" : lower.repairMode();
                String repair = declaration.repairMode() != null
                        ? declaration.repair()
                        : lower == null ? null : lower.repair();
                boolean upgradeDeclared = declaration.upgradeTo() != null
                        || declaration.upgradeIngredient() != null;
                String upgradeTo = upgradeDeclared
                        ? declaration.upgradeTo()
                        : lower == null ? null : lower.upgradeTo();
                String upgradeIngredient = upgradeDeclared
                        ? declaration.upgradeIngredient()
                        : lower == null ? null : lower.upgradeIngredient();
                if (!declaration.enabled()) {
                    upgradeTo = null;
                    upgradeIngredient = null;
                }
                effective.put(declaration.selector(), new GearRule(
                        "effective/" + declaration.selector(),
                        declaration.selector(),
                        type,
                        cap,
                        repairMode,
                        repair,
                        upgradeTo,
                        upgradeIngredient,
                        declaration.enabled(),
                        declaration.source()
                ));
            }
        }
        return List.copyOf(effective.values());
    }

    private static <T> T read(String file, Class<T> type) {
        try (InputStream stream = BondedRulesLoader.class.getResourceAsStream(ROOT + file)) {
            if (stream == null) {
                throw new IllegalStateException("Missing Bonded gear rule resource: " + file);
            }
            T value = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), type);
            if (value == null) {
                throw new IllegalStateException("Empty Bonded gear rule resource: " + file);
            }
            return value;
        } catch (IOException | JsonParseException exception) {
            throw new IllegalStateException("Cannot read Bonded gear rule resource: " + file, exception);
        }
    }

    public record Profile(
            String id,
            String label,
            GearRule.Kind kind,
            int version,
            @Nullable String requiredMod,
            List<String> markers,
            List<GearRule> rules
    ) {
        public boolean modPresent() {
            return requiredMod == null || requiredMod.isBlank() || Platform.isModLoaded(requiredMod);
        }
    }

    private static final class Index {
        private List<String> profiles;
    }

    private static final class ProfileDocument {
        private String id;
        private String label;
        private String kind;
        private int version = 1;
        private String required_mod;
        private List<String> markers = List.of();
        private List<RuleDocument> rules = List.of();

        private Profile toProfile() {
            if (id == null || label == null || kind == null) {
                throw new IllegalStateException("Gear rule profile metadata must include id, label, and kind");
            }
            GearRule.Kind sourceKind;
            try {
                sourceKind = GearRule.Kind.valueOf(kind.toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("Unknown gear rule profile kind: " + kind, exception);
            }
            GearRule.Source source = new GearRule.Source(sourceKind, id, label, version);
            ArrayList<GearRule> declarations = new ArrayList<>();
            for (int index = 0; index < rules.size(); index++) {
                declarations.add(rules.get(index).toRule(id + "/" + index, source));
            }
            return new Profile(
                    id,
                    label,
                    sourceKind,
                    version,
                    required_mod,
                    markers == null ? List.of() : List.copyOf(markers),
                    Collections.unmodifiableList(declarations)
            );
        }
    }

    private static final class RuleDocument {
        private String selector;
        private String type;
        private Integer experience_cap;
        private String repair_mode;
        private String repair;
        private String upgrade_to;
        private String upgrade_ingredient;
        private Boolean enabled;

        private GearRule toRule(String identity, GearRule.Source source) {
            return new GearRule(
                    identity,
                    selector,
                    type,
                    experience_cap,
                    repair_mode,
                    repair,
                    upgrade_to,
                    upgrade_ingredient,
                    enabled == null || enabled,
                    source
            );
        }
    }
}
