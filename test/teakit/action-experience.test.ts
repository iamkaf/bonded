import { Capability, Readiness, describe, test } from "@teakit/test";
import type { BlockPos, ItemId, TeaKitTestContext } from "@teakit/test";

const LEVEL_ONE = "bonded:item_level={experience:0,maxExperience:1000,level:1,bond:0}";
const TARGET: BlockPos = { x: 1, y: 71, z: 0 };

describe.configure({
  timeout: "5m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.ClientInput,
    Capability.PlayerInteractions,
    Capability.PlayerReset,
    Capability.PlayerTeleport,
    Capability.RuntimeTiming,
    Capability.ServerCommands,
    Capability.WorldBlock,
    Capability.WorldEntities,
  ],
});

interface ActionCase {
  name: string;
  item: ItemId;
  experience: number;
  run: (ctx: TeaKitTestContext) => Promise<void>;
}

describe("Bonded action experience", () => {
  test("awards experience for tilling, pathing, lighting, shearing blocks, and detonating", async (ctx) => {
    const cases: ActionCase[] = [
      {
        name: "hoe tills dirt",
        item: "minecraft:iron_hoe",
        experience: 1,
        run: async ({ player, world }) => {
          await world.setBlock(TARGET, "minecraft:dirt");
          await player.useBlockServer(TARGET, { face: "up", hand: "main_hand" });
        },
      },
      {
        name: "shovel flattens grass",
        item: "minecraft:iron_shovel",
        experience: 1,
        run: async ({ player, world }) => {
          await world.setBlock(TARGET, "minecraft:grass_block");
          await player.useBlockServer(TARGET, { face: "up", hand: "main_hand" });
        },
      },
      {
        name: "axe strips a log",
        item: "minecraft:iron_axe",
        experience: 1,
        run: async ({ commands, player }) => {
          await commands.assert(`/setblock ${TARGET.x} ${TARGET.y} ${TARGET.z} minecraft:oak_log`);
          await player.useBlockServer(TARGET, { face: "up", hand: "main_hand" });
          await commands.assert(
            `/execute if block ${TARGET.x} ${TARGET.y} ${TARGET.z} minecraft:stripped_oak_log`,
          );
        },
      },
      {
        name: "flint and steel lights a fire",
        item: "minecraft:flint_and_steel",
        experience: 1,
        run: async ({ player, world }) => {
          await world.setBlock(TARGET, "minecraft:stone");
          await player.useBlockServer(TARGET, { face: "up", hand: "main_hand" });
        },
      },
      {
        name: "flint and steel ignites TNT",
        item: "minecraft:flint_and_steel",
        experience: 10,
        run: async ({ commands, player, world }) => {
          await world.setBlock(TARGET, "minecraft:tnt");
          await player.useBlockServer(TARGET, { face: "up", hand: "main_hand" });
          await commands.run("/kill @e[type=minecraft:tnt]", { requireSuccess: false });
        },
      },
      {
        name: "shears carve a pumpkin",
        item: "minecraft:shears",
        experience: 10,
        run: async ({ player, world }) => {
          await world.setBlock(TARGET, "minecraft:pumpkin");
          await player.useBlockServer(TARGET, { face: "up", hand: "main_hand" });
        },
      },
      {
        name: "shears harvest a full beehive",
        item: "minecraft:shears",
        experience: 10,
        run: async ({ commands, player }) => {
          await commands.assert(`/setblock ${TARGET.x} ${TARGET.y} ${TARGET.z} minecraft:beehive[honey_level=5]`);
          await commands.assert(
            `/execute if block ${TARGET.x} ${TARGET.y} ${TARGET.z} minecraft:beehive[honey_level=5]`,
          );
          await player.useBlockServer(TARGET, { face: "up", hand: "main_hand" });
        },
      },
    ];

    await runCases(ctx, "creative", cases);
  });

  test("awards experience for shearing and igniting mobs and brushing an armadillo", async (ctx) => {
    const interact =
      (type: string, nbt = "") =>
      async ({ commands, entities, player }: TeaKitTestContext) => {
        const at = { x: TARGET.x + 0.5, y: TARGET.y, z: TARGET.z + 0.5 };
        await commands.assert(`/summon ${type} ${at.x} ${at.y} ${at.z} {NoAI:1b,Silent:1b,Tags:["bonded_target"]${nbt}}`);
        const target = await entities.nearest(type, at);
        if (!target) {
          throw new Error(`Expected a ${type} to interact with`);
        }
        await player.useItemOnEntity(target, undefined, { hand: "main_hand" });
      };
    const cases: ActionCase[] = [
      { name: "shears shear a sheep", item: "minecraft:shears", experience: 10, run: interact("minecraft:sheep") },
      {
        name: "shears shear a mooshroom",
        item: "minecraft:shears",
        experience: 50,
        run: interact("minecraft:mooshroom"),
      },
      {
        name: "shears take a snow golem's pumpkin",
        item: "minecraft:shears",
        experience: 10,
        run: interact("minecraft:snow_golem"),
      },
      {
        name: "flint and steel ignite a creeper",
        item: "minecraft:flint_and_steel",
        experience: 50,
        run: interact("minecraft:creeper", ",ExplosionRadius:0b"),
      },
      {
        name: "brush an armadillo",
        item: "minecraft:brush",
        experience: 25,
        run: interact("minecraft:armadillo"),
      },
    ];

    await runCases(ctx, "creative", cases);
  });

  test("awards experience for breaking a block with shears as a survival player", async (ctx) => {
    const cases: ActionCase[] = [
      {
        // One point for breaking a block the shears are right for, one from the shears hook.
        name: "shears break a cobweb",
        item: "minecraft:shears",
        experience: 2,
        run: async ({ commands, player }) => {
          await commands.assert(`/setblock ${TARGET.x} ${TARGET.y} ${TARGET.z} minecraft:cobweb`);
          // The block breaks so fast that the player driver can report it before the server shows air.
          await player.mine(TARGET, { timeoutMs: 5_000 }).catch((error: unknown) => {
            if (!String(error).includes("before the authoritative server observed air")) {
              throw error;
            }
          });
        },
      },
    ];

    await runCases(ctx, "survival", cases);
  });

  test("awards experience for completing a brush on suspicious sand", async (ctx) => {
    const cases: ActionCase[] = [
      {
        name: "brush suspicious sand",
        item: "minecraft:brush",
        experience: 25,
        run: async ({ client, commands, player, runtime, world }) => {
          await world.setBlock(TARGET, "minecraft:suspicious_sand");
          const center = { x: TARGET.x + 0.5, y: TARGET.y + 1, z: TARGET.z + 0.5 };
          await client.lookAt(center);
          await client.waitForFrames(10);
          await runtime.wait(500);
          await player.holdUse(true);
          try {
            // Brushing takes ten strokes, one every half second.
            await runtime.wait(8_000);
          } finally {
            await player.holdUse(false);
          }
          // Brushing to the end turns the block into plain sand.
          await commands.assert(`/execute if block ${TARGET.x} ${TARGET.y} ${TARGET.z} minecraft:sand`);
        },
      },
    ];

    await runCases(ctx, "survival", cases);
  });

  test("awards experience for landing a catch with a fishing rod", async (ctx) => {
    const water = { from: { x: 0, y: 70, z: 3 }, to: { x: 2, y: 70, z: 5 } };
    const cases: ActionCase[] = [
      {
        name: "fishing rod lands a catch",
        item: "minecraft:fishing_rod",
        experience: 10,
        run: async ({ client, player, runtime, world }) => {
          await world.fill(water.from, water.to, "minecraft:water");
          await player.teleport({ x: 1.5, y: 71, z: 0.5 });
          await client.lookAt({ x: 1.5, y: 70.5, z: 4.5 });
          await player.useItem({ hand: "main_hand" });
          await runtime.wait(1_000);
          await player.forceFishingBite();
          await runtime.wait(300);
          await player.useItem({ hand: "main_hand" });
          await runtime.wait(500);
        },
      },
    ];

    try {
      await runCases(ctx, "survival", cases);
    } finally {
      await ctx.world.fill(water.from, water.to, "minecraft:stone");
    }
  });
});

