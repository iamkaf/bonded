import { beforeEach, Capability, Readiness, describe, expect, test } from "@teakit/test";
import type { BlockId, BlockPos, ItemId, ItemStack, TeaKitTestContext } from "@teakit/test";

interface ExpectedPlayerItem {
  id: ItemId;
  slot?: number;
  count?: number;
  damage?: number;
  maxDamage?: number;
}

describe.configure({
  timeout: "20m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.ClientScreenshot,
    Capability.PlayerInteractions,
    Capability.PlayerDriver,
    Capability.PlayerInventory,
    Capability.PlayerPosition,
    Capability.PlayerTeleport,
    Capability.RuntimeTiming,
    Capability.ServerCommands,
    Capability.WorldBlock,
    Capability.WorldEntities,
  ],
});

beforeEach(async (ctx) => {
  await ctx.player.inventory().selectHotbar(0);
});

describe("Bonded gameplay", () => {
  test("uses repair and tool bench materials from adjacent storage", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/tp @s 0 72 0",
        "/fill -3 70 -3 9 70 3 minecraft:stone replace",
        "/fill -3 71 -3 9 76 3 minecraft:air replace",
        "/setblock 0 71 0 bonded:repair_bench",
        "/setblock 1 71 0 minecraft:barrel",
        "/item replace block 1 71 0 container.0 with minecraft:iron_ingot 1",
        "/setblock 4 71 0 bonded:tool_bench",
        "/setblock 5 71 0 bonded:repair_bench",
        "/setblock 6 71 0 minecraft:chest[facing=north]",
        "/item replace block 6 71 0 container.0 with minecraft:copper_ingot 1",
        "/setblock 7 71 0 bonded:repair_bench",
        "/setblock 8 71 0 minecraft:copper_chest[facing=north]",
        "/item replace block 8 71 0 container.0 with minecraft:iron_ingot 1",
      ]);
      await expectBlock(ctx, { x: 0, y: 71, z: 0 }, "bonded:repair_bench", 3000);
      await expectBlock(ctx, { x: 4, y: 71, z: 0 }, "bonded:tool_bench", 3000);
      await ctx.commands.batch([
        "/item replace entity @s weapon.mainhand with minecraft:iron_pickaxe[minecraft:damage=1]",
        "/tp @s 0 72 -1",
      ]);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_pickaxe","slot":0,"damage":0});
      await ctx.commands.batch([
        "/clear @s",
        "/item replace entity @s weapon.mainhand with minecraft:iron_pickaxe[minecraft:damage=1]",
        "/tp @s 8 72 -1",
      ]);
      await ctx.player.useBlockServer({ x: 7, y: 71, z: 0 }, { face: "east", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_pickaxe","slot":0,"damage":0});
      await ctx.commands.batch([
        "/clear @s",
        "/item replace entity @s weapon.mainhand with minecraft:stone_pickaxe",
      ]);
      await ctx.commands.assert("/bonded xp set @s 10 levels");
      await ctx.commands.run("/tp @s 4 72 -1");
      await ctx.player.useBlockServer({ x: 4, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:copper_pickaxe","slot":0,"count":1});
      await ctx.runtime.wait(250);
      await ctx.client.screenshot("bonded-adjacent-storage-benches", { hideOverlay: true });
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/fill -3 70 -3 9 76 3 minecraft:air replace",
      ]);
    }
  });

  test("applies innate bond to generated loot and mob equipment", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/loot give @s loot bonded:test/innate_bond_chest",
      ]);
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":0});
      await ctx.commands.batch([
        "/clear @s",
        "/summon zombie 0 72 0 {NoAI:1b,PersistenceRequired:1b}",
      ]);
      await ctx.runtime.wait(250);
      await ctx.commands.batch([
        "/item replace entity @e[type=minecraft:zombie,limit=1,sort=nearest] weapon.mainhand with minecraft:iron_shovel",
        "/item replace entity @s hotbar.1 from entity @e[type=minecraft:zombie,limit=1,sort=nearest] weapon.mainhand",
      ]);
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","damage":0});
      await ctx.client.screenshot("bonded-innate-loot-bond", { hideOverlay: true });
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/kill @e[type=minecraft:zombie,distance=..16]",
      ]);
    }
  });

  test("adds temporary durability that expires after use", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/tp @s 0 72 0",
        "/fill -3 70 -3 4 70 3 minecraft:stone replace",
        "/fill -3 71 -3 4 76 3 minecraft:air replace",
        "/setblock 0 71 0 bonded:repair_bench",
        "/setblock 1 71 0 minecraft:barrel",
        "/item replace block 1 71 0 container.0 with minecraft:iron_ingot 1",
      ]);
      await expectBlock(ctx, { x: 0, y: 71, z: 0 }, "bonded:repair_bench", 3000);
      await ctx.commands.run("/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=49]");
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","damage":0,"maxDamage":251});
      await ctx.commands.run("/tp @s 2 72 -1");
      await ctx.runtime.wait(250);
      await ctx.commands.run("/setblock 2 71 0 minecraft:dirt");
      await expectBlock(ctx, { x: 2, y: 71, z: 0 }, "minecraft:dirt", 3000);
      await ctx.runtime.wait(500);
      await ctx.player.mine({ x: 2, y: 71, z: 0 }, { timeoutMs: 5000 });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","damage":0,"maxDamage":250});
      await ctx.runtime.wait(250);
      await ctx.client.screenshot("bonded-over-repair", { hideOverlay: true });
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/fill -3 70 -3 4 76 3 minecraft:air replace",
      ]);
    }
  });

  test("caps stacked over-repair durability", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/tp @s 0 72 0",
        "/fill -3 70 -3 3 70 3 minecraft:stone replace",
        "/fill -3 71 -3 3 76 3 minecraft:air replace",
        "/setblock 0 71 0 bonded:repair_bench",
      ]);
      await expectBlock(ctx, { x: 0, y: 71, z: 0 }, "bonded:repair_bench", 3000);
      await ctx.commands.batch([
        "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=1]",
        "/item replace entity @s hotbar.1 with minecraft:iron_ingot 7",
      ]);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":0,"maxDamage":500});
      await expectPlayerItem(ctx, {"id":"minecraft:iron_ingot","slot":1,"count":1});
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/fill -3 70 -3 3 76 3 minecraft:air replace",
      ]);
    }
  });

  test("keeps visible over-repair durability after taking damage", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/tp @s 0 72 0",
        "/fill -3 70 -3 4 70 3 minecraft:stone replace",
        "/fill -3 71 -3 4 76 3 minecraft:air replace",
        "/setblock 0 71 0 bonded:repair_bench",
        "/setblock 1 71 0 minecraft:barrel",
        "/item replace block 1 71 0 container.0 with minecraft:iron_ingot 8",
      ]);
      await expectBlock(ctx, { x: 0, y: 71, z: 0 }, "bonded:repair_bench", 3000);
      await ctx.commands.run("/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=1]");
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","damage":0,"maxDamage":449});
      await ctx.commands.run("/tp @s 2 72 -1");
      await ctx.runtime.wait(250);
      await ctx.commands.run("/setblock 2 71 0 minecraft:dirt");
      await expectBlock(ctx, { x: 2, y: 71, z: 0 }, "minecraft:dirt", 3000);
      await ctx.runtime.wait(500);
      await ctx.player.mine({ x: 2, y: 71, z: 0 }, { timeoutMs: 5000 });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","damage":1,"maxDamage":449});
      await ctx.runtime.wait(250);
      await ctx.client.screenshot("bonded-over-repair-visual", { hideOverlay: false });
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/fill -3 70 -3 4 76 3 minecraft:air replace",
      ]);
    }
  });

  test("keeps maximum durability stable while querying levels", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/tp @s 0 72 0",
        "/fill -3 70 -3 3 70 3 minecraft:stone replace",
        "/fill -3 71 -3 3 76 3 minecraft:air replace",
        "/setblock 0 71 0 bonded:repair_bench",
      ]);
      await expectBlock(ctx, { x: 0, y: 71, z: 0 }, "bonded:repair_bench", 3000);
      await ctx.commands.batch([
        "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=1,bonded:item_level={experience:0,maxExperience:1000,level:1,bond:500}]",
        "/item replace entity @s hotbar.1 with minecraft:iron_ingot 1",
      ]);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":0,"maxDamage":359});
      await ctx.commands.assert("/bonded xp query @s levels");
      await ctx.commands.assert("/bonded xp query @s levels");
      await ctx.commands.assert("/bonded xp query @s levels");
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":0,"maxDamage":359});
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/fill -3 70 -3 3 76 3 minecraft:air replace",
      ]);
    }
  });

  test("clears temporary durability when upgrading a tool", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/tp @s 0 72 0",
        "/fill -3 70 -3 4 70 3 minecraft:stone replace",
        "/fill -3 71 -3 4 76 3 minecraft:air replace",
        "/setblock 0 71 0 bonded:repair_bench",
        "/setblock 2 71 0 bonded:tool_bench",
      ]);
      await expectBlock(ctx, { x: 0, y: 71, z: 0 }, "bonded:repair_bench", 3000);
      await expectBlock(ctx, { x: 2, y: 71, z: 0 }, "bonded:tool_bench", 3000);
      await ctx.commands.batch([
        "/item replace entity @s weapon.mainhand with minecraft:stone_pickaxe[minecraft:damage=1,bonded:item_level={experience:0,maxExperience:50,level:10,bond:500}]",
        "/item replace entity @s hotbar.1 with bonded:scrap 1",
        "/item replace entity @s hotbar.2 with minecraft:copper_ingot 1",
      ]);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:stone_pickaxe","slot":0,"damage":0,"maxDamage":216});
      await ctx.player.useBlockServer({ x: 2, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:copper_pickaxe","slot":0,"damage":0,"maxDamage":240});
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/fill -3 70 -3 4 76 3 minecraft:air replace",
      ]);
    }
  });

  test("repairs near-threshold damage without underflow", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/tp @s 0 72 0",
        "/fill -2 70 -2 4 75 2 minecraft:air replace",
        "/fill -2 70 -2 4 70 2 minecraft:stone replace",
        "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=299,minecraft:max_damage=650,bonded:item_level={level:4,maxExperience:100,experience:96,bond:1116},bonded:applied_bonuses={bonuses:[\"bonded:durability_500\",\"bonded:durability_1000\"]},bonded:max_damage_modifiers={base_max_damage:250,modifiers:[{amount:50.0d,id:\"bonded:durability_500\",operation:\"add_value\"},{amount:50.0d,id:\"bonded:durability_1000\",operation:\"add_value\"},{amount:300.0d,id:\"bonded:over_repair\",operation:\"add_value\"}]}]",
      ]);
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":299});
      await ctx.commands.batch([
        "/setblock 1 71 0 minecraft:dirt",
        "/tp @s 1 72 -1",
      ]);
      await ctx.player.useBlockServer({ x: 1, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":0});
      await ctx.commands.batch([
        "/setblock 2 71 0 minecraft:dirt",
        "/tp @s 2 72 -1",
      ]);
      await ctx.player.useBlockServer({ x: 2, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":1});
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/fill -2 70 -2 4 75 2 minecraft:air replace",
      ]);
    }
  });

  test("preserves damage when bonuses are reapplied", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=251,minecraft:max_damage=650,minecraft:enchantments={\"minecraft:efficiency\":5},bonded:item_level={level:4,maxExperience:100,experience:40,bond:1000},bonded:applied_bonuses={bonuses:[\"bonded:durability_500\",\"bonded:durability_1000\",\"bonded:dig_speed_1000\"]},bonded:max_damage_modifiers={base_max_damage:250,modifiers:[{amount:300.0d,id:\"bonded:over_repair\",operation:\"add_value\"},{amount:50.0d,id:\"bonded:durability_500\",operation:\"add_value\"},{amount:50.0d,id:\"bonded:durability_1000\",operation:\"add_value\"}]}]",
      ]);
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":251,"maxDamage":650});
      await ctx.commands.assert("/bonded xp add @s 1 points");
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":251,"maxDamage":650});
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode creative @s",
      ]);
    }
  });

  test("preserves over-repaired damage while gaining block experience", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/tp @s 0 72 0",
        "/fill -2 70 -2 2 70 2 minecraft:stone replace",
        "/fill -2 71 -2 2 74 2 minecraft:air replace",
        "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=251,minecraft:max_damage=650,minecraft:enchantments={\"minecraft:efficiency\":5},bonded:item_level={level:4,maxExperience:100,experience:40,bond:1000},bonded:applied_bonuses={bonuses:[\"bonded:durability_500\",\"bonded:durability_1000\",\"bonded:dig_speed_1000\"]},bonded:max_damage_modifiers={base_max_damage:250,modifiers:[{amount:300.0d,id:\"bonded:over_repair\",operation:\"add_value\"},{amount:50.0d,id:\"bonded:durability_500\",operation:\"add_value\"},{amount:50.0d,id:\"bonded:durability_1000\",operation:\"add_value\"}]}]",
      ]);
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":251,"maxDamage":650});
      await ctx.commands.batch([
        "/setblock 0 71 0 minecraft:dirt",
        "/tp @s 0 72 -1",
      ]);
      await expectBlock(ctx, { x: 0, y: 71, z: 0 }, "minecraft:dirt", 3000);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":252,"maxDamage":650});
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/fill -2 70 -2 2 74 2 minecraft:air replace",
      ]);
    }
  });

  test("preserves damage through the full material upgrade chain", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/tp @s 0 72 0",
        "/fill -3 70 -3 8 70 3 minecraft:stone replace",
        "/fill -3 71 -3 8 76 3 minecraft:air replace",
        "/setblock 0 71 0 bonded:repair_bench",
        "/setblock 2 71 0 bonded:tool_bench",
      ]);
      await expectBlock(ctx, { x: 0, y: 71, z: 0 }, "bonded:repair_bench", 3000);
      await expectBlock(ctx, { x: 2, y: 71, z: 0 }, "bonded:tool_bench", 3000);
      await ctx.commands.batch([
        "/item replace entity @s weapon.mainhand with minecraft:wooden_shovel[minecraft:damage=1,bonded:item_level={experience:0,maxExperience:30,level:10,bond:500}]",
        "/item replace entity @s hotbar.1 with bonded:scrap 1",
      ]);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.commands.batch([
        "/setblock 4 71 0 minecraft:dirt",
        "/tp @s 4 72 -1",
      ]);
      await expectBlock(ctx, { x: 4, y: 71, z: 0 }, "minecraft:dirt", 3000);
      await ctx.player.useBlockServer({ x: 4, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.commands.batch([
        "/item replace entity @s hotbar.1 with minecraft:cobblestone 1",
        "/tp @s 2 72 -1",
      ]);
      await ctx.player.useBlockServer({ x: 2, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:stone_shovel","slot":0,"damage":0});
      await ctx.commands.assert("/bonded xp set @s 10 levels");
      await ctx.commands.batch([
        "/item replace entity @s hotbar.1 with bonded:scrap 1",
        "/tp @s 0 72 -1",
      ]);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.commands.batch([
        "/setblock 5 71 0 minecraft:dirt",
        "/tp @s 5 72 -1",
      ]);
      await expectBlock(ctx, { x: 5, y: 71, z: 0 }, "minecraft:dirt", 3000);
      await ctx.player.useBlockServer({ x: 5, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.commands.batch([
        "/item replace entity @s hotbar.1 with minecraft:copper_ingot 1",
        "/tp @s 2 72 -1",
      ]);
      await ctx.player.useBlockServer({ x: 2, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:copper_shovel","slot":0,"damage":0});
      await ctx.commands.assert("/bonded xp set @s 10 levels");
      await ctx.commands.batch([
        "/item replace entity @s hotbar.1 with bonded:scrap 1",
        "/tp @s 0 72 -1",
      ]);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.commands.batch([
        "/setblock 6 71 0 minecraft:dirt",
        "/tp @s 6 72 -1",
      ]);
      await expectBlock(ctx, { x: 6, y: 71, z: 0 }, "minecraft:dirt", 3000);
      await ctx.player.useBlockServer({ x: 6, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.commands.batch([
        "/item replace entity @s hotbar.1 with minecraft:iron_ingot 1",
        "/tp @s 2 72 -1",
      ]);
      await ctx.player.useBlockServer({ x: 2, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":0});
      await ctx.commands.batch([
        "/setblock 7 71 0 minecraft:dirt",
        "/tp @s 7 72 -1",
      ]);
      await expectBlock(ctx, { x: 7, y: 71, z: 0 }, "minecraft:dirt", 3000);
      await ctx.player.useBlockServer({ x: 7, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":1});
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/fill -3 70 -3 8 76 3 minecraft:air replace",
      ]);
    }
  });

  test("repairs tools with scrap and applies over-repair", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/tp @s 0 72 0",
        "/fill -3 70 -3 3 70 3 minecraft:stone replace",
        "/fill -3 71 -3 3 76 3 minecraft:air replace",
        "/setblock 0 71 0 bonded:repair_bench",
      ]);
      await expectBlock(ctx, { x: 0, y: 71, z: 0 }, "bonded:repair_bench", 3000);
      await ctx.commands.batch([
        "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=49]",
        "/item replace entity @s hotbar.1 with minecraft:dirt 1",
      ]);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":49,"maxDamage":250});
      await ctx.commands.batch([
        "/clear @s",
        "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=49]",
        "/item replace entity @s hotbar.1 with minecraft:iron_ingot 1",
      ]);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":0,"maxDamage":251});
      await ctx.commands.batch([
        "/clear @s",
        "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=49]",
        "/item replace entity @s hotbar.1 with bonded:scrap 1",
      ]);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":0,"maxDamage":251});
      await ctx.commands.batch([
        "/clear @s",
        "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=49]",
        "/item replace entity @s hotbar.1 with minecraft:iron_ingot 1",
        "/item replace entity @s hotbar.2 with bonded:scrap 1",
      ]);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:iron_shovel","slot":0,"damage":0,"maxDamage":251});
      await expectPlayerItem(ctx, {"id":"minecraft:iron_ingot","slot":1,"count":1});
      await ctx.runtime.wait(250);
      await ctx.client.screenshot("bonded-scrap-repair-bench", { hideOverlay: true });
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/fill -3 70 -3 3 76 3 minecraft:air replace",
      ]);
    }
  });

  test("repairs bows and crossbows", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/tp @s 0 72 0",
        "/fill -3 70 -3 3 70 3 minecraft:stone replace",
        "/fill -3 71 -3 3 76 3 minecraft:air replace",
        "/setblock 0 71 0 bonded:repair_bench",
      ]);
      await expectBlock(ctx, { x: 0, y: 71, z: 0 }, "bonded:repair_bench", 3000);
      await ctx.commands.batch([
        "/item replace entity @s hotbar.0 with minecraft:bow[minecraft:damage=20]",
        "/item replace entity @s hotbar.1 with minecraft:string 1",
      ]);
      await ctx.player.inventory().selectHotbar(0);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:bow","slot":0,"damage":0});
      await ctx.commands.batch([
        "/clear @s",
        "/item replace entity @s hotbar.0 with minecraft:crossbow[minecraft:damage=20]",
        "/item replace entity @s hotbar.1 with minecraft:string 1",
      ]);
      await ctx.player.inventory().selectHotbar(0);
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:crossbow","slot":0,"damage":0});
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/fill -3 70 -3 3 76 3 minecraft:air replace",
      ]);
    }
  });

  test("drops scrap from tool damage and material upgrades", async (ctx) => {
    try {
      await ctx.commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/tp @s 0 72 0",
        "/fill -3 70 -3 6 70 3 minecraft:stone replace",
        "/fill -3 71 -3 6 76 3 minecraft:air replace",
        "/setblock 0 71 0 minecraft:grass_block",
      ]);
      await expectBlock(ctx, { x: 0, y: 71, z: 0 }, "minecraft:grass_block", 3000);
      await ctx.commands.run("/setblock 4 71 0 bonded:tool_bench");
      await expectBlock(ctx, { x: 4, y: 71, z: 0 }, "bonded:tool_bench", 3000);
      await ctx.entities.query({ origin: await ctx.player.position(), radius: 16, type: "minecraft:item" }).removeAll();
      await ctx.commands.run("/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=249,bonded:item_level={experience:0,maxExperience:1000,level:1,bond:250}]");
      await ctx.player.useBlockServer({ x: 0, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.entities.query({ origin: await ctx.player.position(), radius: 8, type: "minecraft:item", item: "bonded:scrap" }).waitForCount(1, { timeout: 5000 });
      await ctx.entities.query({ origin: await ctx.player.position(), radius: 16, type: "minecraft:item" }).removeAll();
      await ctx.commands.batch([
        "/clear @s",
        "/item replace entity @s weapon.mainhand with minecraft:stone_pickaxe",
        "/item replace entity @s hotbar.1 with minecraft:copper_ingot 1",
      ]);
      await ctx.commands.assert("/bonded xp set @s 10 levels");
      await ctx.player.useBlockServer({ x: 4, y: 71, z: 0 }, { face: "up", hand: "main_hand" });
      await expectPlayerItem(ctx, {"id":"minecraft:copper_pickaxe","slot":0,"count":1});
      await ctx.entities.query({ origin: await ctx.player.position(), radius: 8, type: "minecraft:item", item: "bonded:scrap" }).waitForCountAtMost(2, { timeout: 5000 });
    } finally {
      await ctx.commands.batch([
        "/clear @s",
        "/kill @e[type=minecraft:item,distance=..16]",
        "/gamemode creative @s",
        "/fill -3 70 -3 6 76 3 minecraft:air replace",
      ]);
    }
  });

});

