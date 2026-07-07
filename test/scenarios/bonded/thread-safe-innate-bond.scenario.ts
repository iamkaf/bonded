import { describe, expect, test } from "@teakit/test";

describe("Bonded innate bond thread-safety debug command", () => {
  test("applies innate bond in a thread-safe way", async ({ server }) => {
    const result = await server.command("bondeddebug innate-bond-thread-safety", {
      timeoutMs: 10_000,
      captureOutput: true,
      requireSuccess: false,
    });

    expect(result.success).toBe(true);
    expect(result.result).toBe(1);
  });
});
