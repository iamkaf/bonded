package com.iamkaf.bonded.network;

import com.iamkaf.amber.api.networking.v1.NetworkChannel;
import com.iamkaf.amber.api.event.v1.events.common.PlayerEvents;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.rules.BondedRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

public final class BondedNetworking {
    private static final NetworkChannel CHANNEL = NetworkChannel.create(Bonded.resource("main"));
    private static boolean initialized = false;

    private BondedNetworking() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        CHANNEL.register(
                ProgressionSoundPacket.class,
                ProgressionSoundPacket.ENCODER,
                ProgressionSoundPacket.DECODER,
                ProgressionSoundPacket.HANDLER
        );
        CHANNEL.register(
                GearRulesPacket.class,
                GearRulesPacket.ENCODER,
                GearRulesPacket.DECODER,
                GearRulesPacket.HANDLER
        );
        CHANNEL.register(
                DebugGearRulesScreenPacket.class,
                DebugGearRulesScreenPacket.ENCODER,
                DebugGearRulesScreenPacket.DECODER,
                DebugGearRulesScreenPacket.HANDLER
        );
        PlayerEvents.PLAYER_JOIN.register(player -> sendGearRules(player));
        initialized = true;
    }

    public static void sendProgressionSound(ServerPlayer player, ProgressionSoundPacket.Kind kind) {
        CHANNEL.sendToPlayer(new ProgressionSoundPacket(kind), player);
    }

    public static void openDebugGearRulesScreen(ServerPlayer player) {
        CHANNEL.sendToPlayer(new DebugGearRulesScreenPacket(BondedRules.packetJson()), player);
    }

    public static void broadcastGearRules(MinecraftServer server) {
        String json = BondedRules.packetJson();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendGearRules(player, json);
        }
    }

    private static void sendGearRules(ServerPlayer player) {
        sendGearRules(player, BondedRules.packetJson());
    }

    private static void sendGearRules(ServerPlayer player, String json) {
        CHANNEL.sendToPlayer(
                new GearRulesPacket(
                        json,
                        !player.level().getServer().isSingleplayerOwner(new NameAndId(player.getGameProfile()))
                ),
                player
        );
    }
}
