import { Capability, Readiness, describe, expect, test } from "@teakit/test";

describe.configure({
  timeout: "1m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.RuntimeTiming,
    Capability.ServerCommands,
    Capability.WorldBlock,
  ],
});

describe("Bonded horse armor storage", () => {
  test("keeps horse and wolf armor intact in containers", async ({ commands, runtime, world }) => {
    try {
      await commands.batch([
        "/clear @s",
        "/tp @s 0 72 0",
        "/fill -2 70 -2 2 74 2 minecraft:air replace",
        "/fill -2 70 -2 2 70 2 minecraft:stone replace",
        "/setblock 0 71 0 minecraft:chest[facing=north]",
      ]);
      await expect(async () => (await world.block({ x: 0, y: 71, z: 0 })).id)
        .toEventuallyEqual("minecraft:chest", { timeout: "3s" });

      await commands.batch([
        "/item replace block 0 71 0 container.0 with minecraft:golden_horse_armor 1",
        "/item replace block 0 71 0 container.1 with minecraft:iron_horse_armor 1",
        "/item replace block 0 71 0 container.2 with minecraft:wolf_armor 1",
      ]);
      await runtime.wait(500);
      await commands.assert("/data get block 0 71 0 Items", {
        captureOutput: true,
        expectOutputContains: [
          "minecraft:golden_horse_armor",
          "minecraft:iron_horse_armor",
          "minecraft:wolf_armor",
        ],
      });
    } finally {
      await commands.batch([
        "/clear @s",
        "/fill -2 70 -2 2 74 2 minecraft:air replace",
      ]);
    }
  });
});
