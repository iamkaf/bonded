package com.iamkaf.bonded.advancement;

import com.iamkaf.bonded.Bonded;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class BondedAdvancements {
    public static final Identifier FIRST_BOND = Bonded.resource("getting_attached");
    public static final Identifier FIRST_LEVEL = Bonded.resource("it_learns");
    public static final Identifier MAX_LEVEL = Bonded.resource("fully_bonded");
    public static final Identifier UPGRADE = Bonded.resource("a_new_form");
    public static final Identifier OVER_REPAIR = Bonded.resource("better_than_new");
    public static final Identifier BROKEN_GEAR_SCRAP = Bonded.resource("waste_nothing");
    public static final Identifier SCRAP_REPAIR = Bonded.resource("field_maintenance");
    public static final Identifier BOND_500 = Bonded.resource("old_reliable");
    public static final Identifier BOND_1000 = Bonded.resource("trusted_tool");
    public static final Identifier BOND_5000 = Bonded.resource("heirloom");

    private BondedAdvancements() {
    }

    public static void grant(ServerPlayer player, Identifier advancementId) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(advancementId);
        if (advancement != null) {
            player.getAdvancements().award(advancement, "done");
        }
    }

    public static void grantBondMilestones(ServerPlayer player, int bond) {
        if (bond > 0) {
            grant(player, FIRST_BOND);
        }
        if (bond >= 500) {
            grant(player, BOND_500);
        }
        if (bond >= 1000) {
            grant(player, BOND_1000);
        }
        if (bond >= 5000) {
            grant(player, BOND_5000);
        }
    }
}
