import { Capability, Readiness, describe, expect, test } from "@teakit/test";
import type { ItemId, TeaKitTestContext } from "@teakit/test";

const TEST_MIN = { x: -2, y: 70, z: -2 };
const TEST_MAX = { x: 2, y: 74, z: 2 };
const BENCH = { x: 0, y: 71, z: 0 };

const REQUIRED_ITEMS = [
  "immersive_armors:bone_helmet",
  "betterend:terminite_hammer",
  "betterend:terminite_helmet",
  "betterend:terminite_forged_plate",
  "betterend:aeternium_helmet",
  "betterend:aeternium_forged_plate",
  "betternether:cincinnasite_hammer",
  "betternether:cincinnasite_hammer_diamond",
  "betternether:cincinnasite_excavator",
  "betternether:cincinnasite_excavator_diamond",
  "betternether:cincinnasite_pickaxe",
  "betternether:cincinnasite_pickaxe_diamond",
  "betternether:nether_ruby",
  "betternether:nether_ruby_hammer",
  "betternether:nether_ruby_excavator",
  "betternether:nether_ruby_sword",
] as const satisfies readonly ItemId[];

const CLASSIFICATION_CASES = [
  {
    item: "immersive_armors:bone_helmet",
    expected: ["type=armor", "repair=inherit", "source=Immersive Armors"],
  },
  {
    item: "betterend:terminite_hammer",
    expected: ["type=mining_tool", "repair=inherit", "source=BetterEnd"],
  },
  {
    item: "betterend:terminite_helmet",
    expected: [
      "type=armor",
      "repair=inherit",
      "upgrade=betterend:aeternium_helmet",
      "source=BetterEnd",
    ],
  },
  {
    item: "betternether:cincinnasite_pickaxe",
    expected: [
      "type=mining_tool",
      "repair=inherit",
      "upgrade=betternether:cincinnasite_pickaxe_diamond",
      "source=BetterNether",
    ],
  },
  {
    item: "betternether:nether_ruby_sword",
    expected: ["type=melee_weapon", "repair=inherit", "source=BetterNether"],
  },
] as const satisfies ReadonlyArray<{ item: ItemId; expected: readonly string[] }>;

const REPAIR_CASES = [
  {
    item: "immersive_armors:bone_helmet",
    ingredient: "minecraft:bone",
    marker: 2,
  },
  {
    item: "betterend:terminite_helmet",
    ingredient: "betterend:terminite_forged_plate",
    marker: 3,
  },
  {
    item: "betternether:nether_ruby_sword",
    ingredient: "betternether:nether_ruby",
    marker: 4,
  },
] as const satisfies ReadonlyArray<{ item: ItemId; ingredient: ItemId; marker: number }>;

const UPGRADE_CASES = [
  {
    item: "betterend:terminite_helmet",
    result: "betterend:aeternium_helmet",
    ingredient: "betterend:aeternium_forged_plate",
    marker: 5,
  },
  {
    item: "betternether:cincinnasite_pickaxe",
    result: "betternether:cincinnasite_pickaxe_diamond",
    ingredient: "minecraft:diamond",
    marker: 6,
  },
] as const satisfies ReadonlyArray<{
  item: ItemId;
  result: ItemId;
  ingredient: ItemId;
  marker: number;
}>;

describe.configure({
  timeout: "4m",
  tags: ["compat-fixture"],
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.PlayerInteractions,
    Capability.PlayerInventory,
    Capability.PlayerReset,
    Capability.PlayerTeleport,
    Capability.RegistryLookup,
    Capability.ServerCommands,
    Capability.WorldClear,
    Capability.WorldFill,
    Capability.WorldSetBlock,
  ],
});

