package com.iamkaf.bonded.network;

import com.iamkaf.amber.api.networking.v1.Packet;
import com.iamkaf.amber.api.networking.v1.PacketContext;
import com.iamkaf.amber.api.networking.v1.PacketDecoder;
import com.iamkaf.amber.api.networking.v1.PacketEncoder;
import com.iamkaf.amber.api.networking.v1.PacketHandler;
import com.iamkaf.bonded.rules.BondedRules;

/** The server's concrete, restart-frozen rules used by client previews. */
public record GearRulesPacket(String json, boolean remoteAuthority) implements Packet<GearRulesPacket> {
    private static final int MAX_RULE_SNAPSHOT_LENGTH = 1_000_000;
    public static final PacketEncoder<GearRulesPacket> ENCODER =
            (packet, buffer) -> {
                buffer.writeUtf(packet.json, MAX_RULE_SNAPSHOT_LENGTH);
                buffer.writeBoolean(packet.remoteAuthority);
            };
    public static final PacketDecoder<GearRulesPacket> DECODER =
            buffer -> new GearRulesPacket(
                    buffer.readUtf(MAX_RULE_SNAPSHOT_LENGTH),
                    buffer.readBoolean()
            );
    public static final PacketHandler<GearRulesPacket> HANDLER = GearRulesPacket::handle;

    private static void handle(GearRulesPacket packet, PacketContext context) {
        if (!context.isClientSide()) {
            return;
        }
        context.execute(() -> {
            var snapshot = BondedRules.installClientSnapshot(packet.json);
            if (packet.remoteAuthority) {
                com.iamkaf.bonded.Bonded.GEAR_RULES_CONFIG.installRemoteView(snapshot.rules().values());
            } else {
                com.iamkaf.bonded.Bonded.GEAR_RULES_CONFIG.clearRemoteView();
            }
        });
    }
}
