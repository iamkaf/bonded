package com.iamkaf.bonded.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Persistent augment progress. Unknown ids are retained so temporarily missing addons do not lose data.
 */
public record AugmentProgressContainer(Map<Identifier, Integer> progress) {
    public static final Codec<AugmentProgressContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, Codec.INT)
                    .fieldOf("progress")
                    .forGetter(AugmentProgressContainer::progress)
    ).apply(instance, AugmentProgressContainer::new));

    public AugmentProgressContainer {
        Objects.requireNonNull(progress, "progress");
        Map<Identifier, Integer> sanitized = new LinkedHashMap<>();
        progress.forEach((id, value) -> {
            if (id != null && value != null && value > 0) {
                sanitized.put(id, value);
            }
        });
        progress = Map.copyOf(sanitized);
    }

    public static AugmentProgressContainer empty() {
        return new AugmentProgressContainer(Map.of());
    }

    public int get(Identifier id) {
        return progress.getOrDefault(id, 0);
    }

    public boolean isEmpty() {
        return progress.isEmpty();
    }

    public AugmentProgressContainer with(Identifier id, int value) {
        Map<Identifier, Integer> updated = new LinkedHashMap<>(progress);
        if (value <= 0) {
            updated.remove(id);
        } else {
            updated.put(id, value);
        }
        return new AugmentProgressContainer(updated);
    }
}
