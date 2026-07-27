import { Capability, Readiness, describe, expect, test } from "@teakit/test";

describe.configure({
  timeout: "30s",
  readiness: [Readiness.World, Readiness.Player],
  capabilities: [Capability.ServerCommands],
});

describe("Bonded innate bond thread safety", () => {
  test("applies innate bond from the server thread", async ({ server }) => {
    // The debug command invokes monster-equipment bonding on a dedicated worker thread
    // and fails if that off-thread call throws. Brigadier returns 1 only on its success path.
    const result = await server.command("bondeddebug innate-bond-thread-safety", {
      timeoutMs: 10_000,
      captureOutput: true,
      requireSuccess: false,
    });

    expect(result.success).toBe(true);
    expect(result.result).toBe(1);
  });
});
