import { describe, expect, test } from "@teakit/test";
import type { ScenarioDefinition } from "@teakit/test";

const oreExperienceScenario: ScenarioDefinition = {
  name: "bonded-ore-experience",
  setup: [
    { action: "command", command: "/clear @s" },
    { action: "command", command: "/gamemode survival @s" },
    { action: "command", command: "/tp @s 0 72 -1" },
    { action: "command", command: "/fill -2 70 -2 2 74 2 minecraft:air replace" },
    { action: "command", command: "/fill -2 70 -2 2 70 2 minecraft:stone replace" },
    { action: "command", command: "/setblock 0 71 0 minecraft:iron_ore" },
    {
      action: "wait_for_block",
      x: 0,
      y: 71,
      z: 0,
      blockId: "minecraft:iron_ore",
      timeoutMs: 3000
    }
  ],
  steps: [
    {
      action: "command",
      command: "/item replace entity @s weapon.mainhand with minecraft:iron_pickaxe[bonded:item_level={experience:0,maxExperience:1000,level:1,bond:0}]"
    },
    {
      action: "break_block",
      x: 0,
      y: 71,
      z: 0,
      direction: "up",
      timeoutMs: 5000
    },
    {
      action: "assert_command_success",
      command: "/data get entity @s SelectedItem.components.\"bonded:item_level\".experience",
      expectOutputContains: ["10"]
    }
  ],
  cleanup: [
    { action: "command", command: "/clear @s" },
    { action: "command", command: "/gamemode creative @s" },
    { action: "command", command: "/fill -2 70 -2 2 74 2 minecraft:air replace" }
  ]
};

describe("Bonded ore experience", () => {
  test(oreExperienceScenario.name, async ({ scenario }) => {
    const result = await scenario.run(oreExperienceScenario, { timeoutMs: 60_000 });

    expect(result.error ?? null).toBeNull();
  });
});
