import { Capability, Readiness, describe, expect, test } from "@teakit/test";

describe.configure({
  timeout: "8m",
  readiness: [Readiness.ClientReady, Readiness.IntegratedServerReady, Readiness.PlayerSpawned],
  capabilities: [
    Capability.ClientInput,
    Capability.ClientScreen,
    Capability.ClientScreens,
    Capability.PlayerInventory,
    Capability.PlayerUseItem,
    Capability.RuntimeTiming,
    Capability.ServerCommands,
    Capability.WorldRecipes,
  ],
});

describe("Patchouli field guide", () => {
  test(
    "does not expose the guide recipe when Patchouli is absent",
    {
      target: {
        minecraft: ">=26.2",
        loader: ["fabric", "neoforge"],
      },
    },
    async ({ commands }) => {
      await expect(
        commands.run("/recipe give @s bonded:bonded_field_guide", {
          captureOutput: true,
          requireSuccess: true,
        }),
      ).rejects.toThrow();
    },
  );

  test(
    "does not expose an unsupported Forge guide recipe",
    {
      target: {
        loader: ["forge"],
      },
    },
    async ({ commands }) => {
      await expect(
        commands.run("/recipe give @s bonded:bonded_field_guide", {
          captureOutput: true,
          requireSuccess: true,
        }),
      ).rejects.toThrow();
    },
  );

  test(
    "crafts the guide and exposes its recipe and augment chapters",
    {
      target: {
        minecraft: ">=26.1 <26.2",
        loader: ["fabric", "neoforge"],
      },
    },
    async (ctx) => {
      await ctx.commands.run("/gamemode survival");
      await ctx.commands.run("/clear @s");

      const recipe = await ctx.recipes.assertCrafting(
        2,
        1,
        ["minecraft:book", "bonded:scrap"],
        "patchouli:guide_book",
      );
      expect(recipe.recipeId).toBe("bonded:bonded_field_guide");

      await ctx.commands.assert("/item replace entity @s hotbar.0 with minecraft:book");
      await ctx.commands.assert("/item replace entity @s hotbar.1 with bonded:scrap");

      let screen = await ctx.client.openInventory();
      const inventorySlots = screen.menu().slots();
      const bookSlot = inventorySlots.find((slot) => menuItemId(slot.item) === "minecraft:book");
      const scrapSlot = inventorySlots.find((slot) => menuItemId(slot.item) === "bonded:scrap");
      if (!bookSlot || !scrapSlot) {
        throw new Error(`Expected guide ingredients in the inventory: ${JSON.stringify(inventorySlots)}`);
      }

      screen = await screen.menu().slot(bookSlot.slot).click({ clickType: "PICKUP" });
      screen = await screen.menu().slot(1).click({ clickType: "PICKUP" });
      screen = await screen.menu().slot(scrapSlot.slot).click({ clickType: "PICKUP" });
      await screen.menu().slot(2).click({ clickType: "PICKUP" });
      await ctx.runtime.wait(150);

      screen = await ctx.client.screen();
      const craftedSlot = screen.menu().slots().find((slot) => menuItemId(slot.item) === "patchouli:guide_book");
      if (!craftedSlot) {
        throw new Error(`Expected the Bonded Field Guide in the crafting result: ${JSON.stringify(screen.menu().slots())}`);
      }

      await screen.menu().slot(craftedSlot.slot).click({ clickType: "QUICK_MOVE" });
      await ctx.runtime.wait(150);
      await ctx.client.closeMenus();

      const inventory = await ctx.player.inventory();
      const guide = inventory.items.find((item) => menuItemId(item) === "patchouli:guide_book");
      if (!guide || guide.slot === undefined || guide.slot < 0 || guide.slot > 8) {
        throw new Error(`Expected the crafted Bonded Field Guide in the hotbar: ${JSON.stringify(inventory)}`);
      }

      await ctx.client.openInventory();
      await ctx.client.screenshot("bonded-patchouli-field-guide-item");
      await ctx.client.closeMenus();

      await ctx.player.inventory().selectHotbar(guide.slot);
      try {
        await ctx.player.useItem({ hand: "main_hand" });

        let book = await ctx.client.screen();
        await expect(async () => {
          book = await ctx.client.screen();
          return book.screenClass?.startsWith("vazkii.patchouli.client.book.gui.GuiBook") ?? false;
        }).toEventuallyEqual(true, { timeout: "10s" });
        await expect(book).toHaveTitleLike(/bonded field guide/i);

        const categoryLabels = book.widgets().all().map((widget) => widget.label);
        expect(categoryLabels).toEqual(expect.arrayContaining(["Bonding", "Recipes", "Augments"]));
        await ctx.runtime.wait(5_000);
        await ctx.client.screenshot("bonded-patchouli-field-guide");

        await ctx.client.closeMenus();
        await ctx.commands.assert(
          "/open-patchouli-book @s bonded:field_guide bonded:augments/cake_destroyer 0",
        );
        await ctx.runtime.wait(200);
        book = await ctx.client.screen();
        expect(book.screenClass?.startsWith("vazkii.patchouli.client.book.gui.GuiBookEntry")).toBe(true);
        await ctx.client.screenshot("bonded-patchouli-cake-destroyer");

        await ctx.client.closeMenus();
        await ctx.commands.assert(
          "/open-patchouli-book @s bonded:field_guide bonded:recipes/workstations 0",
        );
        await ctx.runtime.wait(200);
        book = await ctx.client.screen();
        expect(book.screenClass?.startsWith("vazkii.patchouli.client.book.gui.GuiBookEntry")).toBe(true);
        await ctx.client.screenshot("bonded-patchouli-workstations");
      } finally {
        await ctx.client.closeMenus();
      }
    },
  );
});

function menuItemId(item: unknown): string | undefined {
  if (!item || typeof item !== "object") {
    return undefined;
  }

  const stack = item as { id?: unknown; itemId?: unknown };
  if (typeof stack.id === "string") {
    return stack.id;
  }

  return typeof stack.itemId === "string" ? stack.itemId : undefined;
}
