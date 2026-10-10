import { Capability, Readiness, describe, test } from "@teakit/test";
import type { TeaKitTestContext } from "@teakit/test";

const TOOL_BENCH = { x: 2, y: 71, z: 0 };

const TIERS = ["wooden", "stone", "copper", "iron", "golden", "diamond", "netherite"] as const;
const NEXT_TIER: Record<string, string | undefined> = {
  wooden: "stone",
  stone: "copper",
  copper: "iron",
  iron: "diamond",
  golden: "diamond",
  diamond: "netherite",
};
const AXE_FAMILIES = new Set(["battleaxe", "hatchet"]);
const CORE_FAMILIES = [
  "battleaxe", "broadsword", "greatsword", "hammer", "hatchet",
  "katana", "longsword", "scythe", "sickle", "warhammer",
];
const NEWER_FAMILIES = ["glaive", "halberd", "morningstar", "rapier"];

interface ExpectedRule {
  item: string;
  type: string;
  upgrade?: string;
}

const CORE_EXTRAS: ExpectedRule[] = [
  { item: "weaponsexpanded:longbow", type: "ranged_weapon" },
  { item: "weaponsexpanded:chain_crossbow", type: "ranged_weapon" },
];
const NEWER_EXTRAS: ExpectedRule[] = [
  { item: "weaponsexpanded:ritual_dagger", type: "melee_weapon" },
];

function weaponRules(families: readonly string[]): ExpectedRule[] {
  return TIERS.flatMap((tier) => families.map((family) => {
    const next = NEXT_TIER[tier];
    return {
      item: `weaponsexpanded:${tier}_${family}`,
      type: AXE_FAMILIES.has(family) ? "mining_tool" : "melee_weapon",
      upgrade: next === undefined ? undefined : `weaponsexpanded:${next}_${family}`,
    };
  }));
}

describe.configure({
  timeout: "3m",
  tags: ["compat-fixture"],
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.PlayerInteractions,
    Capability.PlayerReset,
    Capability.PlayerTeleport,
    Capability.ServerCommands,
  ],
});

describe("Bonded Weapons Expanded gear rules", () => {
  test("resolves every Weapons Expanded rule on the installed release", {
    target: { minecraft: "26.1.2", loader: ["fabric", "neoforge"], mods: "weaponsexpanded" },
  }, async (ctx) => {
    try {
      // A dormant or invalid rule leaves its item unclaimed, so each item proves its rule resolved.
      const expectations = [...weaponRules(CORE_FAMILIES), ...CORE_EXTRAS];
      // Older ports lack the 2.0 weapons, and the second profile's marker keeps it out there.
      const newerWeapons = await ctx.commands.run(
        "/item replace entity @s weapon.mainhand with weaponsexpanded:iron_rapier",
        { requireSuccess: false },
      );
      if (newerWeapons.success) {
        expectations.push(...weaponRules(NEWER_FAMILIES), ...NEWER_EXTRAS);
      }
      for (const rule of expectations) {
        await expectHeldRule(ctx, rule.item, [
          `Bonded rule ${rule.item}: enabled=true, type=${rule.type}`,
          `upgrade=${rule.upgrade ?? "none"}`,
          "source=Weapons Expanded",
        ]);
      }
    } finally {
      await ctx.commands.run("/clear @s", { requireSuccess: false });
    }
  });

  test("upgrades a Weapons Expanded weapon at the Tool Bench without discarding item data", {
    target: { minecraft: "26.1.2", loader: ["fabric", "neoforge"], mods: "weaponsexpanded" },
  }, async (ctx) => {
    try {
      await ctx.player.reset({ gameMode: "survival", inventory: "clear" });
      await ctx.commands.batch([
        "/fill -3 70 -3 4 75 2 minecraft:air replace",
        "/fill -3 70 -3 4 70 2 minecraft:stone replace",
        "/setblock 2 71 0 bonded:tool_bench",
        '/item replace entity @s weapon.mainhand with weaponsexpanded:iron_katana[minecraft:damage=23,minecraft:enchantments={"minecraft:sharpness":2},minecraft:custom_data={weapons_expanded_sentinel:1b},bonded:item_level={experience:0,maxExperience:1000,level:10,bond:512}]',
        "/item replace entity @s hotbar.1 with minecraft:diamond 1",
      ]);
      await ctx.player.teleport({ x: 2, y: 72, z: -1 });
      await ctx.player.useBlockServer(TOOL_BENCH, { face: "up", hand: "main_hand" });

      const upgraded = "weaponsexpanded:diamond_katana";
      await ctx.commands.assert(
        `/execute if items entity @s weapon.mainhand ${upgraded}[minecraft:damage=23,minecraft:custom_data~{weapons_expanded_sentinel:1b}]`,
      );
      await ctx.commands.assert(
        `/execute if items entity @s weapon.mainhand ${upgraded}[minecraft:enchantments={"minecraft:sharpness":2},bonded:item_level~{level:1,bond:512}]`,
      );
      await ctx.commands.assert("/execute unless items entity @s inventory.* minecraft:diamond");
    } finally {
      await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
      await ctx.commands.run("/fill -3 70 -3 4 75 2 minecraft:air replace", {
        requireSuccess: false,
      });
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
    expectOutputContains: [`Bonded rule ${item}`, ...expectedOutput],
    requireSuccess: true,
  });
}
