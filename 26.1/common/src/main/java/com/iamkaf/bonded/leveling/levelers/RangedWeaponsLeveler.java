package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.registry.Tags;
import com.iamkaf.bonded.registry.TierMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Objects;

public class RangedWeaponsLeveler implements GearTypeLeveler {
    @Override
    public String name() {
        return "Ranged Weapons";
    }

    @Override
    public TagKey<Item> tag() {
        return Tags.RANGED_WEAPONS;
    }
}
