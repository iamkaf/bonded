package com.iamkaf.bonded.leveling.levelers;

import com.iamkaf.bonded.registry.Tags;
import com.iamkaf.bonded.registry.TierMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;

public class MiningToolsLeveler implements GearTypeLeveler {
    @Override
    public String name() {
        return "Mining Tools";
    }

    @Override
    public TagKey<Item> tag() {
        return Tags.DIGGING_EQUIPMENT;
    }
}
