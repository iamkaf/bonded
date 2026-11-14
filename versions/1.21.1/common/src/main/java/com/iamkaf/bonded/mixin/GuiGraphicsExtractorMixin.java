package com.iamkaf.bonded.mixin;

import com.iamkaf.bonded.BondedClient;
import com.iamkaf.bonded.util.MaxDamageModifiers;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the over-repair durability bar. Here the vanilla bar is drawn inside the item decoration
 * method instead of its own method, so the vanilla bar is switched off and replaced in place.
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsExtractorMixin {
    @Unique
    private static final int BAR_WIDTH = 13;
    @Unique
    private static final String DECORATIONS =
            "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V";

    @Unique
    private ItemStack bonded$overRepairStack;
    @Unique
    private int bonded$barX;
    @Unique
    private int bonded$barY;

    @Inject(method = DECORATIONS, at = @At("HEAD"))
    private void bonded$findOverRepairBar(Font font, ItemStack itemStack, int x, int y, String text, CallbackInfo ci) {
        boolean overRepair = itemStack.isBarVisible() && itemStack.isDamaged()
                && MaxDamageModifiers.hasOverRepair(itemStack);
        bonded$overRepairStack = overRepair ? itemStack : null;
        bonded$barX = x;
        bonded$barY = y;
    }

    @Redirect(
            method = DECORATIONS,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isBarVisible()Z")
    )
    private boolean bonded$drawOverRepairBar(ItemStack itemStack) {
        if (itemStack != bonded$overRepairStack) {
            return itemStack.isBarVisible();
        }

        GuiGraphics graphics = (GuiGraphics) (Object) this;
        int left = bonded$barX + 2;
        int top = bonded$barY + 13;
        graphics.fill(RenderType.guiOverlay(), left, top, left + BAR_WIDTH, top + 2, -16777216);
        graphics.fill(
                RenderType.guiOverlay(),
                left,
                top,
                left + MaxDamageModifiers.getBaseDurabilityBarWidth(itemStack, BAR_WIDTH),
                top + 1,
                MaxDamageModifiers.getBaseDurabilityBarColor(itemStack) | 0xFF000000
        );
        int pinkWidth = MaxDamageModifiers.getOverRepairBarWidth(itemStack, BAR_WIDTH);
        if (pinkWidth > 0) {
            graphics.fill(
                    RenderType.guiOverlay(),
                    left,
                    top,
                    left + pinkWidth,
                    top + 1,
                    BondedClient.CONFIG.overRepairBarColor.get()
            );
        }
        bonded$overRepairStack = null;
        return false;
    }
}
