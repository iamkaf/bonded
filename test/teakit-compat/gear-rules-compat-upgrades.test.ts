import { Capability, Readiness, describe, test } from "@teakit/test";
import type { TeaKitTestContext } from "@teakit/test";

const REPAIR_BENCH = { x: 0, y: 71, z: 0 };
const TOOL_BENCH = { x: 2, y: 71, z: 0 };

describe.configure({
  timeout: "3m",
  tags: ["compat-fixture"],
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.PlayerInteractions,
    Capability.PlayerReset,
    Capability.PlayerTeleport,
    Capability.ServerCommands,
  ],
});

describe("Bonded compatibility gear rules", () => {
  test("expands the Basic Weapons tag and inherits native repair materials", {
    target: { minecraft: "26.1.2", loader: ["fabric", "neoforge"] },
  }, async (ctx) => {
    try {
      await prepareArena(ctx);

      await expectHeldRule(ctx, "basicweapons:iron_dagger", [
        "Bonded rule basicweapons:iron_dagger",
        "type=melee_weapon",
        "repair=inherit",
        "upgrade=basicweapons:diamond_dagger",
        "source=Basic Weapons",
      ]);
      await expectHeldRule(ctx, "basicweapons:copper_hammer", [
        "Bonded rule basicweapons:copper_hammer",
        "type=melee_weapon",
        "source=Basic Weapons",
      ]);
      await expectHeldRule(ctx, "basicweapons:netherite_pike", [
        "Bonded rule basicweapons:netherite_pike",
        "type=melee_weapon",
        "source=Basic Weapons",
      ]);
      await expectHeldRule(ctx, "basicweapons:diamond_dagger", [
        "Bonded rule basicweapons:diamond_dagger",
        "type=melee_weapon",
        "upgrade=basicweapons:netherite_dagger",
        "source=Basic Weapons",
      ]);

      await ctx.commands.batch([
        "/clear @s",
        "/item replace entity @s weapon.mainhand with basicweapons:iron_dagger[minecraft:damage=20]",
        "/item replace entity @s hotbar.1 with minecraft:iron_ingot 1",
      ]);
      await ctx.player.teleport({ x: 0, y: 72, z: -1 });
      await ctx.player.useBlockServer(REPAIR_BENCH, { face: "up", hand: "main_hand" });

      await ctx.commands.assert(
        "/execute if items entity @s weapon.mainhand basicweapons:iron_dagger[minecraft:damage=0]",
      );
      await ctx.commands.assert(
        "/execute unless items entity @s inventory.* minecraft:iron_ingot",
      );

      await ctx.commands.batch([
        "/clear @s",
        '/item replace entity @s weapon.mainhand with basicweapons:iron_dagger[minecraft:damage=20,minecraft:enchantments={"minecraft:sharpness":2},minecraft:custom_data={basic_weapons_sentinel:1b},bonded:item_level={experience:0,maxExperience:1000,level:10,bond:314}]',
        "/item replace entity @s hotbar.1 with minecraft:diamond 1",
      ]);
      await ctx.player.teleport({ x: 2, y: 72, z: -1 });
      await ctx.player.useBlockServer(TOOL_BENCH, { face: "up", hand: "main_hand" });

      await expectPreservedUpgrade(
        ctx,
        "basicweapons:diamond_dagger",
        20,
        "basic_weapons_sentinel",
        "minecraft:sharpness",
        2,
        314,
      );
      await ctx.commands.assert(
        "/execute unless items entity @s inventory.* minecraft:diamond",
      );

      await ctx.commands.batch([
        "/clear @s",
        '/item replace entity @s weapon.mainhand with basicweapons:diamond_dagger[minecraft:damage=12,minecraft:enchantments={"minecraft:sharpness":3},minecraft:custom_data={netherite_sentinel:1b},bonded:item_level={experience:0,maxExperience:1000,level:10,bond:271}]',
        "/item replace entity @s hotbar.1 with minecraft:netherite_ingot 1",
      ]);
      await ctx.player.useBlockServer(TOOL_BENCH, { face: "up", hand: "main_hand" });

      await expectPreservedUpgrade(
        ctx,
        "basicweapons:netherite_dagger",
        12,
        "netherite_sentinel",
        "minecraft:sharpness",
        3,
        271,
      );
      await ctx.commands.assert(
        "/execute unless items entity @s inventory.* minecraft:netherite_ingot",
      );
    } finally {
      await cleanupArena(ctx);
    }
  });

  test("upgrades Advanced Netherite gear without discarding item data", {
    target: { minecraft: "26.1.2", loader: ["fabric", "neoforge"] },
  }, async (ctx) => {
    try {
      await prepareArena(ctx);

      await expectHeldRule(ctx, "advancednetherite:netherite_iron_sword", [
        "Bonded rule advancednetherite:netherite_iron_sword",
        "type=melee_weapon",
        "repair=inherit",
        "upgrade=advancednetherite:netherite_gold_sword",
        "source=Advanced Netherite",
      ]);
      await expectHeldRule(ctx, "advancednetherite:netherite_iron_helmet", [
        "Bonded rule advancednetherite:netherite_iron_helmet",
        "type=armor",
        "upgrade=advancednetherite:netherite_gold_helmet",
        "source=Advanced Netherite",
      ]);

      await ctx.commands.batch([
        "/clear @s",
        '/item replace entity @s weapon.mainhand with advancednetherite:netherite_iron_sword[minecraft:damage=37,minecraft:enchantments={"minecraft:sharpness":3},minecraft:custom_data={compat_sentinel:1b},bonded:item_level={experience:0,maxExperience:1000,level:10,bond:777}]',
        "/item replace entity @s hotbar.1 with advancednetherite:netherite_gold_ingot 1",
      ]);
      await ctx.player.teleport({ x: 2, y: 72, z: -1 });
      await ctx.player.useBlockServer(TOOL_BENCH, { face: "up", hand: "main_hand" });

      await expectPreservedUpgrade(
        ctx,
        "advancednetherite:netherite_gold_sword",
        37,
        "compat_sentinel",
        "minecraft:sharpness",
        3,
        777,
      );
      await ctx.commands.assert(
        "/execute unless items entity @s inventory.* advancednetherite:netherite_gold_ingot",
      );
      await expectHeldRule(ctx, "advancednetherite:netherite_gold_sword", [
        "type=melee_weapon",
        "repair=inherit",
        "upgrade=advancednetherite:netherite_emerald_sword",
        "source=Advanced Netherite",
      ], false);

      await ctx.commands.assert(
        "/item replace entity @s hotbar.1 with advancednetherite:netherite_gold_ingot 1",
      );
      await ctx.player.teleport({ x: 0, y: 72, z: -1 });
      await ctx.player.useBlockServer(REPAIR_BENCH, { face: "up", hand: "main_hand" });
      await ctx.commands.assert(
        "/execute if items entity @s weapon.mainhand advancednetherite:netherite_gold_sword[minecraft:damage=0,minecraft:custom_data~{compat_sentinel:1b},bonded:item_level~{bond:777}]",
      );
      await ctx.commands.assert(
        "/execute unless items entity @s inventory.* advancednetherite:netherite_gold_ingot",
      );

      await ctx.commands.batch([
        "/clear @s",
        '/item replace entity @s weapon.mainhand with advancednetherite:netherite_iron_helmet[minecraft:damage=17,minecraft:enchantments={"minecraft:unbreaking":2},minecraft:custom_data={armor_sentinel:1b},bonded:item_level={experience:0,maxExperience:1000,level:10,bond:413}]',
        "/item replace entity @s hotbar.1 with advancednetherite:netherite_gold_ingot 1",
      ]);
      await ctx.player.teleport({ x: 2, y: 72, z: -1 });
      await ctx.player.useBlockServer(TOOL_BENCH, { face: "up", hand: "main_hand" });

      await expectPreservedUpgrade(
        ctx,
        "advancednetherite:netherite_gold_helmet",
        17,
        "armor_sentinel",
        "minecraft:unbreaking",
        2,
        413,
      );
      await ctx.commands.assert(
        "/execute unless items entity @s inventory.* advancednetherite:netherite_gold_ingot",
      );
    } finally {
      await cleanupArena(ctx);
    }
  });
});

