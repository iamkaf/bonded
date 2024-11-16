package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.registry.DataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * This mixin adds the item level info to the item's tooltip.
 */
@Mixin(Item.class)
public class ItemMixin {
    @Inject(method =
            "appendHoverText(Lnet/minecraft/world/item/ItemStack;" + "Lnet/minecraft/world/item" +
                    "/Item$TooltipContext;Ljava/util/List;" + "Lnet/minecraft/world/item/TooltipFlag;)V",
            at = @At("HEAD"))
    public void addItemLevelToTooltip(ItemStack stack, Item.TooltipContext context,
            List<Component> tooltipComponents, TooltipFlag tooltipFlag, CallbackInfo ci) {
        var levelingComponent = stack.get(DataComponents.ITEM_LEVEL_CONTAINER.get());

        if (levelingComponent == null) {
            return;
        }

        tooltipComponents.add(Component.literal("Lv. " + levelingComponent.getLevel())
                .withStyle(ChatFormatting.YELLOW));
        if (levelingComponent.getLevel() != Bonded.CONFIG.levelsToUpgrade.get()) {
            tooltipComponents.add(Component.literal("Exp. " + levelingComponent.getExperience() + "/" + levelingComponent.getMaxExperience())
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.literal("Exp. MAX").withStyle(ChatFormatting.GREEN));
        }

        tooltipComponents.add(Component.literal("Bond " + levelingComponent.getBond() + "\ueef2")
                .withStyle(ChatFormatting.RED));

//        var isUpgradable = EquipmentLeveler.isUpgradable(stack);
//
//        if (levelingComponent.getLevel() == EquipmentLeveler.MAXIMUM_LEVEL && isUpgradable) {
//            if (!Screen.hasShiftDown()) {
//                tooltipComponents.add(Component.literal("Hold [SHIFT] for info on upgrading.")
//                        .withStyle(ChatFormatting.AQUA));
//            } else {
//                tooltipComponents.add(Component.literal(
//                                "When the experience is maxed out you can upgrade to the next tier.")
//                        .withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE));
//                tooltipComponents.add(Component.literal(
//                                "Sneak and Right-Click on a Tool Bench to upgrade.")
//                        .withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE));
//                tooltipComponents.add(Component.literal("You must have a piece of " +
//                amberdreams$getRepairIngredientName(
//                                stack) + " in your inventory, it will be consumed.")
//                        .withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE));
//            }
//        }
    }

//    @Unique
//    private @Nullable String amberdreams$getRepairIngredientName(ItemStack stack) {
//        Ingredient ingredient = Ingredient.EMPTY;
//        if (stack.getItem() instanceof ArmorItem armorItem) {
//            ArmorMaterial material = armorItem.getMaterial().value();
//            var upgradeMaterial = ArmorUpgrader.upgradeMap.get(material);
//            if (upgradeMaterial == null) return null;
//
//            ingredient = upgradeMaterial.repairIngredient().get();
//        }
//        if (stack.getItem() instanceof TieredItem tieredItem) {
//            Tier tier = tieredItem.getTier();
//            var upgradeTier = ToolUpgrader.upgradeMap.get(tier);
//            if (upgradeTier == null) return null;
//
//            ingredient = upgradeTier.getRepairIngredient();
//        }
//
//        var repairItem = Arrays.stream(ingredient.getItems()).findFirst().orElse(ItemStack.EMPTY);
//        return repairItem.getItem().getName(stack).getString();
//    }
}
