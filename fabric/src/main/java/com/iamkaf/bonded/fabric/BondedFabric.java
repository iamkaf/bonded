package com.iamkaf.bonded.fabric;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.registry.TierMap;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Repairable;
import net.neoforged.fml.config.ModConfig;

public final class BondedFabric implements ModInitializer {
    @Override
    @SuppressWarnings("deprecation")
    public void onInitialize() {
        Bonded.init();
        NeoForgeConfigRegistry.INSTANCE.register(Bonded.MOD_ID, ModConfig.Type.COMMON, Bonded.CONFIG_SPEC);

        ServerLivingEntityEvents.AFTER_DAMAGE.register((LivingEntity entity, DamageSource source,
                float baseDamageTaken, float damageTaken, boolean blocked) -> {
            if (entity instanceof ServerPlayer player && blocked) {
                Bonded.onShieldBlock(player, baseDamageTaken);
            }
        });

        DefaultItemComponentEvents.MODIFY.register(modifyContext -> {
            TierMap.getRepairMaterialMap().forEach((tier, material) -> {
                modifyContext.modify(tier, builder -> {
                    builder.set(
                            DataComponents.REPAIRABLE,
                            new Repairable(HolderSet.direct(material.builtInRegistryHolder()))
                    );
                });
            });
        });
    }
}
