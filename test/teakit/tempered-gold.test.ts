import { Capability, Readiness, describe, expect, test } from "@teakit/test";

const upgrades = [
  ["golden_sword", "tempered_gold_sword"],
  ["golden_pickaxe", "tempered_gold_pickaxe"],
  ["golden_shovel", "tempered_gold_shovel"],
  ["golden_axe", "tempered_gold_axe"],
  ["golden_hoe", "tempered_gold_hoe"],
  ["golden_helmet", "tempered_gold_helmet"],
  ["golden_chestplate", "tempered_gold_chestplate"],
  ["golden_leggings", "tempered_gold_leggings"],
  ["golden_boots", "tempered_gold_boots"],
] as const;

describe.configure({
  timeout: "2m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.PlayerInteractions,
    Capability.PlayerInventory,
    Capability.ServerCommands,
    Capability.WorldBlock,
  ],
});

describe("Bonded Tempered Gold", () => {
  test("upgrades every golden tool and armor piece", async ({ commands, player, world }) => {
    try {
      await commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/tp @s 0 72 -1",
        "/fill -2 70 -3 2 74 2 minecraft:air replace",
        "/fill -2 70 -3 2 70 2 minecraft:stone replace",
        "/setblock 0 71 0 bonded:tool_bench",
      ]);
      await expect(async () => (await world.block({ x: 0, y: 71, z: 0 })).id)
        .toEventuallyEqual("bonded:tool_bench", { timeout: "3s" });

      for (const [gold, tempered] of upgrades) {
        await commands.batch([
          "/clear @s",
          `/item replace entity @s weapon.mainhand with minecraft:${gold}`,
          "/item replace entity @s hotbar.1 with bonded:tempered_gold_ingot",
        ]);
        await commands.assert("/bonded xp set @s 10 levels");
        await player.useBlockServer(
          { x: 0, y: 71, z: 0 },
          { face: "up", hand: "main_hand" },
        );
        await expect(player.inventory()).toContainItem(`bonded:${tempered}`, {
          slot: 0,
          count: 1,
        });
      }
    } finally {
      await commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/fill -2 70 -3 2 74 2 minecraft:air replace",
      ]);
    }
  });
});
