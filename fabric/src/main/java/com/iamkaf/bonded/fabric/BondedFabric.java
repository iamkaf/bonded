package com.iamkaf.bonded.fabric;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.api.event.GameEvents;
import com.iamkaf.bonded.leveling.GameplayHooks;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public final class BondedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Bonded.init();
        NeoForgeConfigRegistry.INSTANCE.register(Bonded.MOD_ID, ModConfig.Type.COMMON, Bonded.CONFIG_SPEC);

        ServerLivingEntityEvents.AFTER_DAMAGE.register((LivingEntity entity, DamageSource source,
                float baseDamageTaken, float damageTaken, boolean blocked) -> {
            if (entity instanceof ServerPlayer player && blocked) {
                Bonded.onShieldBlock(player, baseDamageTaken);
            }
        });
    }
}
