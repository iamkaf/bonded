package com.iamkaf.bonded;

import com.iamkaf.amber.api.event.v1.events.common.client.HudEvents;
import com.iamkaf.bonded.client.HUD;
import com.iamkaf.bonded.config.BondedClientConfig;
import com.iamkaf.bonded.network.ProgressionSoundPacket;
import com.iamkaf.bonded.registry.Sounds;
import com.iamkaf.konfig.api.v1.ConfigBuilder;
import com.iamkaf.konfig.api.v1.ConfigHandle;
import com.iamkaf.konfig.api.v1.ConfigScope;
import com.iamkaf.konfig.api.v1.Konfig;
import com.iamkaf.konfig.api.v1.SyncMode;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class BondedClient {
    public static final BondedClientConfig CONFIG;
    public static final ConfigHandle CONFIG_HANDLE;

    static {
        ConfigBuilder builder = Konfig.builder(Bonded.MOD_ID, "client")
                .scope(ConfigScope.CLIENT)
                .syncMode(SyncMode.NONE)
                .fileName("bonded-client.toml")
                .schemaVersion(1)
                .comment("Client-only Bonded settings for tooltips, progression sounds, HUD previews, and item bar colors.")
                .info(info -> info
                        .headerKey("bonded.config.info.client.header")
                        .inlineTextKey("bonded.config.info.client.text")
                        .urlKey("bonded.config.info.report_issue", "https://github.com/iamkaf/bonded"));
        CONFIG = new BondedClientConfig(builder);
        CONFIG_HANDLE = builder.build();
    }

    public static void init() {
        HUD.init();
        HudEvents.RENDER_HUD.register(HUD::onRenderHud);
    }

    public static void playProgressionSound(ProgressionSoundPacket.Kind kind) {
        if (!CONFIG.progressionSoundsEnabled.get()) {
            return;
        }

        float volume = CONFIG.progressionSoundVolume.get().floatValue();
        if (volume <= 0.0F) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        SoundEvent sound = kind == ProgressionSoundPacket.Kind.MAX_LEVEL
                ? Sounds.ITEM_MAX_LEVEL.get()
                : Sounds.ITEM_LEVEL.get();
        minecraft.level.playLocalSound(
                minecraft.player.getX(),
                minecraft.player.getY(),
                minecraft.player.getZ(),
                sound,
                SoundSource.PLAYERS,
                volume,
                1.0F,
                false
        );
    }
}
