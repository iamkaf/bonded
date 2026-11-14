package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.api.event.GameEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
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
    //? if >=1.21.2 {
    public SmithingMenuMixin(@Nullable MenuType<?> menuType, int i, Inventory inventory,
            ContainerLevelAccess containerLevelAccess, ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition) {
        super(menuType, i, inventory, containerLevelAccess, itemCombinerMenuSlotDefinition);
    }
    //?} else {
    /*public SmithingMenuMixin(@Nullable MenuType<?> menuType, int i, Inventory inventory,
            ContainerLevelAccess containerLevelAccess) {
        super(menuType, i, inventory, containerLevelAccess);
    }
    *///?}

    @Shadow
    protected abstract List<ItemStack> getRelevantItems();

    @Inject(method = "createResult", at = @At("TAIL"))
    private void bonded$emitSmithingResultEvent(CallbackInfo ci) {
        GameEvents.MODIFY_SMITHING_RESULT.invoker().smith(this.resultSlots.getItem(0), getRelevantItems());
    }
}
