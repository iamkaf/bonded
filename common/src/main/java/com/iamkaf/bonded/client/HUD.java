package com.iamkaf.bonded.client;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.block.RepairBenchBlock;
import com.iamkaf.bonded.block.ToolBenchBlock;
import com.iamkaf.bonded.leveling.levelers.GearTypeLeveler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * HUD class responsible for rendering custom HUD elements in the game.
 */
public class HUD {
    public static final int WHITE = 0xffffffff;
    public static final int OUTLINE_COLOR = 0x00000000;
    public static boolean enabled = true;
    private static Minecraft mc;

    /**
     * Initializes the HUD by getting the Minecraft instance.
     */
    public static void init() {
        mc = Minecraft.getInstance();
    }

    /**
     * Renders the HUD elements on the screen.
     *
     * @param guiGraphics  The graphics context for rendering the GUI.
     * @param deltaTracker The delta tracker for tracking changes.
     */
    public static void onRenderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (mc == null) mc = Minecraft.getInstance();

        if (!shouldRender()) {
            return;
        }

        LocalPlayer player = mc.player;
        assert mc.level != null;
        assert player != null;
        BlockHitResult hitResult = raytrace(mc.level, player);

        boolean hasMenuOpen = mc.screen != null;

        if (hitResult.getType() == BlockHitResult.Type.MISS || hasMenuOpen) {
            return;
        }

        BlockState block = player.level().getBlockState(hitResult.getBlockPos());
        int centerX = guiGraphics.guiWidth() / 2;
        int centerY = guiGraphics.guiHeight() / 2;
        int xOffset = 15;
        int yOffset = -5;

        if (block.getBlock() instanceof RepairBenchBlock) {
            renderRepairBenchInfo(guiGraphics, mc.font, centerX + xOffset, centerY + yOffset, player);
        } else if (block.getBlock() instanceof ToolBenchBlock) {
            renderToolBenchInfo(guiGraphics, mc.font, centerX + xOffset, centerY + yOffset, player);
        }
    }

    /**
     * Checks if the HUD should be rendered.
     *
     * @return true if the HUD should be rendered, false otherwise.
     */
    private static boolean shouldRender() {
        return !(mc.getDebugOverlay()
                .showDebugScreen() || mc.options.hideGui || !enabled || mc.level == null || mc.player == null);
    }

    /**
     * Renders information about the Repair Bench on the HUD.
     *
     * @param guiGraphics  The graphics context for rendering the GUI.
     * @param textRenderer The font renderer for rendering text.
     * @param x            The x-coordinate for rendering.
     * @param y            The y-coordinate for rendering.
     * @param player       The local player.
     */
    private static void renderRepairBenchInfo(GuiGraphics guiGraphics, Font textRenderer, int x, int y, LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        GearTypeLeveler leveler = Bonded.GEAR.getLeveler(stack);
        if (leveler == null) return;

        Repairable repairable = stack.get(DataComponents.REPAIRABLE);
        if (stack.isEmpty() || repairable == null) return;
        ItemStack repairIngredient = repairable.items().get(0).value().getDefaultInstance();

        text(
                guiGraphics,
                textRenderer,
                Component.translatable("bonded.hud.repair"),
                x - 2,
                y - textRenderer.lineHeight - 4
        );
        TooltipRenderUtil.renderTooltipBackground(guiGraphics, x, y, 20, 20, null);
        guiGraphics.renderItem(repairIngredient, x + 2, y + 2);
        HUD.renderTooltip(guiGraphics, repairIngredient, x + 16, y + 12);
        // TODO: guiGraphics.renderItemDecorations()
    }

    /**
     * Renders information about the Tool Bench on the HUD.
     *
     * @param guiGraphics  The graphics context for rendering the GUI.
     * @param textRenderer The font renderer for rendering text.
     * @param x            The x-coordinate for rendering.
     * @param y            The y-coordinate for rendering.
     * @param player       The local player.
     */
    private static void renderToolBenchInfo(GuiGraphics guiGraphics, Font textRenderer, int x, int y, LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        GearTypeLeveler leveler = Bonded.GEAR.getLeveler(stack);
        if (leveler == null) return;

        assert mc.level != null;
        var lookup = mc.level.holderLookup(Registries.ITEM);

        TagKey<Item> upgradeIngredientTag = leveler.getUpgradeIngredient(stack);
        assert upgradeIngredientTag != null;
        Optional<HolderSet.Named<Item>> holders = lookup.get(upgradeIngredientTag);
        if (holders.isEmpty()) return;
        ItemStack upgradeIngredient = holders.get().get(0).value().getDefaultInstance();

        Item upgrade = leveler.getUpgrade(stack);
        if (stack.isEmpty() || upgrade == null) return;

        text(
                guiGraphics,
                textRenderer,
                Component.translatable("bonded.hud.upgrade"),
                x - 1,
                y - textRenderer.lineHeight - 4
        );
        TooltipRenderUtil.renderTooltipBackground(guiGraphics, x, y, 20, 20, null);
        guiGraphics.renderItem(upgradeIngredient, x + 2, y + 2);
        TooltipRenderUtil.renderTooltipBackground(guiGraphics, x + 24 + 4, y, 20, 20, null);
        guiGraphics.renderItem(upgrade.getDefaultInstance(), x + 24 + 2 + 4, y + 2);
        HUD.renderTooltip(guiGraphics, stack, x + 44, y + 12);
    }

    /**
     * Renders text on the HUD.
     *
     * @param context The graphics context for rendering the GUI.
     * @param font    The font renderer for rendering text.
     * @param message The message to render.
     * @param x       The x-coordinate for rendering.
     * @param y       The y-coordinate for rendering.
     */
    private static void text(GuiGraphics context, Font font, Component message, int x, int y) {
        context.drawString(font, message, x, y, WHITE);
    }

    /**
     * Performs a raytrace to determine the block the player is looking at.
     *
     * @param level  The level in which the player is located.
     * @param player The player performing the raytrace.
     * @return The result of the raytrace.
     */
    public static @NotNull BlockHitResult raytrace(Level level, Player player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 rotation = player.getViewVector(1);
        double reach = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        Vec3 combined = eyePosition.add(rotation.x * reach, rotation.y * reach, rotation.z * reach);
        return level.clip(new ClipContext(eyePosition, combined, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    /**
     * Renders a tooltip for the given item stack at the specified coordinates.
     *
     * @param guiGraphics The graphics context for rendering the GUI.
     * @param stack       The item stack for which to render the tooltip.
     * @param x           The x-coordinate for rendering the tooltip.
     * @param y           The y-coordinate for rendering the tooltip.
     */
    private static void renderTooltip(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        guiGraphics.renderTooltip(
                mc.font,
                // converts a list of components to a list of ClientTooltipComponents
                Screen.getTooltipFromItem(mc, stack)
                        .stream()
                        .map(Component::getVisualOrderText)
                        .map(ClientTooltipComponent::create)
                        .toList(), x, y, DefaultTooltipPositioner.INSTANCE, stack.get(DataComponents.TOOLTIP_STYLE)
        );
    }
}