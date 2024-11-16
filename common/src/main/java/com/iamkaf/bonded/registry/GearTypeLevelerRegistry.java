package com.iamkaf.bonded.registry;

import com.google.common.collect.ImmutableSet;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GearTypeLevelerRegistry {
    public final Map<ResourceLocation, GearTypeLeveler> map = new HashMap<>();
    public final Set<GearTypeLeveler> levelers = new HashSet<>();

    public @Nullable GearTypeLeveler get(ItemStack gear) {
        if (gear.isEmpty()) {
            return null;
        }

        return map.get(gear.getItem().arch$registryName());
    }

    public GearTypeLeveler register(GearTypeLeveler leveler) {
        levelers.add(leveler);
        return leveler;
    }

    public void add(ResourceLocation id, GearTypeLeveler typeLeveler) {
        map.putIfAbsent(id, typeLeveler);
    }

    public void remove(ResourceLocation id) {
        map.remove(id);
    }

    public void clear() {
        map.clear();
    }

    public Set<GearTypeLeveler> gearTypeLevelers() {
        return ImmutableSet.copyOf(levelers);
    }
}
