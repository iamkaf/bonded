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
    "exposes the guide recipe only when Modonomicon is loaded",
    {
      target: {
        minecraft: ">=1.21.1",
        loader: ["fabric", "forge", "neoforge"],
      },
    },
    async ({ commands, runtime }) => {
      // Crafting in an earlier run may have unlocked it already.
      await commands.run("/recipe take @s bonded:modonomicon_field_guide", { requireSuccess: false });
      const recipe = commands.run("/recipe give @s bonded:modonomicon_field_guide", {
        captureOutput: true,
        requireSuccess: true,
      });
      if (await runtime.mods.isLoaded("modonomicon")) {
        await recipe;
        await commands.run("/recipe take @s bonded:modonomicon_field_guide");
      } else {
        await expect(recipe).rejects.toThrow();
      }
    },
  );

  test(
    "crafts and opens the Bonded field guide with Modonomicon 1.x",
    {
      target: {
        minecraft: ">=1.21.1 <1.22",
        loader: ["fabric", "forge", "neoforge"],
        mods: "modonomicon",
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
      const guide = await heldGuide(ctx);

      try {
        await ctx.player.inventory().selectHotbar(guide);
        await ctx.player.useItem({ hand: "main_hand" });
        await waitForBook(ctx);
        // The book reopens where it was left, so step back to the index first.
        await showIndex(ctx);
        let book = await ctx.client.screen();
        await expect(book).toHaveTitleLike(/bonded field guide/i);

        await ctx.runtime.wait(1_000);
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

  // The 1.21.1 line replaces these entries, so each one has to load and open on its own.
  test(
    "opens the entries the 1.21.1 field guide overrides",
    {
      target: {
        minecraft: ">=1.21.1 <1.22",
        loader: ["fabric", "forge", "neoforge"],
        mods: "modonomicon",
      },
    },
    async (ctx) => {
      await closeScreenStack(ctx);
      await ctx.commands.run("/gamemode creative");
      await ctx.commands.run("/clear @s");
      await ctx.commands.assert(
        '/give @s modonomicon:modonomicon[modonomicon:book_id="bonded:field_guide"]',
      );
      const guide = await heldGuide(ctx);

      try {
        await ctx.player.inventory().selectHotbar(guide);
        await ctx.player.useItem({ hand: "main_hand" });
        await waitForBook(ctx);
        await ctx.runtime.wait(1_500);

        for (const [category, entry] of [
          ["Bonding", "Growing Together"],
          ["Augments", "Cake Destroyer"],
          ["Augments", "Oceanic"],
        ] as const) {
          // A right click steps back one screen: entry, category, then the index with the categories.
          for (let attempt = 0; attempt < 4 && !(await labels(ctx)).includes(category); attempt++) {
            await ctx.client.click({ x: 200, y: 120, button: 1 });
            await ctx.runtime.wait(400);
          }
          let book = await ctx.client.screen();
          if (!(await labels(ctx)).includes(category)) {
            throw new Error(
              `Expected the ${category} category; the screen is ${book.screenClass}: ${(await labels(ctx)).join(", ")}`,
            );
          }
          await book.widgets().find(category).click();
          await ctx.runtime.wait(300);
          expect(await labels(ctx)).toContain(entry);

          book = await ctx.client.screen();
          await book.widgets().find(entry).click();
          await ctx.runtime.wait(300);
          // An entry page has its page arrows and no entry list; the category list names every entry.
          const controls = await labels(ctx);
          if (!controls.includes("Next Page") || controls.includes(entry)) {
            throw new Error(`Expected the ${entry} entry page; saw ${controls.join(", ")}`);
          }
          await ctx.client.screenshot(`bonded-modonomicon-${entry.toLowerCase().replaceAll(" ", "-")}`);
        }
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

async function heldGuide(ctx: TeaKitTestContext): Promise<number> {
  await ctx.runtime.wait(500);
  const inventory = await ctx.player.inventory();
  const guide = inventory.items.find((item) => menuItemId(item) === "modonomicon:modonomicon");
  if (!guide || guide.slot === undefined || guide.slot < 0 || guide.slot > 8) {
    throw new Error(`Expected the Bonded Field Guide in the hotbar: ${JSON.stringify(inventory)}`);
  }
  return guide.slot;
}

/** Steps back with right clicks until the category index shows, reopening the book if it closed. */
async function showIndex(ctx: TeaKitTestContext): Promise<void> {
  await ctx.runtime.wait(1_000);
  for (let attempt = 0; attempt < 5 && !(await labels(ctx)).includes("Bonding"); attempt++) {
    if ((await ctx.client.screen()).open) {
      await ctx.client.click({ x: 200, y: 120, button: 1 });
    } else {
      await ctx.player.useItem({ hand: "main_hand" });
    }
    await ctx.runtime.wait(500);
  }
}

async function labels(ctx: TeaKitTestContext): Promise<string[]> {
  return (await ctx.client.screen()).widgets().all().map((widget) => widget.label);
}

async function waitForBook(ctx: TeaKitTestContext) {
  let book = await ctx.client.screen();
  await expect(async () => {
    book = await ctx.client.screen();
    return book.screenClass?.startsWith("com.klikli_dev.modonomicon.") ?? false;
  }).toEventuallyEqual(true, { timeout: "10s" }).catch((error: unknown) => {
    throw new Error(`Expected the field guide to open; the screen is ${book.screenClass}: ${String(error)}`);
  });
  return book;
}

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
