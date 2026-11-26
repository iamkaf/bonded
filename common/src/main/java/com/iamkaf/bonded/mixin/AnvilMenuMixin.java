package com.iamkaf.bonded.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
//? if <1.21.2 {
/*import com.iamkaf.bonded.rules.BondedRules;
import com.iamkaf.bonded.rules.ResolvedGearRule;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
*///?}

/**
 * Before 1.21.2 the anvil asks the item whether a material repairs it, so the resolved repair rule is applied
 * there. Later versions apply it in {@link ItemStackMixin}.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
    //? if <1.21.2 {
    /*@Redirect(
            method = "createResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/Item;isValidRepairItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean bonded$useResolvedRepairRule(Item item, ItemStack stack, ItemStack ingredient) {
        var rule = BondedRules.rule(stack.getItem());
        if (rule == null || !rule.enabled() || rule.repairMode() == ResolvedGearRule.RepairMode.INHERIT) {
            return item.isValidRepairItem(stack, ingredient);
        }
        var repairable = BondedRules.repairable(stack);
        return repairable != null && repairable.items().contains(ingredient.getItem().builtInRegistryHolder());
    }
    *///?}
}
