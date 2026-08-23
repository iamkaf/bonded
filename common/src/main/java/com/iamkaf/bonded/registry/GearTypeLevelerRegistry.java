package com.iamkaf.bonded.registry;

import com.google.common.collect.ImmutableSet;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import com.iamkaf.bonded.rules.BondedRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GearTypeLevelerRegistry {
    public final Map<Identifier, GearTypeLeveler> map = new HashMap<>();
    public final Set<GearTypeLeveler> levelers = new HashSet<>();

    public @Nullable GearTypeLeveler get(ItemStack gear) {
        if (gear.isEmpty()) {
            return null;
        }

        var rule = BondedRules.rule(gear.getItem());
        if (rule != null && !rule.enabled()) {
            return null;
        }
        if (rule != null && !rule.type().equals("inherit")) {
            return levelers.stream()
                    .filter(candidate -> candidate.id().equals(rule.type()))
                    .filter(candidate -> candidate.supports(gear))
                    .findFirst()
                    .orElse(null);
        }

        GearTypeLeveler leveler = map.get(BuiltInRegistries.ITEM.getKey(gear.getItem()));
        if (leveler != null) {
            return leveler.supports(gear) ? leveler : null;
        }

        return levelers.stream()
                .filter(candidate -> gear.getItem().builtInRegistryHolder().is(candidate.tag()))
                .filter(candidate -> candidate.supports(gear))
                .findFirst()
                .orElse(null);
    }

    public GearTypeLeveler register(GearTypeLeveler leveler) {
        levelers.add(leveler);
        return leveler;
    }

    public void add(Identifier id, GearTypeLeveler typeLeveler) {
        map.putIfAbsent(id, typeLeveler);
    }

    public void remove(Identifier id) {
        map.remove(id);
    }

    public void clear() {
        map.clear();
    }

    public Set<GearTypeLeveler> gearTypeLevelers() {
        return ImmutableSet.copyOf(levelers);
    }
}