async function runCases(
  ctx: TeaKitTestContext,
  gameMode: "creative" | "survival",
  cases: readonly ActionCase[],
): Promise<void> {
  const failures: string[] = [];
  try {
    await ctx.commands.run("/scoreboard objectives remove bonded_action_xp", { requireSuccess: false });
    await ctx.commands.assert("/scoreboard objectives add bonded_action_xp dummy");
    await ctx.commands.assert("/bondeddebug critical-chance 0");
    for (const action of cases) {
      try {
        await prepare(ctx, gameMode, action.item);
        await action.run(ctx);
        await ctx.runtime.wait(200);
        await expectExperience(ctx, action.item, action.experience);
      } catch (error) {
        failures.push(`${action.name}: ${error instanceof Error ? error.message : String(error)}\n  held: ${await heldItem(ctx)}`);
      }
    }
  } finally {
    await ctx.commands.run("/scoreboard objectives remove bonded_action_xp", { requireSuccess: false });
    await ctx.commands.run("/bondeddebug critical-chance 0", { requireSuccess: false });
    await ctx.commands.run("/kill @e[tag=bonded_target]", { requireSuccess: false });
    await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
    await ctx.world.fill({ x: -3, y: 71, z: -3 }, { x: 9, y: 76, z: 6 }, "minecraft:air");
  }
  if (failures.length > 0) {
    throw new Error(`Expected every action to award its experience:\n${failures.join("\n")}`);
  }
}

async function prepare(
  ctx: TeaKitTestContext,
  gameMode: "creative" | "survival",
  item: ItemId,
): Promise<void> {
  await ctx.commands.run("/kill @e[type=!minecraft:player,distance=..30]", { requireSuccess: false });
  await ctx.player.reset({ gameMode, inventory: "clear" });
  await ctx.world.fill({ x: -3, y: 70, z: -3 }, { x: 9, y: 70, z: 6 }, "minecraft:stone");
  await ctx.world.fill({ x: -3, y: 71, z: -3 }, { x: 9, y: 76, z: 6 }, "minecraft:air");
  await ctx.player.teleport({ x: 0.5, y: 71, z: -0.5 });
  await ctx.commands.assert(`/item replace entity @s weapon.mainhand with ${item}[${LEVEL_ONE}]`);
  await ctx.player.inventory().selectHotbar(0);
}

async function heldItem(ctx: TeaKitTestContext): Promise<string> {
  const result = await ctx.commands.run("/data get entity @s SelectedItem", {
    captureOutput: true,
    requireSuccess: false,
  });
  return (result.output ?? []).join(" ");
}

/** Reads the held item's experience, since 1.21.1 item predicates cannot match modded components. */
async function expectExperience(ctx: TeaKitTestContext, item: ItemId, experience: number): Promise<void> {
  await ctx.commands.assert(`/execute if items entity @s weapon.mainhand ${item}`);
  await ctx.commands.assert(
    '/execute store result score @s bonded_action_xp run data get entity @s SelectedItem.components."bonded:item_level".experience',
  );
  await ctx.commands.assert(`/execute if score @s bonded_action_xp matches ${experience}`);
}
