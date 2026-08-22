import { Capability, Readiness, describe, test } from "@teakit/test";

describe.configure({
  timeout: "2m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.ClientInput,
    Capability.ClientScreenshot,
    Capability.PlayerInteractions,
    Capability.PlayerReset,
    Capability.PlayerTeleport,
    Capability.RuntimeTiming,
    Capability.ServerCommands,
    Capability.WorldClear,
    Capability.WorldFill,
    Capability.WorldSetBlock,
  ],
});

describe("Bonded bench effects", () => {
  test("carries a chest material into the bench work surface", async (ctx) => {
    const min = { x: -4, y: 69, z: -5 };
    const max = { x: 5, y: 76, z: 3 };
    let guiHidden = false;

    try {
      await ctx.player.reset({ gameMode: "survival", inventory: "clear" });
      await ctx.commands.run("/gamerule announceAdvancements false");
      await ctx.world.clear(min, max);
      await ctx.world.fill({ x: -4, y: 69, z: -5 }, { x: 5, y: 69, z: 3 }, "minecraft:stone");
      await ctx.world.setBlock({ x: 0, y: 70, z: 0 }, "bonded:repair_bench");
      await ctx.world.setBlock({ x: 1, y: 70, z: 0 }, "bonded:tool_bench");
      await ctx.commands.batch([
        "/setblock 2 70 0 minecraft:chest[facing=north]",
        "/item replace block 2 70 0 container.0 with minecraft:iron_ingot 1",
        "/item replace entity @s weapon.mainhand with minecraft:iron_pickaxe[minecraft:damage=24]",
      ]);
      await ctx.player.teleport({ x: 1, y: 71, z: -4 });
      await ctx.client.lookAt({ x: 1, y: 70.8, z: 0 });

      await ctx.player.useBlockServer({ x: 0, y: 70, z: 0 }, { face: "up", hand: "main_hand" });
      await ctx.commands.assert(
        "/execute unless items block 2 70 0 container.* minecraft:iron_ingot",
      );
      await ctx.runtime.wait(500);
      await ctx.client.keyState(290, true);
      await ctx.client.keyState(290, false);
      guiHidden = true;
      await ctx.client.screenshot("bonded-chest-to-bench-flight", { hideOverlay: true });
    } finally {
      if (guiHidden) {
        await ctx.client.keyState(290, true);
        await ctx.client.keyState(290, false);
      }
      await ctx.commands.run("/gamerule announceAdvancements true");
      await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
      await ctx.world.clear(min, max);
    }
  });
});
