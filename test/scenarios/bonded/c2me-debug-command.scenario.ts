import { describe, expect, test } from "@teakit/test";

describe("Bonded C2ME debug command", () => {
  test("does not use server level random off-thread", async ({ server }) => {
    const result = await server.command("bondeddebug c2me-random", {
      timeoutMs: 10_000,
      captureOutput: true,
      requireSuccess: false,
    });

    expect(result.success).toBe(true);
    expect(result.result).toBe(1);
  });
});
