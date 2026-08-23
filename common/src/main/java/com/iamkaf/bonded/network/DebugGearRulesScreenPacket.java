package com.iamkaf.bonded.network;

import com.iamkaf.amber.api.networking.v1.Packet;
import com.iamkaf.amber.api.networking.v1.PacketContext;
import com.iamkaf.amber.api.networking.v1.PacketDecoder;
import com.iamkaf.amber.api.networking.v1.PacketEncoder;
import com.iamkaf.amber.api.networking.v1.PacketHandler;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.BondedClient;
import com.iamkaf.bonded.rules.BondedRules;

/** Development-only packet used by the runtime config-screen probe. */
public record DebugGearRulesScreenPacket(String json) implements Packet<DebugGearRulesScreenPacket> {
    private static final int MAX_RULE_SNAPSHOT_LENGTH = 1_000_000;
    public static final PacketEncoder<DebugGearRulesScreenPacket> ENCODER =
            (packet, buffer) -> buffer.writeUtf(packet.json, MAX_RULE_SNAPSHOT_LENGTH);
    public static final PacketDecoder<DebugGearRulesScreenPacket> DECODER =
            buffer -> new DebugGearRulesScreenPacket(buffer.readUtf(MAX_RULE_SNAPSHOT_LENGTH));
    public static final PacketHandler<DebugGearRulesScreenPacket> HANDLER = DebugGearRulesScreenPacket::handle;

    private static void handle(DebugGearRulesScreenPacket packet, PacketContext context) {
        if (!context.isClientSide()) {
            return;
        }
        context.execute(() -> {
            var snapshot = BondedRules.installClientSnapshot(packet.json);
            Bonded.GEAR_RULES_CONFIG.installRemoteView(snapshot.rules().values());
            BondedClient.openGearRulesConfigScreen();
        });
    }
}
