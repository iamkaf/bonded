import { Capability, Readiness, describe, test } from "@teakit/test";
import type { TeaKitTestContext } from "@teakit/test";

describe.configure({
  timeout: "2m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [Capability.PlayerReset, Capability.ServerCommands],
});

async function gameVersion(ctx: TeaKitTestContext): Promise<[number, number, number]> {
  const version = (await ctx.runtime.health()).minecraftVersion ?? "";
  const [major = 0, minor = 0, patch = 0] = version.split(".").map((part) => Number.parseInt(part, 10) || 0);
  return [major, minor, patch];
}

/** Before 1.21.5 the attribute_modifiers component wraps its entries in a modifiers list. */
async function usesModifiersWrapper(ctx: TeaKitTestContext): Promise<boolean> {
  const [major, minor, patch] = await gameVersion(ctx);
  return major === 1 && (minor < 21 || (minor === 21 && patch < 5));
}

/** Before 1.21.2 attribute ids carry a generic. prefix. */
async function attributePrefix(ctx: TeaKitTestContext): Promise<string> {
  const [major, minor, patch] = await gameVersion(ctx);
  return major === 1 && (minor < 21 || (minor === 21 && patch < 2)) ? "minecraft:generic." : "minecraft:";
}

describe("Bonded attribute modifiers", () => {
  test("preserves third-party modifiers that share a vanilla modifier id", async (ctx) => {
    try {
      await ctx.player.reset({ gameMode: "survival", inventory: "clear" });
      const wrapped = await usesModifiersWrapper(ctx);
      const attribute = await attributePrefix(ctx);
      const entry = `{type:"${attribute}movement_speed",id:"minecraft:armor.leggings",amount:0.05d,operation:"add_value",slot:"legs"}`;
      const component = wrapped ? `{modifiers:[${entry}]}` : `[${entry}]`;
      const path = wrapped ? 'SelectedItem.components."minecraft:attribute_modifiers".modifiers' : 'SelectedItem.components."minecraft:attribute_modifiers"';
      await ctx.commands.assert(
        `/item replace entity @s weapon.mainhand with minecraft:iron_leggings[minecraft:attribute_modifiers=${component},bonded:item_level={experience:0,maxExperience:1000,level:1,bond:0}]`,
      );

      await ctx.commands.assert("/bonded xp add @s 1 points");

      await ctx.commands.assert(
        `/data get entity @s ${path}[{type:"${attribute}movement_speed",id:"minecraft:armor.leggings"}]`,
      );
      await ctx.commands.assert(
        `/data get entity @s ${path}[{type:"${attribute}armor",id:"minecraft:armor.leggings"}]`,
      );
      await ctx.commands.assert(
        `/data get entity @s ${path}[{type:"${attribute}armor_toughness",id:"minecraft:armor.leggings"}]`,
      );
    } finally {
      await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
    }
  });
});
