import { Capability, Readiness, describe, test } from "@teakit/test";
import type { BlockId, BlockPos, ClientScreen, ItemId, TeaKitTestContext } from "@teakit/test";

const STATION: BlockPos = { x: 0, y: 71, z: 1 };

describe.configure({
  timeout: "5m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.ClientScreen,
    Capability.ClientScreens,
    Capability.PlayerInteractions,
    Capability.PlayerReset,
    Capability.PlayerTeleport,
    Capability.RuntimeTiming,
    Capability.ServerCommands,
    Capability.WorldBlock,
  ],
});

describe("Bonded vanilla workstations", () => {
  for (const [gear, material] of [
    ["minecraft:iron_pickaxe", "minecraft:iron_ingot"],
    ["minecraft:bow", "minecraft:string"],
  ] as const) {
    test(`repairs a ${gear.split(":")[1]} in an anvil with ${material.split(":")[1]}`, async (ctx) => {
      try {
        await prepare(ctx, "minecraft:anvil");
        await ctx.commands.assert(`/item replace entity @s hotbar.0 with ${gear}[minecraft:damage=100]`);
        await ctx.commands.assert(`/item replace entity @s hotbar.1 with ${material} 4`);
        await ctx.player.openBlock(STATION);
        await ctx.runtime.wait(500);

        await moveInto(ctx, gear, 0, 3);
        await moveInto(ctx, material, 1, 3);
        await takeResult(ctx, gear, 2, 3);

        await ctx.commands.assert(`/execute if items entity @s container.* ${gear}`);
        await ctx.commands.assert(`/execute unless items entity @s container.* ${gear}[minecraft:damage=100]`);
      } finally {
        await cleanup(ctx);
      }
    });
  }

  test("keeps the bond and restarts the levels when a smithing table upgrades gear", async (ctx) => {
    try {
      await prepare(ctx, "minecraft:smithing_table");
      await ctx.commands.run("/scoreboard objectives remove bonded_workstation_bond", { requireSuccess: false });
      await ctx.commands.assert("/scoreboard objectives add bonded_workstation_bond dummy");
      await ctx.commands.assert(
        "/item replace entity @s hotbar.0 with minecraft:diamond_pickaxe[bonded:item_level={experience:0,maxExperience:1000,level:10,bond:77}]",
      );
      await ctx.commands.assert("/item replace entity @s hotbar.1 with minecraft:netherite_upgrade_smithing_template");
      await ctx.commands.assert("/item replace entity @s hotbar.2 with minecraft:netherite_ingot");
      await ctx.player.openBlock(STATION);
      await ctx.runtime.wait(500);

      await moveInto(ctx, "minecraft:netherite_upgrade_smithing_template", 0, 4);
      await moveInto(ctx, "minecraft:diamond_pickaxe", 1, 4);
      await moveInto(ctx, "minecraft:netherite_ingot", 2, 4);
      await takeResult(ctx, "minecraft:netherite_pickaxe", 3, 4);

      await ctx.commands.assert("/execute if items entity @s container.* minecraft:netherite_pickaxe");
      await ctx.commands.assert(
        '/execute store result score @s bonded_workstation_bond run data get entity @s Inventory[{id:"minecraft:netherite_pickaxe"}].components."bonded:item_level".bond',
      );
      await ctx.commands.assert("/execute if score @s bonded_workstation_bond matches 77");
      await ctx.commands.assert(
        '/execute store result score @s bonded_workstation_bond run data get entity @s Inventory[{id:"minecraft:netherite_pickaxe"}].components."bonded:item_level".level',
      );
      await ctx.commands.assert("/execute if score @s bonded_workstation_bond matches 1");
    } finally {
      await cleanup(ctx);
    }
  });
});

async function prepare(ctx: TeaKitTestContext, station: BlockId): Promise<void> {
  await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
  await ctx.world.fill({ x: -3, y: 70, z: -3 }, { x: 3, y: 70, z: 3 }, "minecraft:stone");
  await ctx.world.fill({ x: -3, y: 71, z: -3 }, { x: 3, y: 74, z: 3 }, "minecraft:air");
  await ctx.player.teleport({ x: 0.5, y: 71, z: -0.5 });
  await ctx.world.setBlock(STATION, station);
}

async function cleanup(ctx: TeaKitTestContext): Promise<void> {
  await ctx.commands.run("/scoreboard objectives remove bonded_workstation_bond", { requireSuccess: false });
  await ctx.client.closeMenus();
  await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
  await ctx.world.clear({ x: -3, y: 71, z: -3 }, { x: 3, y: 74, z: 3 });
}

/** Shift-clicks an item from the player's part of the open menu into its input slot. */
async function moveInto(
  ctx: TeaKitTestContext,
  item: ItemId,
  target: number,
  firstPlayerSlot: number,
): Promise<void> {
  const screen = await ctx.client.screen();
  const source = screen
    .menu()
    .slots()
    .find((slot) => slot.slot >= firstPlayerSlot && itemId(slot.item) === item);
  if (!source) {
    throw new Error(`Expected ${item} in the player's inventory: ${JSON.stringify(screen.menu().slots())}`);
  }
  await screen.menu().slot(source.slot).click({ clickType: "QUICK_MOVE" });
  await ctx.runtime.wait(300);
  const placed = (await ctx.client.screen()).menu().slots().find((slot) => slot.slot === target);
  if (itemId(placed?.item) !== item) {
    throw new Error(`Expected ${item} in input slot ${target}; filled slots: ${filledSlots(await ctx.client.screen())}`);
  }
}

/** Moves the finished result into the inventory, then closes the menu. */
async function takeResult(
  ctx: TeaKitTestContext,
  item: ItemId,
  resultSlot: number,
  firstPlayerSlot: number,
): Promise<void> {
  await ctx.runtime.wait(200);
  const screen = await ctx.client.screen();
  const result = screen.menu().slots().find((slot) => slot.slot === resultSlot);
  if (!result || itemId(result.item) !== item) {
    throw new Error(`Expected ${item} in the result slot; filled slots: ${filledSlots(screen)}`);
  }
  await screen.menu().slot(resultSlot).click({ clickType: "QUICK_MOVE" });
  await ctx.runtime.wait(200);
  const after = await ctx.client.screen();
  const taken = after
    .menu()
    .slots()
    .some((slot) => slot.slot >= firstPlayerSlot && itemId(slot.item) === item);
  if (!taken) {
    throw new Error(`Expected the ${item} result to reach the inventory: ${JSON.stringify(after.menu().slots())}`);
  }
  await ctx.client.closeMenus();
}

function itemId(item: unknown): string | undefined {
  if (!item || typeof item !== "object") {
    return undefined;
  }
  const stack = item as { id?: unknown; itemId?: unknown };
  if (typeof stack.id === "string") {
    return stack.id;
  }
  return typeof stack.itemId === "string" ? stack.itemId : undefined;
}

function filledSlots(screen: ClientScreen): string {
  return JSON.stringify(screen.menu().slots().filter((slot) => slot.item));
}
