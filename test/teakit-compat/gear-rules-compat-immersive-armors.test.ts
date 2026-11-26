import { Capability, Readiness, describe, test } from "@teakit/test";
import type { TeaKitTestContext } from "@teakit/test";

const BENCH = { x: 0, y: 71, z: 0 };

describe.configure({
  timeout: "3m",
  tags: ["compat-fixture"],
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.PlayerInteractions,
    Capability.PlayerInventory,
    Capability.PlayerReset,
    Capability.PlayerTeleport,
    Capability.ServerCommands,
    Capability.WorldClear,
    Capability.WorldFill,
    Capability.WorldSetBlock,
  ],
});

// The wider component suite needs BetterEnd and BetterNether, which cannot load in the 1.21.1 development
// runtime, so Immersive Armors gets its own pass there.
describe("Bonded Immersive Armors compatibility", () => {
  test("classifies Immersive Armors gear and repairs it with its native material", {
    target: { minecraft: ">=1.21.1 <1.22", loader: ["fabric", "neoforge"], mods: "immersive_armors" },
  }, async (ctx) => {
    try {
      await ctx.commands.assert("/item replace entity @s weapon.mainhand with immersive_armors:bone_helmet");
      await ctx.commands.run("/bondeddebug rules query", {
        captureOutput: true,
        expectOutputContains: ["type=armor", "repair=inherit", "source=Immersive Armors"],
        requireSuccess: true,
      });

      await prepareBench(ctx);
      await ctx.commands.assert("/item replace entity @s weapon.mainhand with immersive_armors:bone_helmet[minecraft:damage=1]");
      await ctx.commands.assert("/item replace entity @s hotbar.1 with minecraft:bone");
      await ctx.player.useBlockServer(BENCH, { face: "up", hand: "main_hand" });
      await ctx.player.inventory().waitForItem("immersive_armors:bone_helmet", { slot: 0, timeout: "5s" });
      await ctx.player.inventory().waitForItemAbsent("minecraft:bone", { timeout: "5s" });
      await ctx.commands.assert(
        "/execute if items entity @s weapon.mainhand immersive_armors:bone_helmet[minecraft:damage=0]",
      );
    } finally {
      await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
      await ctx.world.clear({ x: -2, y: 70, z: -2 }, { x: 2, y: 74, z: 2 });
    }
  });
});

async function prepareBench(ctx: TeaKitTestContext): Promise<void> {
  await ctx.player.reset({ gameMode: "survival", inventory: "clear" });
  await ctx.world.clear({ x: -2, y: 70, z: -2 }, { x: 2, y: 74, z: 2 });
  await ctx.world.fill({ x: -2, y: 70, z: -2 }, { x: 2, y: 70, z: 2 }, "minecraft:stone");
  await ctx.world.setBlock(BENCH, "bonded:repair_bench");
  await ctx.player.teleport({ x: 0, y: 72, z: -1 });
}
