package com.iamkaf.bonded.mixin;

import com.iamkaf.amber.api.item.SmartTooltip;
import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.registry.DataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * This mixin adds the item level info to the item's tooltip.
 */
@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V", at = @At("HEAD"))
    public void bonded$addItemLevelToTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder, TooltipFlag flag, CallbackInfo ci) {
        if (!Bonded.CONFIG.enableTooltips.get()) {
            return;
        }
        var levelingComponent = stack.get(DataComponents.ITEM_LEVEL_CONTAINER.get());

        if (levelingComponent == null) {
            return;
        }

        List<Component> tooltipComponents = new ArrayList<>();

        tooltipComponents.add(Component.literal("Lv. " + levelingComponent.getLevel()).withStyle(ChatFormatting.YELLOW));
        if (levelingComponent.getLevel() != Bonded.CONFIG.levelsToUpgrade.get()) {
            tooltipComponents.add(Component.literal("Exp. " + levelingComponent.getExperience() + "/" + levelingComponent.getMaxExperience())
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.literal("Exp. MAX").withStyle(ChatFormatting.GREEN));
        }

        tooltipComponents.add(Component.literal("Bond " + levelingComponent.getBond() + "\ueef2")
                .withStyle(ChatFormatting.RED));
        tooltipComponents.forEach(tooltipAdder);

        if (flag.isAdvanced()) {
            tooltipComponents = new ArrayList<>();
            var bonuses = stack.get(DataComponents.APPLIED_BONUSES_CONTAINER.get());
            MutableComponent bonusesTooltip =
                    Component.literal(String.format("Bonuses (%s):", bonuses == null ? 0 : bonuses.bonuses().size()));
            SmartTooltip smartTooltip = new SmartTooltip().shift(bonusesTooltip);
            if (bonuses != null) {
                for (var bonus : bonuses.bonuses()) {
                    smartTooltip.shift(Component.literal("- " + bonus.toString()));
                }
            }
            smartTooltip.into(tooltipComponents);
            // FIXME: bandaid fix until i update SmartTooltip to 1.21.5
            tooltipComponents.forEach(tooltipAdder);
        }
    }
}
