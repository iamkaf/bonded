import { Capability, Readiness, describe, test } from "@teakit/test";

describe.configure({
  timeout: "1m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [Capability.ServerCommands],
});

describe("Bonded weapon experience", () => {
  test("awards experience when a hit kills the target", async ({ commands, runtime }) => {
    try {
      await commands.run("/gamemode survival @s", { requireSuccess: false });
      await commands.run("/clear @s", { requireSuccess: false });
      await commands.run("/scoreboard objectives remove bonded_fatal_xp", { requireSuccess: false });
      await commands.assert("/scoreboard objectives add bonded_fatal_xp dummy");
      await commands.assert(
        "/item replace entity @s weapon.mainhand with minecraft:iron_sword[bonded:item_level={experience:0,maxExperience:1000,level:1,bond:0}]",
      );
      const attackDeadline = Date.now() + 30_000;
      let attacked = false;
      let attackOutput = "";
      let lastAttackResult = "";
      while (Date.now() < attackDeadline) {
        const result = await commands.run(
          "/bondeddebug attack-one-health-target",
          {
            captureOutput: true,
            requireSuccess: false,
          },
        );
        lastAttackResult = JSON.stringify(result);
        attackOutput = (result.output ?? []).join("\n");
        if (typeof result.result === "number" && result.result > 0) {
          attacked = true;
          break;
        }
        await runtime.wait(100);
      }
      if (!attacked) {
        throw new Error(
          `Player did not land a lethal full-strength attack within 30 seconds: ${attackOutput}; ${lastAttackResult}`,
        );
      }
      await commands.assert(
        '/execute store result score @s bonded_fatal_xp run data get entity @s SelectedItem.components."bonded:item_level".experience',
      );
      await commands.assert("/execute if score @s bonded_fatal_xp matches 1..");
    } finally {
      await commands.run("/scoreboard objectives remove bonded_fatal_xp", { requireSuccess: false });
      await commands.run("/clear @s", { requireSuccess: false });
      await commands.run("/gamemode creative @s", { requireSuccess: false });
    }
  });
});
