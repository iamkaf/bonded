package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.augment.OceanicAugment;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "doTick", at = @At("TAIL"))
    private void bonded$tickAugments(CallbackInfo ci) {
        OceanicAugment.tick((ServerPlayer) (Object) this);
    }
}
