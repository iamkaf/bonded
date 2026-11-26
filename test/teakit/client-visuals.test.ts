import { Capability, Readiness, describe, expect, test } from "@teakit/test";
import type { BlockPos, TeaKitTestContext } from "@teakit/test";

const BENCH: BlockPos = { x: 0, y: 71, z: 2 };
const LEVELED = "bonded:item_level={experience:5,maxExperience:100,level:3,bond:42}";

describe.configure({
  timeout: "5m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.ClientInput,
    Capability.ClientScreen,
    Capability.ClientScreens,
    Capability.ClientScreenshot,
    Capability.PlayerInventory,
    Capability.PlayerReset,
    Capability.PlayerTeleport,
    Capability.RuntimeLogs,
    Capability.RuntimeTiming,
    Capability.ServerCommands,
    Capability.WorldBlock,
  ],
});

// Tooltip, durability bar, and bench preview drawing has no readable output, so each test draws the
// state, captures it for inspection, and fails if the render path logged an error while doing so.
describe("Bonded client visuals", () => {
  test("draws the bond level, experience, and augment lines in an item tooltip", async (ctx) => {
    const errors = await errorCount(ctx);
    try {
      await ctx.player.reset({ gameMode: "survival", inventory: "clear" });
      await ctx.commands.assert(`/item replace entity @s hotbar.0 with minecraft:iron_pickaxe[${LEVELED}]`);
      await ctx.commands.assert("/bonded augment set @s bonded:cake_destroyer 40");

      await hoverHotbarSlot(ctx, 0);
      await ctx.runtime.wait(500);
      await ctx.client.screenshot("bonded-item-tooltip", { hideOverlay: false });
    } finally {
      await ctx.client.closeMenus();
      await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
    }
    expect(await errorCount(ctx), "errors logged while drawing the tooltip").toBe(errors);
  });

  test("draws the over-repair bar on a damaged over-repaired item", async (ctx) => {
    const errors = await errorCount(ctx);
    try {
      await ctx.player.reset({ gameMode: "survival", inventory: "clear" });
      await ctx.world.fill({ x: -3, y: 70, z: -3 }, { x: 5, y: 70, z: 5 }, "minecraft:stone");
      await ctx.world.fill({ x: -3, y: 71, z: -3 }, { x: 5, y: 76, z: 5 }, "minecraft:air");
      await ctx.player.teleport({ x: 2.5, y: 71, z: -1.5 });
      await ctx.world.setBlock({ x: 0, y: 71, z: 0 }, "bonded:repair_bench");
      await ctx.commands.assert("/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=49]");
      await ctx.commands.assert("/item replace entity @s hotbar.1 with minecraft:iron_ingot 3");
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.commands.assert(
        "/execute if items entity @s weapon.mainhand minecraft:iron_shovel[minecraft:damage=0]",
      );
      // Wear the shovel down to half, keeping the over-repair it just gained.
      await ctx.commands.assert(
        '/item modify entity @s weapon.mainhand {"function":"minecraft:set_damage","damage":0.5}',
      );
      await ctx.commands.assert(
        "/execute unless items entity @s weapon.mainhand minecraft:iron_shovel[minecraft:damage=0]",
      );

      await ctx.client.openInventory();
      await ctx.client.waitForFrames(5);
      await ctx.client.screenshot("bonded-over-repair-bar", { hideOverlay: false });
    } finally {
      await ctx.client.closeMenus();
      await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
      await ctx.world.clear({ x: -1, y: 71, z: -1 }, { x: 3, y: 73, z: 3 });
    }
    expect(await errorCount(ctx), "errors logged while drawing the durability bar").toBe(errors);
  });

  test("previews the repair material and the upgrade on the benches", async (ctx) => {
    const errors = await errorCount(ctx);
    try {
      await ctx.player.reset({ gameMode: "survival", inventory: "clear" });
      await ctx.world.fill({ x: -3, y: 70, z: -3 }, { x: 5, y: 70, z: 5 }, "minecraft:stone");
      await ctx.world.fill({ x: -3, y: 71, z: -3 }, { x: 5, y: 76, z: 5 }, "minecraft:air");
      await ctx.player.teleport({ x: 0.5, y: 71, z: 0.5 });
      await ctx.world.setBlock(BENCH, "bonded:repair_bench");
      await ctx.commands.assert(
        `/item replace entity @s weapon.mainhand with minecraft:iron_pickaxe[minecraft:damage=40,${LEVELED}]`,
      );
      await ctx.commands.assert("/item replace entity @s hotbar.1 with minecraft:iron_ingot 4");
      await ctx.player.inventory().selectHotbar(0);
      await ctx.client.lookAt({ x: BENCH.x + 0.5, y: BENCH.y + 0.5, z: BENCH.z + 0.5 });
      await ctx.client.waitForFrames(10);
      await ctx.client.screenshot("bonded-repair-bench-hud", { hideOverlay: false });

      await ctx.world.setBlock(BENCH, "bonded:tool_bench");
      await ctx.client.lookAt({ x: BENCH.x + 0.5, y: BENCH.y + 0.5, z: BENCH.z + 0.5 });
      await ctx.client.waitForFrames(10);
      await ctx.client.screenshot("bonded-tool-bench-hud", { hideOverlay: false });
    } finally {
      await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
      await ctx.world.clear(BENCH, BENCH);
    }
    expect(await errorCount(ctx), "errors logged while drawing the bench previews").toBe(errors);
  });
});

async function errorCount(ctx: TeaKitTestContext): Promise<number> {
  return (await ctx.logs.entries({ levelAtLeast: "ERROR" })).length;
}

/** Opens the inventory and rests the cursor over a hotbar slot so its tooltip draws. */
async function hoverHotbarSlot(ctx: TeaKitTestContext, hotbarIndex: number): Promise<void> {
  const screen = await ctx.client.openInventory();
  const slot = screen.menu().slots().find((entry) => entry.slot === 36 + hotbarIndex);
  if (!slot || slot.x === undefined || slot.y === undefined) {
    throw new Error(`Expected hotbar slot ${hotbarIndex} in the inventory: ${JSON.stringify(screen.menu().slots())}`);
  }
  await ctx.client.scroll({ x: slot.x + 8, y: slot.y + 8, verticalAmount: 0 });
  await ctx.client.waitForFrames(5);
}
