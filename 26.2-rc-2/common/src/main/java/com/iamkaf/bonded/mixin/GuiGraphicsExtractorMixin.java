package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.util.MaxDamageModifiers;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
    private static final int BONDED_OVER_REPAIR_COLOR = ARGB.color(255, 255, 102, 221);
    private static final int BAR_WIDTH = 13;

    @Inject(method = "itemBar", at = @At("HEAD"), cancellable = true)
    private void bonded$drawOverRepairBar(ItemStack itemStack, int x, int y, CallbackInfo ci) {
        if (!itemStack.isBarVisible() || !itemStack.isDamaged() || !MaxDamageModifiers.hasOverRepair(itemStack)) {
            return;
        }

        GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;
        int left = x + 2;
        int top = y + 13;
        graphics.fill(RenderPipelines.GUI, left, top, left + BAR_WIDTH, top + 2, -16777216);
        graphics.fill(
                RenderPipelines.GUI,
                left,
                top,
                left + MaxDamageModifiers.getBaseDurabilityBarWidth(itemStack, BAR_WIDTH),
                top + 1,
                ARGB.opaque(MaxDamageModifiers.getBaseDurabilityBarColor(itemStack))
        );
        int pinkWidth = MaxDamageModifiers.getOverRepairBarWidth(itemStack, BAR_WIDTH);
        if (pinkWidth > 0) {
            graphics.fill(RenderPipelines.GUI, left, top, left + pinkWidth, top + 1, BONDED_OVER_REPAIR_COLOR);
        }
        ci.cancel();
    }
}