async function expectBlock(ctx: TeaKitTestContext, position: BlockPos, blockId: BlockId, timeout: number) {
  await expect(async () => (await ctx.world.block(position)).id).toEventuallyEqual(blockId, { timeout });
}

async function expectPlayerItem(ctx: TeaKitTestContext, expected: ExpectedPlayerItem) {
  await ctx.player.inventory().waitForItem(expected.id, {
    slot: expected.slot,
    count: expected.count ?? 1,
    timeout: "5s",
  });
  if (expected.count !== undefined) {
    await expect(async () => (await playerItem(ctx, expected))?.count)
      .toEventuallyEqual(expected.count, { timeout: "5s", interval: "50ms" });
  }
  if (expected.damage !== undefined) {
    await expect(async () => (await playerItem(ctx, expected))?.damage)
      .toEventuallyEqual(expected.damage, { timeout: "5s", interval: "50ms" });
  }
  if (expected.maxDamage !== undefined) {
    await expect(async () => (await playerItem(ctx, expected))?.maxDamage)
      .toEventuallyEqual(expected.maxDamage, { timeout: "5s", interval: "50ms" });
  }
}

async function playerItem(ctx: TeaKitTestContext, expected: ExpectedPlayerItem) {
  const inventory = await ctx.player.inventory();
  return inventory.items.find((candidate) =>
    itemId(candidate) === expected.id && (expected.slot === undefined || candidate.slot === expected.slot)
  );
}

function itemId(item: ItemStack) {
  return item.id ?? (item.itemId as string | undefined);
}
