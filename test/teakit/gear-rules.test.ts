import { Capability, Readiness, describe, test } from "@teakit/test";
import type { TeaKitTestContext } from "@teakit/test";

describe.configure({
  timeout: "1m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [Capability.ServerCommands],
});

describe("Bonded gear rules", () => {
  test("reports builtin rules and the unknown-gear fallback", async (ctx) => {
    try {
      await expectHeldRule(ctx, "minecraft:iron_sword", [
        "Bonded rule minecraft:iron_sword",
        "type=melee_weapon",
        "cap=100",
        "upgrade=minecraft:diamond_sword",
        "source=Bonded",
      ]);
      await expectHeldRule(ctx, "minecraft:bow", [
        "Bonded rule minecraft:bow",
        "type=ranged_weapon",
        "cap=150",
        "repair=item:minecraft:string",
        "upgrade=none",
        "source=Bonded",
        "anvilRepair=true",
      ]);
      await expectHeldRule(ctx, "minecraft:mace", [
        "Bonded rule minecraft:mace: tag fallback",
        "cap=1000",
      ]);
    } finally {
      await ctx.commands.run("/clear @s", { requireSuccess: false });
    }
  });
});

async function expectHeldRule(
  ctx: TeaKitTestContext,
  item: string,
  expectedOutput: readonly string[],
): Promise<void> {
  await ctx.commands.assert(`/item replace entity @s weapon.mainhand with ${item}`);
  await ctx.commands.run("/bondeddebug rules query", {
    captureOutput: true,
    expectOutputContains: [...expectedOutput],
    requireSuccess: true,
  });
}
