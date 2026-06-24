import { describe, expect, test } from "@teakit/test";
import type { ScenarioDefinition } from "@teakit/test";

const horseArmorStorageScenario: ScenarioDefinition = {
  name: "bonded-horse-armor-storage-stability",
  setup: [
    {
      action: "command",
      command: "/clear @s"
    },
    {
      action: "command",
      command: "/tp @s 0 72 0"
    },
    {
      action: "command",
      command: "/fill -2 70 -2 2 74 2 minecraft:air replace"
    },
    {
      action: "command",
      command: "/fill -2 70 -2 2 70 2 minecraft:stone replace"
    },
    {
      action: "command",
      command: "/setblock 0 71 0 minecraft:chest[facing=north]"
    },
    {
      action: "wait_for_block",
      x: 0,
      y: 71,
      z: 0,
      blockId: "minecraft:chest",
      timeoutMs: 3000
    }
  ],
  steps: [
    {
      action: "command",
      command: "/item replace block 0 71 0 container.0 with minecraft:golden_horse_armor 1"
    },
    {
      action: "command",
      command: "/item replace block 0 71 0 container.1 with minecraft:iron_horse_armor 1"
    },
    {
      action: "command",
      command: "/item replace block 0 71 0 container.2 with minecraft:wolf_armor 1"
    },
    {
      action: "wait_ms",
      durationMs: 500
    },
    {
      action: "assert_command_success",
      command: "/data get block 0 71 0 Items",
      expectOutputContains: [
        "minecraft:golden_horse_armor",
        "minecraft:iron_horse_armor",
        "minecraft:wolf_armor"
      ]
    }
  ],
  cleanup: [
    {
      action: "command",
      command: "/clear @s"
    },
    {
      action: "command",
      command: "/fill -2 70 -2 2 74 2 minecraft:air replace"
    }
  ]
};

describe("Bonded horse armor storage", () => {
  test(horseArmorStorageScenario.name, async ({ scenario }) => {
    const result = await scenario.run(horseArmorStorageScenario, { timeoutMs: 60_000 });

    expect(result.error ?? null).toBeNull();
  });
});
