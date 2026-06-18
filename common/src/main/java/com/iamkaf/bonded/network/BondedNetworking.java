package com.iamkaf.bonded.network;

import com.iamkaf.amber.api.networking.v1.NetworkChannel;
import com.iamkaf.bonded.Bonded;
import net.minecraft.server.level.ServerPlayer;

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
        initialized = true;
    }

    public static void sendProgressionSound(ServerPlayer player, ProgressionSoundPacket.Kind kind) {
        CHANNEL.sendToPlayer(new ProgressionSoundPacket(kind), player);
    }
}