describe("Bonded compatibility gear components", () => {
  test("loads every compatibility fixture used by the gameplay checks", {
    target: { minecraft: "26.1.2", loader: "fabric" },
  }, async ({ registry }) => {
    await expect(registry.missing([...REQUIRED_ITEMS])).resolves.toEqual([]);
  });

  test("classifies external armor and tools through their shipped profiles", {
    target: { minecraft: "26.1.2", loader: "fabric" },
  }, async (ctx) => {
    try {
      for (const fixture of CLASSIFICATION_CASES) {
        await expectHeldRule(ctx, fixture.item, fixture.expected);
      }
    } finally {
      await ctx.commands.run("/clear @s", { requireSuccess: false });
    }
  });

  test("repairs compatibility gear with its native material without dropping components", {
    target: { minecraft: "26.1.2", loader: "fabric" },
  }, async (ctx) => {
    try {
      await prepareBench(ctx, "bonded:repair_bench");
      for (const fixture of REPAIR_CASES) {
        await ctx.commands.run("/clear @s", { requireSuccess: false });
        await ctx.commands.assert(
          `/item replace entity @s weapon.mainhand with ${fixture.item}[minecraft:damage=1,immersive_armors:set_count=${fixture.marker}]`,
        );
        await ctx.commands.assert(`/item replace entity @s hotbar.1 with ${fixture.ingredient}`);

        await ctx.player.useBlockServer(BENCH, { face: "up", hand: "main_hand" });
        await ctx.player.inventory().waitForItem(fixture.item, {
          slot: 0,
          timeout: "5s",
        });
        await ctx.player.inventory().waitForItemAbsent(fixture.ingredient, { timeout: "5s" });
        await ctx.commands.assert(
          `/execute if items entity @s weapon.mainhand ${fixture.item}[minecraft:damage=0,immersive_armors:set_count=${fixture.marker}]`,
        );
      }
    } finally {
      await cleanup(ctx);
    }
  });

  test("upgrades external gear without dropping third-party components", {
    target: { minecraft: "26.1.2", loader: "fabric" },
  }, async (ctx) => {
    try {
      await prepareBench(ctx, "bonded:tool_bench");
      for (const fixture of UPGRADE_CASES) {
        await ctx.commands.run("/clear @s", { requireSuccess: false });
        await ctx.commands.assert(
          `/item replace entity @s weapon.mainhand with ${fixture.item}[minecraft:damage=7,immersive_armors:set_count=${fixture.marker},bonded:item_level={experience:0,maxExperience:1000,level:10,bond:${fixture.marker}}]`,
        );
        await ctx.commands.assert(`/item replace entity @s hotbar.1 with ${fixture.ingredient}`);

        await ctx.player.useBlockServer(BENCH, { face: "up", hand: "main_hand" });
        await ctx.player.inventory().waitForItem(fixture.result, {
          slot: 0,
          timeout: "5s",
        });
        await ctx.player.inventory().waitForItemAbsent(fixture.ingredient, { timeout: "5s" });
        await ctx.commands.assert(
          `/execute if items entity @s weapon.mainhand ${fixture.result}[minecraft:damage=7,immersive_armors:set_count=${fixture.marker},bonded:item_level~{bond:${fixture.marker}}]`,
        );
      }
    } finally {
      await cleanup(ctx);
    }
  });
});

async function prepareBench(
  ctx: TeaKitTestContext,
  bench: "bonded:repair_bench" | "bonded:tool_bench",
): Promise<void> {
  await ctx.player.reset({ gameMode: "survival", inventory: "clear" });
  await ctx.world.clear(TEST_MIN, TEST_MAX);
  await ctx.world.fill({ x: -2, y: 70, z: -2 }, { x: 2, y: 70, z: 2 }, "minecraft:stone");
  await ctx.world.setBlock(BENCH, bench);
  await ctx.player.teleport({ x: 0, y: 72, z: -1 });
}

async function cleanup(ctx: TeaKitTestContext): Promise<void> {
  await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
  await ctx.world.clear(TEST_MIN, TEST_MAX);
}

async function expectHeldRule(
  ctx: TeaKitTestContext,
  item: ItemId,
  expectedOutput: readonly string[],
): Promise<void> {
  await ctx.commands.assert(`/item replace entity @s weapon.mainhand with ${item}`);
  await ctx.commands.run("/bondeddebug rules query", {
    captureOutput: true,
    expectOutputContains: [...expectedOutput],
    requireSuccess: true,
  });
}
