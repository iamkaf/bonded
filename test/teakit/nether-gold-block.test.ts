import { Capability, Readiness, describe, test } from "@teakit/test";
import type { TeaKitTestContext } from "@teakit/test";

describe.configure({
  timeout: "2m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.PlayerInteractions,
    Capability.ServerCommands,
  ],
});

describe("Bonded Nether block breaking", () => {
  test("breaks a gold block without crashing the server", async (ctx) => {
    const { commands, player } = ctx;
    try {
      await commands.batch([
        "/clear @s",
        "/gamemode survival @s",
        "/execute in minecraft:the_nether run tp @s 0 121 -1",
        "/execute in minecraft:the_nether run fill -2 119 -3 2 124 2 minecraft:air replace",
        "/execute in minecraft:the_nether run fill -2 119 -3 2 119 2 minecraft:netherrack replace",
        "/item replace entity @s weapon.mainhand with minecraft:diamond_pickaxe",
      ]);

      await commands.assert("/execute if dimension minecraft:the_nether");
      await waitForBlock(ctx, "minecraft:gold_block", true);
      await player.mine({ x: 0, y: 120, z: 0 }, { timeoutMs: 5_000 });
      await commands.assert("/execute if dimension minecraft:the_nether run say Bonded server responsive");
      await waitForBlock(ctx, "minecraft:air", false);
    } finally {
      await commands.batch([
        "/clear @s",
        "/gamemode creative @s",
        "/execute in minecraft:the_nether run fill -2 119 -3 2 124 2 minecraft:air replace",
        "/execute in minecraft:overworld run tp @s 0 72 0",
      ]);
    }
  });
});

async function waitForBlock(
  ctx: TeaKitTestContext,
  block: string,
  place: boolean,
): Promise<void> {
  const deadline = Date.now() + 10_000;
  while (Date.now() < deadline) {
    if (place) {
      await ctx.commands.run(
        `/execute in minecraft:the_nether run setblock 0 120 0 ${block}`,
        { requireSuccess: false },
      );
    }
    const result = await ctx.commands.run(
      `/execute in minecraft:the_nether if block 0 120 0 ${block}`,
      { requireSuccess: false },
    );
    if (result.success) return;
    await ctx.runtime.wait(100);
  }
  throw new Error(`Nether test block did not become ${block}`);
}
