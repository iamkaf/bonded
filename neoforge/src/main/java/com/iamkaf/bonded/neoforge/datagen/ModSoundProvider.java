package com.iamkaf.bonded.neoforge.datagen;


import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.registry.Sounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class ModSoundProvider extends SoundDefinitionsProvider {
    public ModSoundProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Bonded.MOD_ID, existingFileHelper);
    }

    @Override
    public void registerSounds() {
        // Accepts a Supplier<SoundEvent>, a SoundEvent, or a ResourceLocation as the first parameter.
        add(Sounds.ITEM_LEVEL, SoundDefinition.definition()
                // Add sound objects to the sound definition. Parameter is a vararg.
                .with(
                        // Accepts either a string or a ResourceLocation as the first parameter.
                        // The second parameter can be either SOUND or EVENT, and can be omitted if the
                        // former.
                        sound(Sounds.ITEM_LEVEL.get().getLocation(), SoundDefinition.SoundType.SOUND)
                                // Sets the volume. Also has a double counterpart.
                                .volume(0.8f)
                                // Sets the pitch. Also has a double counterpart.
                                .pitch(1.2f)
                                // Sets the weight.
                                .weight(2)
                                // Sets the attenuation distance.
                                .attenuationDistance(8)
                                // Enables streaming.
                                // Also has a parameterless overload that defers to stream(true).
                                .stream(true)
                                // Enables preloading.
                                // Also has a parameterless overload that defers to preload(true).
                                .preload(true))
                // Sets the subtitle.
                .subtitle("sound.bonded.item_level")
                // Enables replacing.
                .replace(true));

        add(Sounds.ITEM_MAX_LEVEL,
                SoundDefinition.definition()
                        .with(sound(Sounds.ITEM_MAX_LEVEL.get().getLocation(),
                                SoundDefinition.SoundType.SOUND
                        ).volume(0.8f)
                                .pitch(1.2f)
                                .weight(2)
                                .attenuationDistance(8)
                                .stream(true)
                                .preload(true))
                        .subtitle("sound.bonded.item_max_level")
                        .replace(true)
        );
    }
}