import { Capability, Readiness, describe, test } from "@teakit/test";
import type { ClientScreen, TeaKitTestContext } from "@teakit/test";

describe.configure({
  timeout: "2m",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [
    Capability.ServerCommands,
    Capability.ClientScreen,
    Capability.ClientScreens,
    Capability.ClientScreenshot,
    Capability.RuntimeTiming,
  ],
});

describe("Bonded gear-rule screen", () => {
  test("shows the remote snapshot without editable controls", async (ctx) => {
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
      "com.iamkaf.konfig.impl.v1.client.fieldset.KonfigFieldsetListScreen",
      { timeoutMs: 10_000 },
    );
    assertInactive(screen, ["Add", "Copy", "Delete", "Up", "Down"]);
    const firstRule = screen.lists().entries()[0];
    if (!firstRule) throw new Error("Remote gear-rule snapshot is empty");
    await ctx.client.click({
      x: firstRule.x + firstRule.width / 2,
      y: firstRule.y + 20,
      button: 0,
    });
    await ctx.runtime.wait(250);
    screen = await ctx.client.screen();
    assertInactive(screen, ["Add", "Copy", "Delete", "Up", "Down"]);
    await ctx.client.screenshot("bonded-gear-rules-remote-read-only");
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

function assertInactive(screen: ClientScreen, labels: readonly string[]): void {
  for (const label of labels) {
    const widget = screen.widgets().all().find((candidate) => candidate.label === label);
    if (!widget || widget.active) {
      throw new Error(`Expected ${label} to be present and inactive`);
    }
  }
}
