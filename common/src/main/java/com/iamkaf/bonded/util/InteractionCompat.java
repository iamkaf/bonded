package com.iamkaf.bonded.util;

import net.minecraft.world.InteractionResult;

/** Interaction result checks whose shape changed between Minecraft versions. */
public final class InteractionCompat {
    private InteractionCompat() {
    }

    /** Whether an item use on the server did its work, which is what experience rewards hang on. */
    public static boolean succeeded(InteractionResult result) {
        //? if >=1.21.2 {
        return result == InteractionResult.SUCCESS;
        //?} else {
        /*// Before 1.21.2 the server answers a success with CONSUME, and only the client sees SUCCESS.
        return result.consumesAction();
        *///?}
    }
}
