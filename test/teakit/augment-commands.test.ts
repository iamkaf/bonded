import { Capability, Readiness, describe, expect, test } from "@teakit/test";
import type { TeaKitTestContext } from "@teakit/test";

const SCOREBOARD = "bonded_aug_cmd";
const CHEST = { x: 0, y: 71, z: 0 };
const RESTORED_CHEST = { x: 1, y: 71, z: 0 };

describe.configure({
  timeout: "2m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.PlayerReset,
    Capability.ServerCommands,
    Capability.WorldClear,
    Capability.WorldFill,
    Capability.WorldSetBlock,
  ],
});

describe("Bonded augment commands", () => {
  test("manipulates held-item progress through the bonded command", async (ctx) => {
    try {
      await prepare(ctx);
      await ctx.commands.assert("/bonded");
      await ctx.commands.assert(
        "/item replace entity @s weapon.mainhand with minecraft:iron_sword",
      );

      await ctx.commands.assert("/bonded augment set @s bonded:cake_destroyer 40");
      await expectProgress(ctx, "bonded:cake_destroyer", 40);
      await ctx.commands.assert("/bonded augment add @s bonded:cake_destroyer 5");
      await expectProgress(ctx, "bonded:cake_destroyer", 45);
      await ctx.commands.assert(
        "/bonded augment add @s bonded:cake_destroyer 2147483647",
      );
      await expectProgress(ctx, "bonded:cake_destroyer", 100);

      expect(await commandFails(
        ctx,
        "/bonded augment set @s bonded:cake_destroyer 100",
      )).toBe(true);

      await ctx.commands.assert("/bonded augment set @s bonded:cake_destroyer 0");
      await expectProgress(ctx, "bonded:cake_destroyer", 0);
    } finally {
      await cleanup(ctx);
    }
  });

  test("rejects malformed or inapplicable mutations", async (ctx) => {
    try {
      await prepare(ctx);

      expect(await commandFails(
        ctx,
        "/augment set @s bonded:cake_destroyer 1",
      )).toBe(true);
      expect(await commandFails(
        ctx,
        "/bonded augment set @s bonded:cake_destroyer 1",
      )).toBe(true);

      await ctx.commands.assert(
        "/item replace entity @s weapon.mainhand with minecraft:iron_sword",
      );
      expect(await commandFails(
        ctx,
        "/bonded augment set @s bonded:missing 1",
      )).toBe(true);
      expect(await commandFails(
        ctx,
        "/bonded augment add @s bonded:cake_destroyer -1",
      )).toBe(true);
      expect(await commandFails(
        ctx,
        "/bonded augment add @s bonded:oceanic 1",
      )).toBe(true);
      await expectProgress(ctx, "bonded:oceanic", 0);
    } finally {
      await cleanup(ctx);
    }
  });

  test("preserves known and unknown progress through serialized storage", async (ctx) => {
    try {
      await prepare(ctx);
      await ctx.commands.batch([
        `/setblock ${CHEST.x} ${CHEST.y} ${CHEST.z} minecraft:chest`,
        "/item replace entity @s weapon.mainhand with minecraft:iron_sword[bonded:augment_progress={progress:{\"example:unknown\":7}}]",
        "/bonded augment set @s bonded:cake_destroyer 63",
        `/item replace block ${CHEST.x} ${CHEST.y} ${CHEST.z} container.0 from entity @s weapon.mainhand`,
        `/clone ${CHEST.x} ${CHEST.y} ${CHEST.z} ${CHEST.x} ${CHEST.y} ${CHEST.z} ${RESTORED_CHEST.x} ${RESTORED_CHEST.y} ${RESTORED_CHEST.z} replace`,
        "/clear @s",
        `/item replace entity @s weapon.mainhand from block ${RESTORED_CHEST.x} ${RESTORED_CHEST.y} ${RESTORED_CHEST.z} container.0`,
      ]);

      await expectProgress(ctx, "bonded:cake_destroyer", 63);
      await ctx.commands.assert(
        "/execute if items entity @s weapon.mainhand minecraft:iron_sword[bonded:augment_progress~{progress:{\"example:unknown\":7}}]",
      );
    } finally {
      await cleanup(ctx);
    }
  });
});

async function prepare(ctx: TeaKitTestContext): Promise<void> {
  await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
  await ctx.world.clear({ x: -2, y: 69, z: -2 }, { x: 2, y: 74, z: 2 });
  await ctx.world.fill({ x: -2, y: 69, z: -2 }, { x: 2, y: 70, z: 2 }, "minecraft:stone");
  await ctx.commands.run(`/scoreboard objectives remove ${SCOREBOARD}`, { requireSuccess: false });
  await ctx.commands.assert(`/scoreboard objectives add ${SCOREBOARD} dummy`);
}

async function cleanup(ctx: TeaKitTestContext): Promise<void> {
  await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
  await ctx.world.clear({ x: -2, y: 69, z: -2 }, { x: 2, y: 74, z: 2 });
  await ctx.commands.run(`/scoreboard objectives remove ${SCOREBOARD}`, { requireSuccess: false });
}

async function expectProgress(
  ctx: TeaKitTestContext,
  augment: string,
  expected: number,
): Promise<void> {
  await ctx.commands.assert(
    `/execute store result score #actual ${SCOREBOARD} run bonded augment query @s ${augment}`,
  );
  await ctx.commands.assert(
    `/execute if score #actual ${SCOREBOARD} matches ${expected}`,
  );
}

async function commandFails(ctx: TeaKitTestContext, command: string): Promise<boolean> {
  try {
    await ctx.commands.assert(command);
    return false;
  } catch {
    return true;
  }
}
