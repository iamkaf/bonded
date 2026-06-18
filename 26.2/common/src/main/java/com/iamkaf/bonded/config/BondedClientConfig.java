package com.iamkaf.bonded.config;

import com.iamkaf.konfig.api.v1.ConfigBuilder;
import com.iamkaf.konfig.api.v1.ConfigValue;

public class BondedClientConfig {
    public final ConfigValue<Boolean> tooltipsEnabled;
    public final ConfigValue<Boolean> benchHudEnabled;
    public final ConfigValue<Integer> overRepairBarColor;
    public final ConfigValue<Boolean> progressionSoundsEnabled;
    public final ConfigValue<Double> progressionSoundVolume;

    public BondedClientConfig(ConfigBuilder builder) {
        builder.push("tooltips")
                .categoryComment("Client tooltip details for bonded gear.")
                .categoryInfo(info -> info
                        .headerKey("bonded.config.info.tooltips.header")
                        .inlineTextKey("bonded.config.info.tooltips.text"))
                .header("Tooltips");
        tooltipsEnabled = builder.bool("tooltips_enabled", true)
                .comment("Shows Bonded level, experience, and bonus information in item tooltips.")
                .info(info -> info.inlineTextKey("bonded.config.tooltips_enabled.info"))
                .clientOnly()
                .build();
        builder.pop();

        builder.push("sounds")
                .categoryComment("Client sound feedback for Bonded item progression.")
                .categoryInfo(info -> info
                        .headerKey("bonded.config.info.sounds.header")
                        .inlineTextKey("bonded.config.info.sounds.text"))
                .header("Sounds");
        progressionSoundsEnabled = builder.bool("progression_sounds_enabled", true)
                .comment("Plays Bonded level-up and max-level sounds on your own client.")
                .info(info -> info.inlineTextKey("bonded.config.progression_sounds_enabled.info"))
                .clientOnly()
                .build();
        progressionSoundVolume = builder.doubleRange("progression_sound_volume", 1.0d, 0.0d, 1.0d)
                .comment("Volume for Bonded progression sounds on your own client.")
                .info(info -> info.inlineTextKey("bonded.config.progression_sound_volume.info"))
                .clientOnly()
                .build();
        builder.pop();

        builder.push("hud")
                .categoryComment("Client HUD previews and item bar colors.")
                .categoryInfo(info -> info
                        .headerKey("bonded.config.info.hud.header")
                        .inlineTextKey("bonded.config.info.hud.text"))
                .header("HUD");
        benchHudEnabled = builder.bool("bench_hud_enabled", true)
                .comment("Shows repair and upgrade previews while looking at Bonded benches.")
                .info(info -> info.inlineTextKey("bonded.config.bench_hud_enabled.info"))
                .clientOnly()
                .build();
        overRepairBarColor = builder.colorArgb("over_repair_bar_color", 0xFFFF66DD)
                .comment("Color used for temporary over-repair durability on item bars.")
                .info(info -> info.inlineTextKey("bonded.config.over_repair_bar_color.info"))
                .clientOnly()
                .build();
        builder.pop();
    }
}
