package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.leveling.types.GearTypeLeveler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GearTypeLevelerRegistry {
    public final Map<ResourceLocation, GearTypeLeveler> map = new HashMap<>();

    public @Nullable GearTypeLeveler get(ItemStack gear) {
        if (gear.isEmpty()) {
            return null;
        }

        return map.get(gear.getItem().arch$registryName());
    }

    public void add(ResourceLocation id, GearTypeLeveler typeLeveler) {
        map.put(id, typeLeveler);
    }

    public void remove(ResourceLocation id) {
        map.remove(id);
    }

    public void clear() {
        map.clear();
    }
}
