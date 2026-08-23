package com.iamkaf.bonded.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BondedRulesProfilesTest {
    private static final String ROOT = "/data/bonded/gear_rules/";
    private static final Set<String> TYPES = Set.of(
            "inherit",
            "armor",
            "melee_weapon",
            "ranged_weapon",
            "mining_tool",
            "utility"
    );
    private static final Set<String> REPAIR_MODES = Set.of("inherit", "none", "item", "tag");

    @Test
    void shipsValidProfileDocumentsWithStableProvenance() {
        Map<String, JsonObject> profiles = profiles();
        assertFalse(profiles.isEmpty());

        Set<String> ruleIdentities = new HashSet<>();
        for (var profile : profiles.values()) {
            String id = requiredString(profile, "id");
            String label = requiredString(profile, "label");
            String kind = requiredString(profile, "kind");
            int version = profile.get("version").getAsInt();
            assertIdentifier(id);
            assertFalse(label.isBlank());
            assertTrue(version > 0);

            JsonArray markers = profile.has("markers") ? profile.getAsJsonArray("markers") : new JsonArray();
            if (kind.equals("builtin")) {
                assertFalse(profile.has("required_mod"));
            } else {
                assertEquals("compat", kind);
                assertFalse(requiredString(profile, "required_mod").isBlank());
                assertFalse(markers.isEmpty(), () -> "Missing activation marker: " + id);
            }
            markers.forEach(marker -> assertIdentifier(marker.getAsString()));

            JsonArray rules = profile.getAsJsonArray("rules");
            assertNotNull(rules, () -> "Missing rules: " + id);
            assertFalse(rules.isEmpty(), () -> "Empty profile: " + id);
            for (int index = 0; index < rules.size(); index++) {
                assertTrue(ruleIdentities.add(id + "/" + index));
                assertRuleShape(id + "/" + index, rules.get(index).getAsJsonObject());
            }
        }
    }

    @Test
    void keepsTheRequiredBuiltinAndCompatibilityContracts() {
        Map<String, JsonObject> profiles = profiles();
        assertTrue(profiles.keySet().containsAll(Set.of(
                "bonded:builtin",
                "bonded:basic_weapons",
                "bonded:advanced_netherite",
                "bonded:immersive_armors",
                "bonded:betterend",
                "bonded:betternether"
        )));

        var ironSword = rule(profiles.get("bonded:builtin"), "minecraft:iron_sword");
        assertEquals("melee_weapon", ironSword.get("type").getAsString());
        assertEquals(100, ironSword.get("experience_cap").getAsInt());
        assertEquals("minecraft:diamond_sword", ironSword.get("upgrade_to").getAsString());
        assertEquals("minecraft:diamond_tool_materials", ironSword.get("upgrade_ingredient").getAsString());

        var bow = rule(profiles.get("bonded:builtin"), "minecraft:bow");
        assertEquals(150, bow.get("experience_cap").getAsInt());
        assertEquals("item", bow.get("repair_mode").getAsString());
        assertEquals("minecraft:string", bow.get("repair").getAsString());

        var basicWeapons = rule(profiles.get("bonded:basic_weapons"), "#basicweapons:basic_weapon");
        assertEquals("melee_weapon", basicWeapons.get("type").getAsString());
        assertEquals("inherit", basicWeapons.get("repair_mode").getAsString());

        var advancedNetherite = rule(
                profiles.get("bonded:advanced_netherite"),
                "advancednetherite:netherite_iron_sword"
        );
        assertEquals(
                "advancednetherite:netherite_gold_sword",
                advancedNetherite.get("upgrade_to").getAsString()
        );
        assertEquals(
                "advancednetherite:upgrade_to_netherite_gold",
                advancedNetherite.get("upgrade_ingredient").getAsString()
        );

        var immersiveArmor = rule(profiles.get("bonded:immersive_armors"), "immersive_armors:bone_helmet");
        assertEquals("armor", immersiveArmor.get("type").getAsString());
        assertEquals("inherit", immersiveArmor.get("repair_mode").getAsString());
        assertFalse(immersiveArmor.has("repair"));

        JsonObject betterNether = profiles.get("bonded:betternether");
        for (JsonElement element : betterNether.getAsJsonArray("rules")) {
            String selector = element.getAsJsonObject().get("selector").getAsString();
            assertFalse(selector.endsWith("_hammer") || selector.endsWith("_hammer_diamond"));
            assertFalse(selector.endsWith("_excavator") || selector.endsWith("_excavator_diamond"));
        }
    }

    @Test
    void configRowsContainCompleteEffectiveCompatibilityRules() {
        List<GearRule> effective = BondedRulesLoader.effectiveRules(BondedRulesLoader.loadProfiles(), 1000);
        List<GearRule> netheriteHelmets = effective.stream()
                .filter(rule -> rule.selector().equals("minecraft:netherite_helmet"))
                .toList();

        assertEquals(1, netheriteHelmets.size());
        GearRule helmet = netheriteHelmets.getFirst();
        assertEquals("armor", helmet.type());
        assertEquals(3000, helmet.experienceCap());
        assertEquals("inherit", helmet.repairMode());
        assertEquals("advancednetherite:netherite_iron_helmet", helmet.upgradeTo());
        assertEquals("advancednetherite:upgrade_to_netherite_iron", helmet.upgradeIngredient());
        assertEquals("Advanced Netherite", helmet.source().label());
    }

    private static Map<String, JsonObject> profiles() {
        JsonObject index = read("index.json");
        JsonArray files = index.getAsJsonArray("profiles");
        assertNotNull(files);
        assertFalse(files.isEmpty());

        Map<String, JsonObject> profiles = new HashMap<>();
        Set<String> fileNames = new HashSet<>();
        for (JsonElement file : files) {
            String fileName = file.getAsString();
            assertTrue(fileNames.add(fileName), () -> "Duplicate profile file: " + fileName);
            JsonObject profile = read(fileName);
            String id = requiredString(profile, "id");
            assertNull(profiles.put(id, profile), () -> "Duplicate profile id: " + id);
        }
        return profiles;
    }

    private static JsonObject read(String file) {
        try (var stream = BondedRulesProfilesTest.class.getResourceAsStream(ROOT + file)) {
            assertNotNull(stream, () -> "Missing shipped gear-rule resource: " + file);
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (IOException exception) {
            throw new AssertionError("Could not read shipped gear-rule resource: " + file, exception);
        }
    }

    private static JsonObject rule(JsonObject profile, String selector) {
        assertNotNull(profile, () -> "Missing profile for selector " + selector);
        for (JsonElement element : profile.getAsJsonArray("rules")) {
            JsonObject rule = element.getAsJsonObject();
            if (selector.equals(rule.get("selector").getAsString())) {
                return rule;
            }
        }
        throw new AssertionError("Missing shipped rule for " + selector);
    }

    private static void assertRuleShape(String identity, JsonObject rule) {
        String selector = requiredString(rule, "selector");
        assertIdentifier(stripHash(selector));
        if (rule.has("type")) {
            String type = rule.get("type").getAsString();
            assertTrue(TYPES.contains(type), () -> "Unknown type in " + identity + ": " + type);
        }
        if (rule.has("experience_cap")) {
            assertTrue(rule.get("experience_cap").getAsInt() > 0, () -> "Non-positive cap in " + identity);
        }
        if (rule.has("repair_mode")) {
            String mode = rule.get("repair_mode").getAsString();
            assertTrue(REPAIR_MODES.contains(mode), () -> "Unknown repair mode in " + identity + ": " + mode);
            if (mode.equals("item") || mode.equals("tag")) {
                assertIdentifier(stripHash(requiredString(rule, "repair")));
            }
        }

        boolean hasUpgrade = hasText(rule, "upgrade_to");
        boolean hasIngredient = hasText(rule, "upgrade_ingredient");
        assertEquals(hasUpgrade, hasIngredient, () -> "Incomplete upgrade in " + identity);
        if (hasUpgrade) {
            assertIdentifier(rule.get("upgrade_to").getAsString());
            assertIdentifier(stripHash(rule.get("upgrade_ingredient").getAsString()));
        }
    }

    private static boolean hasText(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() && !object.get(key).getAsString().isBlank();
    }

    private static String requiredString(JsonObject object, String key) {
        assertTrue(hasText(object, key), () -> "Missing field: " + key);
        return object.get(key).getAsString();
    }

    private static String stripHash(String value) {
        return value.startsWith("#") ? value.substring(1) : value;
    }

    private static void assertIdentifier(String value) {
        assertNotNull(value);
        assertTrue(
                value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"),
                () -> "Invalid identifier: " + value
        );
    }
}
