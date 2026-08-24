package com.iamkaf.bonded.block;

import com.iamkaf.amber.api.billboard.v1.Billboard;
import com.iamkaf.amber.api.billboard.v1.BillboardAnimation;
import com.iamkaf.amber.api.billboard.v1.Billboards;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

final class BenchEffects {
    private static final int MATERIAL_TRAVEL_TICKS = 14;

    private BenchEffects() {
    }

    static void play(
            ServerPlayer viewer,
            BlockPos benchPos,
            ItemStack material,
            BlockPos storagePos
    ) {
        Vec3 materialOrigin = storagePos == null
                ? viewer.position().add(0.0D, 1.15D, 0.0D)
                : Vec3.atCenterOf(storagePos).add(0.0D, 0.55D, 0.0D);
        Vec3 workSurface = Vec3.atCenterOf(benchPos).add(0.0D, 0.72D, 0.0D);

        Billboard materialFlight = Billboard.item(materialOrigin, material.getItem(), 0.52F)
                .forTicks(MATERIAL_TRAVEL_TICKS)
                .translateBy(workSurface.subtract(materialOrigin), BillboardAnimation.Easing.EASE_OUT_CUBIC)
                .rotateBy(0.0D, 0.0D, 18.0D, BillboardAnimation.Easing.EASE_OUT_CUBIC)
                .scaleFromTo(
                        new Vec3(0.94D, 1.06D, 0.94D),
                        new Vec3(1.04D, 0.96D, 1.04D),
                        BillboardAnimation.Easing.EASE_OUT_CUBIC
                );
        Billboards.show(viewer, materialFlight);
    }
}
