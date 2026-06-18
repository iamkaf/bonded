package com.iamkaf.bonded.network;

import com.iamkaf.amber.api.networking.v1.Packet;
import com.iamkaf.amber.api.networking.v1.PacketContext;
import com.iamkaf.amber.api.networking.v1.PacketDecoder;
import com.iamkaf.amber.api.networking.v1.PacketEncoder;
import com.iamkaf.amber.api.networking.v1.PacketHandler;
import com.iamkaf.bonded.BondedClient;

public record ProgressionSoundPacket(Kind kind) implements Packet<ProgressionSoundPacket> {
    public static final PacketEncoder<ProgressionSoundPacket> ENCODER =
            (packet, buffer) -> buffer.writeEnum(packet.kind);
    public static final PacketDecoder<ProgressionSoundPacket> DECODER =
            buffer -> new ProgressionSoundPacket(buffer.readEnum(Kind.class));
    public static final PacketHandler<ProgressionSoundPacket> HANDLER = ProgressionSoundPacket::handle;

    public enum Kind {
        LEVEL_UP,
        MAX_LEVEL
    }

    private static void handle(ProgressionSoundPacket packet, PacketContext context) {
        if (!context.isClientSide()) {
            return;
        }

        context.execute(() -> BondedClient.playProgressionSound(packet.kind));
    }
}
