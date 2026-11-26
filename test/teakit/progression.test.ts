import { Capability, Readiness, describe, expect, test } from "@teakit/test";
import type { BlockPos, SpyHandle, TeaKitTestContext } from "@teakit/test";

const ORE: BlockPos = { x: 0, y: 71, z: 0 };
const BENCH: BlockPos = { x: 3, y: 71, z: 0 };

describe.configure({
  timeout: "5m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.ClientInput,
    Capability.PlayerInteractions,
    Capability.PlayerInventory,
    Capability.PlayerReset,
    Capability.PlayerTeleport,
    Capability.RuntimeTiming,
    Capability.ServerCommands,
    Capability.WorldBlock,
  ],
});

describe("Bonded progression feedback", () => {
  test("announces level-ups and the max level in chat", async (ctx) => {
    const chat = await ctx.spy.method(
      "bonded.chat",
      "net.minecraft.client.gui.components.ChatComponent#addMessage",
    );
    try {
      await ctx.commands.assert("/bondeddebug critical-chance 0");

      await mineIronOre(ctx, "bonded:item_level={experience:995,maxExperience:1000,level:1,bond:0}");
      await expectCalls(chat, "bonded.gameplay.level_up");

      await mineIronOre(ctx, "bonded:item_level={experience:995,maxExperience:1000,level:9,bond:0}");
      await expectCalls(chat, "bonded.gameplay.max_level");
    } finally {
      await ctx.spy.detach(chat);
      await resetArena(ctx);
    }
  });

  test("grants every Bonded advancement from the action that earns it", async (ctx) => {
    try {
      await ctx.commands.assert("/bondeddebug critical-chance 0");
      await ctx.commands.assert("/advancement revoke @s everything");

      // Owning a tool bench opens the tree.
      await ctx.commands.assert("/give @s bonded:tool_bench");
      await ctx.runtime.wait(500);
      await expectAdvancements(ctx, ["bonded:bonded"]);

      // One block of mining takes a worn item across the first level and every bond milestone.
      await mineIronOre(ctx, "bonded:item_level={experience:995,maxExperience:1000,level:1,bond:4995}");
      await expectAdvancements(ctx, [
        "bonded:getting_attached",
        "bonded:it_learns",
        "bonded:old_reliable",
        "bonded:trusted_tool",
        "bonded:heirloom",
      ]);

      await mineIronOre(ctx, "bonded:item_level={experience:995,maxExperience:1000,level:9,bond:0}");
      await expectAdvancements(ctx, ["bonded:fully_bonded"]);

      // A max-level pickaxe upgrades at the tool bench.
      await resetArena(ctx, "survival");
      await ctx.world.setBlock(BENCH, "bonded:tool_bench");
      await ctx.commands.assert(
        "/item replace entity @s weapon.mainhand with minecraft:iron_pickaxe[bonded:item_level={experience:0,maxExperience:1000,level:10,bond:1}]",
      );
      await ctx.commands.assert("/item replace entity @s hotbar.1 with minecraft:diamond 1");
      await ctx.player.useBlockServer(BENCH, { face: "up", hand: "main_hand" });
      await expectAdvancements(ctx, ["bonded:a_new_form"]);

      // Repairing past full durability, and repairing with scrap.
      await ctx.world.setBlock(BENCH, "bonded:repair_bench");
      await ctx.commands.assert("/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=49]");
      await ctx.commands.assert("/item replace entity @s hotbar.1 with bonded:scrap 1");
      await ctx.player.useBlockServer(BENCH, { face: "up", hand: "main_hand" });
      await ctx.commands.assert("/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=49]");
      await ctx.commands.assert("/item replace entity @s hotbar.1 with minecraft:iron_ingot 3");
      await ctx.player.useBlockServer(BENCH, { face: "up", hand: "main_hand" });
      await expectAdvancements(ctx, ["bonded:field_maintenance", "bonded:better_than_new"]);

      // A tool that wears out gives its scrap back.
      await ctx.commands.assert(
        "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=249]",
      );
      await ctx.world.setBlock({ x: 1, y: 71, z: 0 }, "minecraft:dirt");
      await ctx.player.mine({ x: 1, y: 71, z: 0 }, { timeoutMs: 5_000 });
      await expectAdvancements(ctx, ["bonded:waste_nothing"]);
    } finally {
      await resetArena(ctx);
    }
  });
});

async function resetArena(ctx: TeaKitTestContext, gameMode: "creative" | "survival" = "creative"): Promise<void> {
  await ctx.player.reset({ gameMode, inventory: "clear" });
  await ctx.world.fill({ x: -3, y: 70, z: -3 }, { x: 6, y: 70, z: 3 }, "minecraft:stone");
  await ctx.world.fill({ x: -3, y: 71, z: -3 }, { x: 6, y: 76, z: 3 }, "minecraft:air");
  await ctx.player.teleport({ x: 0.5, y: 71, z: -1.5 });
}

/** Mines one iron ore with a fresh iron pickaxe carrying the given Bonded state. */
async function mineIronOre(ctx: TeaKitTestContext, level: string): Promise<void> {
  await resetArena(ctx, "survival");
  await ctx.world.setBlock(ORE, "minecraft:iron_ore");
  await ctx.commands.assert(`/item replace entity @s weapon.mainhand with minecraft:iron_pickaxe[${level}]`);
  await ctx.player.inventory().selectHotbar(0);
  await ctx.runtime.wait(500);
  await ctx.player.mine(ORE, { timeoutMs: 5_000 });
  await ctx.runtime.wait(500);
}

async function expectCalls(spy: SpyHandle, text: string): Promise<void> {
  let seen = "";
  await expect(async () => {
    seen = JSON.stringify(await spy.$calls());
    return seen.includes(text);
  })
    .toEventuallyEqual(true, { timeout: "5s" })
    .catch((error: unknown) => {
      throw new Error(`Expected ${text} from ${spy.name}; saw ${seen.slice(-900)}: ${String(error)}`);
    });
}

async function expectAdvancements(ctx: TeaKitTestContext, ids: readonly string[]): Promise<void> {
  await ctx.runtime.wait(300);
  const missing: string[] = [];
  for (const id of ids) {
    const result = await ctx.commands.run(`/execute if entity @s[advancements={${id}=true}]`, {
      requireSuccess: false,
    });
    if (!result.success || result.result === 0) {
      missing.push(id);
    }
  }
  expect(missing).toEqual([]);
}
