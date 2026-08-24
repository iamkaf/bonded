import { Capability, Readiness, describe, test } from "@teakit/test";

describe.configure({
  timeout: "2m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [Capability.PlayerReset, Capability.ServerCommands],
});

describe("Bonded attribute modifiers", () => {
  test("preserves third-party modifiers that share a vanilla modifier id", async (ctx) => {
    try {
      await ctx.player.reset({ gameMode: "survival", inventory: "clear" });
      await ctx.commands.assert(
        '/item replace entity @s weapon.mainhand with minecraft:iron_leggings[minecraft:attribute_modifiers=[{type:"minecraft:movement_speed",id:"minecraft:armor.leggings",amount:0.05d,operation:"add_value",slot:"legs"}],bonded:item_level={experience:0,maxExperience:1000,level:1,bond:0}]',
      );

      await ctx.commands.assert("/bonded xp add @s 1 points");

      await ctx.commands.assert(
        '/data get entity @s SelectedItem.components."minecraft:attribute_modifiers"[{type:"minecraft:movement_speed",id:"minecraft:armor.leggings"}]',
      );
      await ctx.commands.assert(
        '/data get entity @s SelectedItem.components."minecraft:attribute_modifiers"[{type:"minecraft:armor",id:"minecraft:armor.leggings"}]',
      );
      await ctx.commands.assert(
        '/data get entity @s SelectedItem.components."minecraft:attribute_modifiers"[{type:"minecraft:armor_toughness",id:"minecraft:armor.leggings"}]',
      );
    } finally {
      await ctx.player.reset({ gameMode: "creative", inventory: "clear" });
    }
  });
});
