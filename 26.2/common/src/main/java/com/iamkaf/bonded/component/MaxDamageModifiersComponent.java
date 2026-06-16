package com.iamkaf.bonded.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record MaxDamageModifiersComponent(int baseMaxDamage, List<AttributeModifier> modifiers) {
    public static final Codec<MaxDamageModifiersComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("base_max_damage").forGetter(MaxDamageModifiersComponent::baseMaxDamage),
            AttributeModifier.CODEC.listOf().optionalFieldOf("modifiers", List.of())
                    .forGetter(MaxDamageModifiersComponent::modifiers)
    ).apply(instance, MaxDamageModifiersComponent::new));

    public MaxDamageModifiersComponent {
        baseMaxDamage = Math.max(0, baseMaxDamage);
        modifiers = List.copyOf(modifiers);
    }

    public static MaxDamageModifiersComponent make(int baseMaxDamage) {
        return new MaxDamageModifiersComponent(baseMaxDamage, List.of());
    }

    public Optional<AttributeModifier> get(Identifier id) {
        return modifiers.stream().filter(modifier -> modifier.is(id)).findFirst();
    }

    public MaxDamageModifiersComponent withModifier(AttributeModifier modifier) {
        List<AttributeModifier> updated = modifiers.stream()
                .filter(existing -> !existing.is(modifier.id()))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        updated.add(modifier);
        return new MaxDamageModifiersComponent(baseMaxDamage, updated);
    }

    public MaxDamageModifiersComponent withoutModifier(Identifier id) {
        return new MaxDamageModifiersComponent(
                baseMaxDamage,
                modifiers.stream().filter(modifier -> !modifier.is(id)).toList()
        );
    }

    public boolean isEmpty() {
        return modifiers.isEmpty();
    }

    public int additiveTotal() {
        return additiveTotalExcept(null);
    }

    public int additiveTotalExcept(Identifier excludedId) {
        int total = 0;
        for (AttributeModifier modifier : modifiers) {
            if (excludedId != null && modifier.is(excludedId)) {
                continue;
            }

            if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                total += Math.max(0, (int) Math.round(modifier.amount()));
            }
        }
        return total;
    }
}
