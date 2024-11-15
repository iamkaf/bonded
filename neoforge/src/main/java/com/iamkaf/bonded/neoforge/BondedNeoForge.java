package com.iamkaf.bonded.neoforge;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.api.event.GameEvents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

@Mod(Bonded.MOD_ID)
public final class BondedNeoForge {
    public BondedNeoForge(IEventBus eBussy, ModContainer container) {
        // Run our common setup.
        Bonded.init();
        container.registerConfig(ModConfig.Type.COMMON, Bonded.CONFIG_SPEC);
    }

    @EventBusSubscriber
    static class Events {
        @SubscribeEvent
        public static void onEntityDamageAfter(LivingShieldBlockEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                // this is the ideal implementation but the fabric side doesn't have anything similar (as of 1.21.1)
                // Bonded.onShieldBlock(player, event.getBlockedDamage());

                Bonded.onShieldBlock(player, event.getOriginalBlockedDamage());
            }
        }
    }
}
