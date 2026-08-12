package com.iamkaf.bonded.compat;

import com.iamkaf.amber.api.functions.v1.ItemFunctions;
import com.iamkaf.bonded.Bonded;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

final class LiteminerExperience {
    private static final Identifier SILK_TOUCH =
            Identifier.fromNamespaceAndPath("minecraft", "silk_touch");

    private LiteminerExperience() {
    }

    static int forBlock(ItemStack tool, BlockState state) {
        return ItemFunctions.containsEnchantment(tool, SILK_TOUCH)
                ? 1
                : Bonded.GEAR.getExperienceForBlock(state);
    }

    static int total(int originExperience, List<Integer> secondaryExperience) {
        return originExperience + secondary(secondaryExperience);
    }

    /**
     * Applies one-half reward to the first secondary block, one-third to the next, and so on,
     * then rounds the combined secondary reward once.
     */
    static int secondary(List<Integer> blockExperience) {
        double experience = 0;
        for (int index = 0; index < blockExperience.size(); index++) {
            experience += (double) blockExperience.get(index) / (index + 2);
        }
        return (int) Math.round(experience);
    }
}
