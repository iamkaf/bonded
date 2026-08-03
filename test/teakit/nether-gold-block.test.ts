import { Capability, Readiness, describe, expect, test } from "@teakit/test";

describe.configure({
  timeout: "2m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.PlayerInteractions,
    Capability.ServerCommands,
    Capability.WorldBlock,
  ],
});

describe("Bonded Nether block breaking", () => {
  test("breaks a gold block without crashing the server", async ({ commands, player, world }) => {
    try {
      await commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/execute in minecraft:the_nether run tp @s 0 121 -3",
        "/fill -2 119 -3 2 124 2 minecraft:air replace",
        "/fill -2 119 -3 2 119 2 minecraft:netherrack replace",
        "/item replace entity @s weapon.mainhand with minecraft:diamond_pickaxe",
      ]);

      await commands.assert("/execute if dimension minecraft:the_nether");
      await commands.assert("/setblock 0 120 0 minecraft:gold_block");
      await player.mine({ x: 0, y: 120, z: 0 }, { timeoutMs: 5_000 });
      await commands.assert("/execute if dimension minecraft:the_nether run say Bonded server responsive");
      expect((await world.block({ x: 0, y: 120, z: 0 })).id).toBe("minecraft:air");
    } finally {
      await commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/fill -2 119 -3 2 124 2 minecraft:air replace",
        "/execute in minecraft:overworld run tp @s 0 72 0",
      ]);
    }
  });
});
