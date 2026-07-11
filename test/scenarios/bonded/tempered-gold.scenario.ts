import { describe, expect, test } from "@teakit/test";
import type { ScenarioAction, ScenarioDefinition } from "@teakit/test";

const upgrades = [
  ["golden_sword", "tempered_gold_sword"],
  ["golden_pickaxe", "tempered_gold_pickaxe"],
  ["golden_shovel", "tempered_gold_shovel"],
  ["golden_axe", "tempered_gold_axe"],
  ["golden_hoe", "tempered_gold_hoe"],
  ["golden_helmet", "tempered_gold_helmet"],
  ["golden_chestplate", "tempered_gold_chestplate"],
  ["golden_leggings", "tempered_gold_leggings"],
  ["golden_boots", "tempered_gold_boots"]
] as const;

const upgradeSteps: ScenarioAction[] = upgrades.flatMap(([gold, tempered]) => [
  { action: "command", command: "/clear @s" },
  { action: "command", command: `/item replace entity @s weapon.mainhand with minecraft:${gold}` },
  { action: "command", command: "/item replace entity @s hotbar.1 with bonded:tempered_gold_ingot" },
  { action: "assert_command_success", command: "/bonded xp set @s 10 levels" },
  {
    action: "use_block_server",
    x: 0,
    y: 71,
    z: 0,
    direction: "up",
    hand: "main_hand"
  },
  { action: "assert_inventory_item", itemId: `bonded:${tempered}`, slot: 0, count: 1 }
]);

const temperedGoldScenario: ScenarioDefinition = {
  name: "bonded-tempered-gold-upgrades",
  setup: [
    { action: "command", command: "/clear @s" },
    { action: "command", command: "/gamemode survival @s" },
    { action: "command", command: "/tp @s 0 72 -1" },
    { action: "command", command: "/fill -2 70 -3 2 74 2 minecraft:air replace" },
    { action: "command", command: "/fill -2 70 -3 2 70 2 minecraft:stone replace" },
    { action: "command", command: "/setblock 0 71 0 bonded:tool_bench" },
    {
      action: "wait_for_block",
      x: 0,
      y: 71,
      z: 0,
      blockId: "bonded:tool_bench",
      timeoutMs: 3000
    }
  ],
  steps: upgradeSteps,
  cleanup: [
    { action: "command", command: "/clear @s" },
    { action: "command", command: "/gamemode creative @s" },
    { action: "command", command: "/fill -2 70 -3 2 74 2 minecraft:air replace" }
  ]
};

describe("Bonded Tempered Gold", () => {
  test(temperedGoldScenario.name, async ({ scenario }) => {
    const result = await scenario.run(temperedGoldScenario, { timeoutMs: 90_000 });

    expect(result.error ?? null).toBeNull();
  });
});
