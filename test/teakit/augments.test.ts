import { Capability, Readiness, describe, expect, test } from "@teakit/test";
import type { TeaKitTestContext } from "@teakit/test";

const SCOREBOARD = "bonded_aug";

describe.configure({
  timeout: "3m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.PlayerInteractions,
    Capability.PlayerReset,
    Capability.PlayerTeleport,
    Capability.RuntimeTiming,
    Capability.ServerCommands,
    Capability.WorldBlock,
    Capability.WorldClear,
    Capability.WorldEntities,
    Capability.WorldFill,
    Capability.WorldPathing,
    Capability.WorldSetBlock,
  ],
});

describe("Bonded augments", () => {
  test("activates Cake Destroyer from cake blocks and drops sugar on melee damage", async (ctx) => {
    const mobCount = 300;

    try {
      await prepare(ctx);
      await ctx.commands.batch([
        "/gamerule maxEntityCramming 0",
        "/item replace entity @s weapon.mainhand with minecraft:iron_sword",
        "/bonded augment set @s bonded:cake_destroyer 98",
        "/setblock 0 71 0 minecraft:cake",
        "/setblock 1 71 0 minecraft:candle_cake",
        "/tp @s 0 72 -1",
      ]);

      await ctx.player.mine({ x: 0, y: 71, z: 0 }, { timeoutMs: 5_000 });
      await expectAugmentProgress(ctx, "bonded:cake_destroyer", 99);
      await ctx.player.mine({ x: 1, y: 71, z: 0 }, { timeoutMs: 5_000 });
      await expectAugmentProgress(ctx, "bonded:cake_destroyer", 100);

      await ctx.commands.batch(
        Array.from(
          { length: mobCount },
          () => "/summon minecraft:zombie 4 71 0 {NoAI:1b,Silent:1b}",
        ),
      );
      await ctx.commands.assert(
        "/execute as @e[type=minecraft:zombie,distance=..16] run damage @s 3 minecraft:player_attack by @p",
      );
      await expect(async () => !await commandFails(
        ctx,
        "/execute if items entity @e[type=minecraft:item,distance=..16] contents minecraft:sugar",
      )).toEventuallyEqual(true, { timeout: "5s", interval: "100ms" });
    } finally {
      await ctx.commands.run("/kill @e[type=minecraft:zombie,distance=..32]", { requireSuccess: false });
      await ctx.commands.run("/kill @e[type=minecraft:item,distance=..32]", { requireSuccess: false });
      await ctx.commands.run("/gamerule maxEntityCramming 24", { requireSuccess: false });
      await cleanup(ctx);
    }
  });

  test("activates Oceanic underwater and applies its swimming modifier", async (ctx) => {
    try {
      await prepare(ctx);
      await ctx.world.pool(
        { x: 8, y: 71, z: -2 },
        { size: 13, depth: 3, fluid: "minecraft:water" },
      );
      await ctx.commands.batch([
        "/item replace entity @s weapon.mainhand with minecraft:iron_leggings",
        "/bonded augment set @s bonded:oceanic 7499",
        "/item replace entity @s armor.legs from entity @s weapon.mainhand",
        "/item replace entity @s weapon.mainhand with minecraft:air",
        "/tp @s 9 69 0",
      ]);

      await ctx.runtime.wait(750);
      await ctx.commands.assert(
        "/item replace entity @s weapon.mainhand from entity @s armor.legs",
      );
      await expectAugmentProgress(ctx, "bonded:oceanic", 7_500);
      await ctx.commands.assert(
        "/item replace entity @s armor.legs from entity @s weapon.mainhand",
      );
      await ctx.commands.assert("/item replace entity @s weapon.mainhand with minecraft:air");

      await ctx.commands.assert("/bondeddebug swimming start");
      await expect(async () => await attributeValue(ctx))
        .toEventuallyEqual(1_000, { timeout: "10s", interval: "100ms" });

      await ctx.commands.assert("/bondeddebug swimming stop");
      await ctx.player.teleport({ x: 0, y: 72, z: -1 });
      await expect(async () => await attributeValue(ctx))
        .toEventuallyEqual(0, { timeout: "5s", interval: "100ms" });
    } finally {
      await ctx.commands.run("/bondeddebug swimming stop", { requireSuccess: false });
      await cleanup(ctx);
    }
  });
});

async function prepare(ctx: TeaKitTestContext): Promise<void> {
  await ctx.player.reset({ gameMode: "survival", inventory: "clear" });
  await ctx.world.clear({ x: -3, y: 69, z: -3 }, { x: 22, y: 75, z: 3 });
  await ctx.world.fill({ x: -3, y: 69, z: -3 }, { x: 22, y: 70, z: 3 }, "minecraft:stone");
  await ctx.commands.run(`/scoreboard objectives remove ${SCOREBOARD}`, { requireSuccess: false });
  await ctx.commands.assert(`/scoreboard objectives add ${SCOREBOARD} dummy`);
}

async function cleanup(ctx: TeaKitTestContext): Promise<void> {
  await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
  await ctx.world.clear({ x: -3, y: 69, z: -3 }, { x: 22, y: 75, z: 3 });
  await ctx.commands.run(`/scoreboard objectives remove ${SCOREBOARD}`, { requireSuccess: false });
}

async function expectAugmentProgress(
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

async function attributeValue(ctx: TeaKitTestContext): Promise<number> {
  await ctx.commands.assert(
    `/execute store result score #attribute ${SCOREBOARD} run attribute @s minecraft:water_movement_efficiency get 1000`,
  );
  const active = !await commandFails(
    ctx,
    `/execute if score #attribute ${SCOREBOARD} matches 1000`,
  );
  if (active) {
    return 1_000;
  }
  const inactive = !await commandFails(
    ctx,
    `/execute if score #attribute ${SCOREBOARD} matches 0`,
  );
  return inactive ? 0 : Number.NaN;
}

async function commandFails(ctx: TeaKitTestContext, command: string): Promise<boolean> {
  try {
    await ctx.commands.assert(command);
    return false;
  } catch {
    return true;
  }
}
