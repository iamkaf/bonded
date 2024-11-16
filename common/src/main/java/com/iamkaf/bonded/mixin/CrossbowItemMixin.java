package com.iamkaf.bonded.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CrossbowItem;
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
@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin extends Item {
    public CrossbowItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(method =
            "appendHoverText(Lnet/minecraft/world/item/ItemStack;" + "Lnet/minecraft/world/item" +
                    "/Item$TooltipContext;Ljava/util/List;" + "Lnet/minecraft/world/item/TooltipFlag;)V",
            at = @At("HEAD"))
    public void addItemLevelToTooltip(ItemStack stack, TooltipContext context,
            List<Component> tooltipComponents, TooltipFlag tooltipFlag, CallbackInfo ci) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
