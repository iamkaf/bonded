package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.loot.WorldInnateBond;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(LootTable.class)
public class LootTableMixin {
    @Shadow
    @Final
    private ContextKeySet paramSet;

    @ModifyVariable(
            method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Consumer<ItemStack> bonded$applyInnateBondToLoot(Consumer<ItemStack> output, LootContext context) {
        return stack -> {
            WorldInnateBond.applyToLoot(stack, this.paramSet, context);
            output.accept(stack);
        };
    }
}
