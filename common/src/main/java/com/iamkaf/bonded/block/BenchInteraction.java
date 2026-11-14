package com.iamkaf.bonded.block;

//? if >=1.21.2 {
import net.minecraft.world.InteractionResult;
//?} else {
/*import net.minecraft.world.ItemInteractionResult;
*///?}

/** Results for a bench's held-item interaction, whose result type changed in 1.21.2. */
final class BenchInteraction {
    private BenchInteraction() {
    }

    //? if >=1.21.2 {
    static InteractionResult handledByServer() {
        return InteractionResult.SUCCESS_SERVER;
    }

    static InteractionResult succeeded() {
        return InteractionResult.SUCCESS;
    }
    //?} else {
    /*static ItemInteractionResult handledByServer() {
        return ItemInteractionResult.SUCCESS;
    }

    static ItemInteractionResult succeeded() {
        return ItemInteractionResult.SUCCESS;
    }
    *///?}
}
