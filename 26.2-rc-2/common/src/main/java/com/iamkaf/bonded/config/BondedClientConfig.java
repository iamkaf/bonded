package com.iamkaf.bonded.config;

import com.iamkaf.konfig.api.v1.ConfigBuilder;
import com.iamkaf.konfig.api.v1.ConfigValue;

public class BondedClientConfig {
    public final ConfigValue<Boolean> tooltipsEnabled;
    public final ConfigValue<Boolean> benchHudEnabled;
    public final ConfigValue<Integer> overRepairBarColor;

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