async function prepareArena(ctx: TeaKitTestContext): Promise<void> {
  await ctx.player.reset({ gameMode: "survival", inventory: "clear" });
  await ctx.commands.batch([
    "/fill -3 70 -3 4 75 2 minecraft:air replace",
    "/fill -3 70 -3 4 70 2 minecraft:stone replace",
    "/setblock 0 71 0 bonded:repair_bench",
    "/setblock 2 71 0 bonded:tool_bench",
  ]);
}

async function cleanupArena(ctx: TeaKitTestContext): Promise<void> {
  await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
  await ctx.commands.run("/fill -3 70 -3 4 75 2 minecraft:air replace", {
    requireSuccess: false,
  });
}

async function expectHeldRule(
  ctx: TeaKitTestContext,
  item: string,
  expectedOutput: readonly string[],
  replaceHeldItem = true,
): Promise<void> {
  if (replaceHeldItem) {
    await ctx.commands.assert(`/item replace entity @s weapon.mainhand with ${item}`);
  }
  await ctx.commands.run("/bondeddebug rules query", {
    captureOutput: true,
    expectOutputContains: [...expectedOutput],
    requireSuccess: true,
  });
}

async function expectPreservedUpgrade(
  ctx: TeaKitTestContext,
  item: string,
  damage: number,
  sentinel: string,
  enchantment: string,
  enchantmentLevel: number,
  bond: number,
): Promise<void> {
  await ctx.commands.assert(
    `/execute if items entity @s weapon.mainhand ${item}[minecraft:damage=${damage}]`,
  );
  await ctx.commands.assert(
    `/execute if items entity @s weapon.mainhand ${item}[minecraft:custom_data~{${sentinel}:1b}]`,
  );
  await ctx.commands.assert(
    `/execute if items entity @s weapon.mainhand ${item}[minecraft:enchantments={"${enchantment}":${enchantmentLevel}}]`,
  );
  await ctx.commands.assert(
    `/execute if items entity @s weapon.mainhand ${item}[bonded:item_level~{level:1,bond:${bond}}]`,
  );
}
