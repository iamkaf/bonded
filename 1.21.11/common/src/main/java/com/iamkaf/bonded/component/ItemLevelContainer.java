package com.iamkaf.bonded.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public class ItemLevelContainer {
    // codec for serializing this class
    public static final Codec<ItemLevelContainer> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("experience").forGetter(ItemLevelContainer::getExperience),
                    Codec.INT.fieldOf("maxExperience").forGetter(ItemLevelContainer::getMaxExperience),
                    Codec.INT.fieldOf("level").forGetter(ItemLevelContainer::getLevel),
                    Codec.INT.fieldOf("bond").forGetter(ItemLevelContainer::getBond)
            ).apply(instance, ItemLevelContainer::new));


    /**
     * The current experience the item has.
     */
    private final int experience;
    /**
     * The experience needed for the item to reach the next level.
     */
    private final int maxExperience;
    /**
     * The item's level.
     */
    private final int level;
    /**
     * The accumulated bond level with the item.
     */
    private final int bond;

    public ItemLevelContainer(int experience, int maxExperience, int level, int bond) {
        this.experience = experience;
        this.maxExperience = maxExperience;
        this.level = level;
        this.bond = bond;
    }

    public static ItemLevelContainer make(int maxExperience) {
        return new ItemLevelContainer(0, maxExperience, 1, 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.experience, this.maxExperience, this.level);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else {
            return obj instanceof ItemLevelContainer ex && this.experience == ex.experience && this.maxExperience == ex.maxExperience && this.level == ex.level && this.bond == ex.bond;
        }
    }

    public int getExperience() {
        return experience;
    }

    public int getMaxExperience() {
        return maxExperience;
    }

    public int getLevel() {
        return level;
    }

    public int getBond() {
        return bond;
    }

    public ItemLevelContainer addExperience(int amount) {
        return new ItemLevelContainer(experience + amount, maxExperience, level, bond);
    }

    public ItemLevelContainer addLevel(int newMaxExperience) {
        return new ItemLevelContainer(0, newMaxExperience, level + 1, bond);
    }

    public ItemLevelContainer addBond(int amount) {
        return new ItemLevelContainer(experience, maxExperience, level, bond + amount);
    }
}
