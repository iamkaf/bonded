package com.iamkaf.bonded.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record AppliedBonusesContainer(List<ResourceLocation> bonuses) {
    // codec for serializing this class
    public static final Codec<AppliedBonusesContainer> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(ResourceLocation.CODEC.listOf()
                            .fieldOf("bonuses")
                            .forGetter(AppliedBonusesContainer::bonuses))
                    .apply(instance, AppliedBonusesContainer::new));

    public static AppliedBonusesContainer make() {
        return new AppliedBonusesContainer(List.of());
    }

    public static AppliedBonusesContainer make(List<ResourceLocation> bonuses) {
        return new AppliedBonusesContainer(bonuses);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else {
            return obj instanceof AppliedBonusesContainer ex && this.bonuses == ex.bonuses;
        }
    }

    public AppliedBonusesContainer addBonus(ResourceLocation bonus) {
        List<ResourceLocation> newList = new ArrayList<>(bonuses);
        newList.add(bonus);
        return new AppliedBonusesContainer(List.copyOf(newList));
    }
}