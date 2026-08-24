import { Capability, Readiness, describe, test } from "@teakit/test";
import type { ClientScreen, ScreenListEntrySnapshot, TeaKitTestContext } from "@teakit/test";

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
      let screen = await openCatalog(ctx);
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
      await waitForCommand(ctx, "/bondeddebug rules user-count 0");
      await waitForCommand(ctx, "/bondeddebug rules active-user-count 0");
      created = false;
      await waitForCommand(ctx, "/bondeddebug rules query", ["cap=100", "source=Bonded"]);
    } finally {
      if (created) {
        await deleteCreatedOverride(ctx);
      }
      await closeScreenStack(ctx);
      await ctx.commands.run("/clear @s", { requireSuccess: false });
    }
  });
});

async function openCatalog(ctx: TeaKitTestContext): Promise<ClientScreen> {
  await ctx.commands.assert("/bondeddebug rules preview-remote-view");
  const configScreen = "com.iamkaf.konfig.impl.v1.client.screen.KonfigConfigScreen";
  const catalogScreen = "com.iamkaf.konfig.impl.v1.client.fieldset.KonfigFieldsetCatalogScreen";
  const deadline = Date.now() + 10_000;
  let screen = await ctx.client.screen();
  while (Date.now() < deadline) {
    if (screen.screenClass === catalogScreen) return screen;
    if (screen.screenClass === configScreen) break;
    await ctx.runtime.wait(100);
    screen = await ctx.client.screen();
  }
  if (screen.screenClass !== configScreen) {
    throw new Error(`Expected the Bonded config or Gear Rules catalog; observed: ${screen.screenClass}`);
  }
  screen = await waitForEntry(ctx, "Rules");
  const rules = screen.lists().entries().find((entry) => entry.label.includes("Rules"));
  if (!rules) throw new Error("Missing Bonded gear rules config entry");
  await clickEntry(ctx, rules, 0.75);
  const catalogDeadline = Date.now() + 10_000;
  let lastScreenClass = screen.screenClass;
  while (Date.now() < catalogDeadline) {
    screen = await ctx.client.screen();
    lastScreenClass = screen.screenClass;
    if (screen.screenClass === catalogScreen) return screen;
    if (screen.screenClass === configScreen) {
      const currentRules = screen.lists().entries().find((entry) => entry.label.includes("Rules"));
      if (currentRules) await clickEntry(ctx, currentRules, 0.75);
    }
    await ctx.runtime.wait(250);
  }
  throw new Error(`Gear Rules catalog did not open; observed: ${lastScreenClass}`);
}

async function deleteCreatedOverride(ctx: TeaKitTestContext): Promise<void> {
  let screen = await ctx.client.screen();
  let deleteButton = screen.widgets().all().find((widget) => widget.label === "Delete" && widget.active);
  if (!deleteButton) {
    await closeScreenStack(ctx);
    screen = await openCatalog(ctx);
    const profile = await waitForListEntry(ctx, "User Overrides");
    await clickEntry(ctx, profile);
    const override = await waitForListEntry(ctx, "minecraft:iron_sword");
    await clickEntry(ctx, override);
    screen = await waitForActiveWidget(ctx, "Delete");
    deleteButton = screen.widgets().all().find((widget) => widget.label === "Delete" && widget.active);
  }
  if (!deleteButton) throw new Error("Could not recover the test Gear Rules override for cleanup");
  await screen.widgets().find("Delete").click();
  await waitForCommand(ctx, "/bondeddebug rules user-count 0");
  await waitForCommand(ctx, "/bondeddebug rules active-user-count 0");
}

async function waitForListEntry(
  ctx: TeaKitTestContext,
  label: string,
): Promise<ScreenListEntrySnapshot> {
  const deadline = Date.now() + 10_000;
  while (Date.now() < deadline) {
    const screen = await ctx.client.screen();
    const entry = screen.lists().entries().find((candidate) => candidate.label === label);
    if (entry) return entry;
    await ctx.runtime.wait(100);
  }
  throw new Error(`Missing ${label} while recovering the Gear Rules test override`);
}

async function clickEntry(
  ctx: TeaKitTestContext,
  entry: { x: number; y: number; width: number; height: number },
  horizontalPosition = 0.5,
): Promise<void> {
  await ctx.client.click({
    x: entry.x + entry.width * horizontalPosition,
    y: entry.y + entry.height / 2,
    button: 0,
  });
}

async function closeScreenStack(ctx: TeaKitTestContext): Promise<void> {
  for (let attempt = 0; attempt < 6; attempt++) {
    const screen = await ctx.client.screen();
    if (!screen.open) return;
    await ctx.client.key(256, { release: true });
    await ctx.runtime.wait(100);
  }

  const screen = await ctx.client.screen();
  throw new Error(`Gear Rules screen did not close during cleanup: ${screen.screenClass}`);
}

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
