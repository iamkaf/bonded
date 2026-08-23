import { Capability, Readiness, describe, test } from "@teakit/test";
import type { ClientScreen, TeaKitTestContext } from "@teakit/test";

describe.configure({
  timeout: "2m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.ServerCommands,
    Capability.ClientScreen,
    Capability.ClientScreens,
    Capability.ClientInput,
    Capability.ClientScreenshot,
    Capability.RuntimeTiming,
  ],
});

describe("Bonded gear-rule screen", () => {
  test("lets an operator add and remove server-owned overrides", async (ctx) => {
    let created = false;
    try {
      await waitForCommand(ctx, "/bondeddebug rules user-count 0");
      await waitForCommand(ctx, "/bondeddebug rules active-user-count 0");
      await ctx.commands.assert("/bondeddebug rules preview-remote-view");
      let screen = await ctx.client.waitForScreen(
        "com.iamkaf.konfig.impl.v1.client.screen.KonfigConfigScreen",
        { timeoutMs: 10_000 },
      );
      screen = await waitForEntry(ctx, "Rules");
      const rules = screen.lists().entries().find((entry) => entry.label.includes("Rules"));
      if (!rules) throw new Error("Missing Bonded gear rules config entry");
      await ctx.client.click({
        x: rules.x + rules.width * 0.75,
        y: rules.y + rules.height / 2,
        button: 0,
      });

      screen = await ctx.client.waitForScreen(
        "com.iamkaf.konfig.impl.v1.client.fieldset.KonfigFieldsetCatalogScreen",
        { timeoutMs: 10_000 },
      );
      assertActive(screen, ["New Override", "Done"]);
      assertCatalogProfile(screen, "Bonded");
      assertCatalogProfile(screen, "User Overrides");

      await screen.widgets().find("New Override").click();
      created = true;
      await waitForCommand(ctx, "/bondeddebug rules user-count 1");
      await waitForCommand(ctx, "/bondeddebug rules active-user-count 1", ["minecraft:iron_sword"]);
      await ctx.commands.assert("/item replace entity @s weapon.mainhand with minecraft:iron_sword");
      await waitForCommand(ctx, "/bondeddebug rules query", ["cap=1000", "source=User"]);
      screen = await waitForActiveWidget(ctx, "Delete");
      await ctx.client.screenshot("bonded-gear-rules-catalog-live-override");

      await screen.widgets().find("Delete").click();
      created = false;
      await waitForCommand(ctx, "/bondeddebug rules user-count 0");
      await waitForCommand(ctx, "/bondeddebug rules active-user-count 0");
      await waitForCommand(ctx, "/bondeddebug rules query", ["cap=100", "source=Bonded"]);
    } finally {
      if (created) {
        const screen = await ctx.client.screen();
        const deleteButton = screen.widgets().all().find((widget) => widget.label === "Delete" && widget.active);
        if (deleteButton) {
          await screen.widgets().find("Delete").click();
          await waitForCommand(ctx, "/bondeddebug rules user-count 0");
          await waitForCommand(ctx, "/bondeddebug rules active-user-count 0");
        }
      }
      await ctx.commands.run("/clear @s", { requireSuccess: false });
    }
  });
});

async function waitForEntry(ctx: TeaKitTestContext, label: string): Promise<ClientScreen> {
  const startedAt = Date.now();
  let screen = await ctx.client.screen();
  while (Date.now() - startedAt < 10_000) {
    const list = screen.widgets().all().find((widget) => widget.widgetClass.includes("KonfigEntryList"));
    const entry = screen.lists().entries().find((candidate) => candidate.label.includes(label));
    if (list && entry && entry.y >= list.y && entry.y + entry.height <= list.y + list.height) return screen;
    await screen.scroll({ vertical: -2 });
    await ctx.runtime.wait(100);
    screen = await ctx.client.screen();
  }
  const labels = screen.lists().entries().map((entry) => entry.label).join(", ");
  throw new Error(`Missing Bonded gear rules config entry; observed: ${labels}`);
}

async function waitForActiveWidget(ctx: TeaKitTestContext, label: string): Promise<ClientScreen> {
  const deadline = Date.now() + 10_000;
  let screen = await ctx.client.screen();
  while (Date.now() < deadline) {
    const widget = screen.widgets().all().find((candidate) => candidate.label === label);
    if (widget?.active) return screen;
    await ctx.runtime.wait(100);
    screen = await ctx.client.screen();
  }
  throw new Error(`Expected ${label} to become active`);
}

function assertCatalogProfile(screen: ClientScreen, label: string): void {
  if (!screen.lists().entries().some((entry) => entry.label === label)) {
    const observed = screen.lists().entries().map((entry) => entry.label).join(", ");
    throw new Error(`Missing ${label} catalog entry; observed: ${observed}`);
  }
}

function assertActive(screen: ClientScreen, labels: readonly string[]): void {
  for (const label of labels) {
    const widget = screen.widgets().all().find((candidate) => candidate.label === label);
    if (!widget || !widget.active) {
      throw new Error(`Expected ${label} to be present and active`);
    }
  }
}

async function waitForCommand(
  ctx: TeaKitTestContext,
  command: string,
  outputContains: readonly string[] = [],
): Promise<void> {
  const deadline = Date.now() + 10_000;
  let lastOutput = "";
  while (Date.now() < deadline) {
    const result = await ctx.commands.run(command, { captureOutput: true, requireSuccess: false });
    lastOutput = (result.output ?? []).join("\n");
    if (result.success && outputContains.every((expected) => lastOutput.includes(expected))) return;
    await ctx.runtime.wait(100);
  }
  throw new Error(`Command did not become true: ${command}; output: ${lastOutput}`);
}
