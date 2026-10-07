import { Capability, Readiness, describe, test } from "@teakit/test";

describe.configure({
  timeout: "1m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.ClientInput,
    Capability.ClientScreenshot,
    Capability.PlayerInteractions,
    Capability.RuntimeTiming,
    Capability.ServerCommands,
    Capability.WorldBlock,
  ],
});

describe("Bonded ore experience", () => {
  test("awards experience when the player mines iron ore", async ({ commands, player, world }) => {
    try {
      await commands.assert("/bondeddebug critical-chance 0");
      await player.reset({ gameMode: "survival", inventory: "clear" });
      await player.teleport({ x: 0, y: 72, z: -1 });
      await world.clear({ x: -2, y: 70, z: -2 }, { x: 2, y: 74, z: 2 });
      await world.fill({ x: -2, y: 70, z: -2 }, { x: 2, y: 70, z: 2 }, "minecraft:stone");
      await world.setBlock({ x: 0, y: 71, z: 0 }, "minecraft:iron_ore");

      await commands.assert(
        "/item replace entity @s weapon.mainhand with minecraft:iron_pickaxe[bonded:item_level={experience:0,maxExperience:1000,level:1,bond:0}]",
      );
      await player.mine({ x: 0, y: 71, z: 0 }, { timeoutMs: 5_000 });
      await commands.assert(
        '/data get entity @s SelectedItem.components."bonded:item_level".experience',
        { captureOutput: true, expectOutputContains: ["10"] },
      );
    } finally {
      await player.reset({ gameMode: "creative", inventory: "clear" });
      await world.clear({ x: -2, y: 70, z: -2 }, { x: 2, y: 74, z: 2 });
    }
  });

  test("triples experience on a critical and announces it", async ({ client, commands, player, runtime, world }) => {
    try {
      await commands.assert("/bondeddebug critical-chance 1");
      await player.reset({ gameMode: "survival", inventory: "clear" });
      await player.teleport({ x: 0, y: 72, z: -1 });
      await world.clear({ x: -2, y: 70, z: -2 }, { x: 2, y: 74, z: 2 });
      await world.fill({ x: -2, y: 70, z: -2 }, { x: 2, y: 70, z: 2 }, "minecraft:stone");
      await world.setBlock({ x: 0, y: 71, z: 0 }, "minecraft:iron_ore");

      await commands.assert(
        "/item replace entity @s weapon.mainhand with minecraft:iron_pickaxe[bonded:item_level={experience:0,maxExperience:1000,level:1,bond:0}]",
      );
      await client.lookAt({ x: 0.5, y: 71.5, z: 0.5 });
      await player.mine({ x: 0, y: 71, z: 0 }, { timeoutMs: 5_000 });
      await runtime.wait(300);
      await client.screenshot("bonded-critical-experience", { hideOverlay: false });
      await commands.assert(
        "/execute if items entity @s weapon.mainhand minecraft:iron_pickaxe[bonded:item_level~{experience:30,bond:30}]",
      );
    } finally {
      await commands.run("/bondeddebug critical-chance 0", { requireSuccess: false });
      await player.reset({ gameMode: "creative", inventory: "clear" });
      await world.clear({ x: -2, y: 70, z: -2 }, { x: 2, y: 74, z: 2 });
    }
  });
});
