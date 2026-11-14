package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.loot.WorldInnateBond;
//? if >=1.21.2
import net.minecraft.util.context.ContextKeySet;
//? if <1.21.2
/*import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;*/
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
    //? if >=1.21.2
    private ContextKeySet paramSet;
    //? if <1.21.2
    /*private LootContextParamSet paramSet;*/

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
