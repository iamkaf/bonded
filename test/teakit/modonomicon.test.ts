import { Capability, Readiness, describe, expect, test } from "@teakit/test";
import type { TeaKitTestContext } from "@teakit/test";

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

describe("Modonomicon field guide", () => {
  test(
    "does not expose the guide recipe without Modonomicon",
    {
      target: {
        minecraft: ">=1.21.11",
        loader: ["forge", "neoforge"],
      },
    },
    async ({ commands }) => {
      await expect(
        commands.run("/recipe give @s bonded:modonomicon_field_guide", {
          captureOutput: true,
          requireSuccess: true,
        }),
      ).rejects.toThrow();
    },
  );

  test(
    "crafts and opens the Bonded field guide with Modonomicon 1.x",
    {
      target: {
        minecraft: "1.21.11",
        loader: ["fabric"],
      },
    },
    async (ctx) => {
      await closeScreenStack(ctx);
      await ctx.commands.run("/gamemode creative");
      await ctx.commands.run("/setblock 0 79 0 minecraft:stone");
      await ctx.commands.run("/tp @s 0 80 0");
      await ctx.commands.run("/clear @s");

      const recipe = await ctx.recipes.assertCrafting(
        2,
        1,
        ["minecraft:writable_book", "bonded:scrap"],
        "modonomicon:modonomicon",
      );
      expect(recipe.recipeId).toBe("bonded:modonomicon_field_guide");

      await ctx.commands.assert(
        '/give @s modonomicon:modonomicon[modonomicon:book_id="bonded:field_guide"]',
      );
      const inventory = await ctx.player.inventory();
      const guide = inventory.items.find((item) => menuItemId(item) === "modonomicon:modonomicon");
      if (!guide || guide.slot === undefined || guide.slot < 0 || guide.slot > 8) {
        throw new Error(`Expected the Bonded Field Guide in the hotbar: ${JSON.stringify(inventory)}`);
      }

      await ctx.player.inventory().selectHotbar(guide.slot);
      try {
        await ctx.player.useItem({ hand: "main_hand" });

        let book = await ctx.client.screen();
        await expect(async () => {
          book = await ctx.client.screen();
          return book.screenClass?.startsWith("com.klikli_dev.modonomicon.client.gui.book") ?? false;
        }).toEventuallyEqual(true, { timeout: "10s" });
        await expect(book).toHaveTitleLike(/bonded field guide/i);

        await ctx.runtime.wait(2_000);
        book = await ctx.client.screen();
        const controls = book.widgets().all().map((widget) => widget.label);
        expect(controls).toEqual(
          expect.arrayContaining(["Bonding", "Recipes", "Augments", "Previous Page", "Next Page", "Exit"]),
        );
        await ctx.client.screenshot("bonded-modonomicon-field-guide-1x");
      } finally {
        await closeScreenStack(ctx);
      }
    },
  );

  test(
    "crafts and opens the Bonded field guide",
    {
      target: {
        minecraft: ">=26.1.2",
        loader: ["fabric"],
      },
    },
    async (ctx) => {
      await closeScreenStack(ctx);
      await ctx.commands.run("/gamemode creative");
      await ctx.commands.run("/setblock 0 79 0 minecraft:stone");
      await ctx.commands.run("/tp @s 0 80 0");
      await ctx.commands.run("/clear @s");

      const recipe = await ctx.recipes.assertCrafting(
        2,
        1,
        ["minecraft:writable_book", "bonded:scrap"],
        "modonomicon:modonomicon",
      );
      expect(recipe.recipeId).toBe("bonded:modonomicon_field_guide");

      await ctx.commands.assert(
        '/give @s modonomicon:modonomicon[modonomicon:book_id="bonded:field_guide"]',
      );
      const inventory = await ctx.player.inventory();
      const guide = inventory.items.find((item) => menuItemId(item) === "modonomicon:modonomicon");
      if (!guide || guide.slot === undefined || guide.slot < 0 || guide.slot > 8) {
        throw new Error(`Expected the Bonded Field Guide in the hotbar: ${JSON.stringify(inventory)}`);
      }

      await ctx.player.inventory().selectHotbar(guide.slot);
      try {
        await ctx.player.useItem({ hand: "main_hand" });

        let book = await ctx.client.screen();
        await expect(async () => {
          book = await ctx.client.screen();
          return book.screenClass?.startsWith("com.klikli_dev.modonomicon.client.gui.book") ?? false;
        }).toEventuallyEqual(true, { timeout: "10s" });
        await expect(book).toHaveTitleLike(/bonded field guide/i);

        const categoryLabels = book.widgets().all().map((widget) => widget.label);
        expect(categoryLabels).toEqual(expect.arrayContaining(["Bonding", "Recipes", "Augments"]));
        await ctx.runtime.wait(2_000);
        await ctx.client.screenshot("bonded-modonomicon-field-guide");

        await book.widgets().find("Bonding").click();
        await ctx.runtime.wait(150);

        book = await ctx.client.screen();
        const bondingEntries = book.widgets().all().map((widget) => widget.label);
        expect(bondingEntries).toEqual(expect.arrayContaining(["Getting Attached", "Growing Together"]));
        await book.widgets().find("Getting Attached").click();
        await ctx.runtime.wait(150);

        book = await ctx.client.screen();
        const entryControls = book.widgets().all().map((widget) => widget.label);
        if (
          !book.screenClass?.endsWith("MultiLayerScreen") ||
          !entryControls.includes("Next Page")
        ) {
          const labels = book.widgets().all().map((widget) => widget.label).join(", ");
          throw new Error(`Expected the Getting Attached entry screen; observed ${book.screenClass}: ${labels}`);
        }
      } finally {
        await closeScreenStack(ctx);
      }
    },
  );
});

async function closeScreenStack(ctx: TeaKitTestContext): Promise<void> {
  for (let attempt = 0; attempt < 6; attempt++) {
    const screen = await ctx.client.screen();
    if (!screen.open) return;
    await ctx.client.key(256, { release: true });
    await ctx.runtime.wait(100);
  }

  const screen = await ctx.client.screen();
  throw new Error(`Modonomicon screen did not close during cleanup: ${screen.screenClass}`);
}

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
