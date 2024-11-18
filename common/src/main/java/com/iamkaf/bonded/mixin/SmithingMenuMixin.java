package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.api.event.GameEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin extends ItemCombinerMenu {
    public SmithingMenuMixin(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    @Shadow
    protected abstract List<ItemStack> getRelevantItems();

//    @Inject(method = "onTake", at = @At("TAIL"))
//    private void bonded$onTake(Player player, ItemStack stack, CallbackInfo ci) {
//        GameEvents.MODIFY_SMITHING_RESULT.invoker().smith(player, stack, this.getRelevantItems());
//    }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void bonded$emitSmithingResultEvent(CallbackInfo ci) {
        GameEvents.MODIFY_SMITHING_RESULT.invoker().smith(this.resultSlots.getItem(0), getRelevantItems());
    }
}
