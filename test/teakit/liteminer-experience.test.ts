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
    Capability.WorldBlock,
    Capability.WorldClear,
    Capability.WorldFill,
    Capability.WorldSetBlock,
  ],
});

describe("Bonded Liteminer integration", () => {
  test("awards the exact diminishing experience curve for vein-mined blocks", async (ctx) => {
    const min = { x: -2, y: 69, z: -2 };
    const max = { x: 5, y: 74, z: 8 };
    const cases = [
      { blockCount: 1, expectedExperience: 10 },
      { blockCount: 2, expectedExperience: 15 },
      { blockCount: 3, expectedExperience: 18 },
      { blockCount: 4, expectedExperience: 21 },
    ];
    let guiHidden = false;

    try {
      await ctx.client.keyState(96, false);
      await ctx.player.reset({ gameMode: "survival", inventory: "clear" });
      await ctx.world.clear(min, max);
      await ctx.world.fill({ x: -2, y: 69, z: -2 }, { x: 5, y: 69, z: 8 }, "minecraft:stone");
      await ctx.client.command("/liteminer shape set 0");

      for (let index = 0; index < cases.length; index++) {
        const { blockCount, expectedExperience } = cases[index];
        const z = index * 2;

        await ctx.commands.assert(
          "/item replace entity @s weapon.mainhand with minecraft:iron_pickaxe[bonded:item_level={experience:0,maxExperience:1000,level:1,bond:0}]",
        );
        await ctx.world.fill({ x: 0, y: 70, z }, { x: blockCount - 1, y: 70, z }, "minecraft:iron_ore");
        await ctx.player.teleport({ x: 0, y: 70, z: z - 1 });
        await ctx.client.lookAt({ x: 0.5, y: 70.5, z: z + 0.5 });
        await ctx.client.keyState(96, true);
        await ctx.runtime.wait(1_200);

        if (blockCount === 4) {
          await ctx.client.screenshot("bonded-liteminer-experience-hud", { hideOverlay: false });
        }

        await ctx.player.mine({ x: 0, y: 70, z }, { timeoutMs: 5_000 });
        await ctx.client.keyState(96, false);
        if (blockCount === 4) {
          await ctx.runtime.wait(150);
          await ctx.client.keyState(290, true);
          await ctx.client.keyState(290, false);
          guiHidden = true;
          await ctx.client.screenshot("bonded-liteminer-experience-popup", { hideOverlay: true });
          await ctx.client.keyState(290, true);
          await ctx.client.keyState(290, false);
          guiHidden = false;
        }
        await ctx.runtime.wait(500);

        await ctx.commands.assert(
          `/execute if items entity @s weapon.mainhand minecraft:iron_pickaxe[bonded:item_level~{experience:${expectedExperience}}]`,
        );
      }
    } finally {
      if (guiHidden) {
        await ctx.client.keyState(290, true);
        await ctx.client.keyState(290, false);
      }
      await ctx.client.keyState(96, false);
      await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
      await ctx.world.clear(min, max);
    }
  });
});
