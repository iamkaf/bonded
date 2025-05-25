package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.registry.Tags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ArmorLeveler implements GearTypeLeveler {
    @Override
    public String name() {
        return "Armors";
    }

    @Override
    public TagKey<Item> tag() {
        return Tags.ARMORS;
    }
}
