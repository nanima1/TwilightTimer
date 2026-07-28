# 3x3 solver engine evaluation

Date: 2026-07-27

## Decision

Use a pinned, unmodified source snapshot of `cs0x7f/min2phase` behind the existing `CubeSolver` interface. Keep the inverse-scramble implementation as the immediate fallback for generated scrambles, and run two-phase optimization away from the Android main thread.

Package the deterministic full-table payload produced by `Tools.saveTo()` and decode it through a project-owned bulk loader. If the resource cannot be read, fall back to runtime table generation so solving remains available.

The selected revision is `4d183b9eff8119cac72bc50ef35a7d8990740e06`. The upstream README offers an MIT license option and documents the public `Search` and `Tools` interfaces. The source snapshot is kept local so builds do not depend on JitPack or mutable GitHub branches.

## Evidence

- The upstream usage guide documents full initialization, `Tools.fromScramble`, `Search.solution`, and an estimated full initialization time of about 200 ms: https://github.com/cs0x7f/min2phase/blob/4d183b9eff8119cac72bc50ef35a7d8990740e06/README.md
- The upstream benchmark reports about 1 MB memory, 195 ms full initialization, and 0.805 ms for a 21-move target on its reference desktop environment: https://github.com/cs0x7f/min2phase/blob/4d183b9eff8119cac72bc50ef35a7d8990740e06/Benchmark.md
- The upstream README includes both GPLv3 and MIT license options. This project selects MIT and packages its text with the solver module: https://github.com/cs0x7f/min2phase/blob/4d183b9eff8119cac72bc50ef35a7d8990740e06/README.md
- A local JDK probe on the selected revision measured 144.9 ms initialization, 5.73 ms first solve, and 1.45 ms average across 100 warm solves. The returned 21-move algorithm replayed to the solved facelet state.
- The API 30 x86_64 release baseline measured 130.53 ms full initialization and 1.95 ms median warm solve time.
- With the compressed precomputed resource, three fresh-process runs measured 17.04 ms, 17.64 ms, and 19.54 ms full-table initialization. First solves measured 2.34 ms, 2.87 ms, and 2.81 ms. The 50-run warm-solve median remained 1.91 ms.
- The table payload is 997,738 bytes before APK compression and 575,195 bytes after compression. Its SHA-256 digest is pinned in the solver regression test.
- Reusing one lazily created, synchronized `Search` workspace reduced the representative warm-solve median from 1.777 ms to 1.729/1.733 ms across two after runs (2.6% mean reduction). The stable slow-tail median moved from 7.404 ms to 6.865/6.926 ms (6.9% mean reduction). These API 30 x86_64 measurements used the same unlocked emulator and benchmark inputs.
- Encoding validated scramble moves directly to min2phase indices removed the intermediate move list, joined string, and second parser pass. On the same unlocked emulator, the representative median moved from 1.806 ms to 1.729/1.747 ms (3.8% mean reduction), while the slow-tail median moved from 7.194 ms to 6.966/6.909 ms (3.6% mean reduction). Both benchmarks reported about 31 fewer allocations per solve.
- The emulator had unlocked clocks, so these values are comparison baselines rather than physical-device acceptance numbers.

## Rejected candidates

### WCA scrambler-min2phase 0.19.2

This is a stable Maven Central artifact, but its published POM selects GPL-3.0. That license is not appropriate for the current Apache-2.0 application distribution strategy.

Source: https://repo.maven.apache.org/maven2/org/worldcubeassociation/tnoodle/scrambler-min2phase/0.19.2/scrambler-min2phase-0.19.2.pom

### cube-kociemba 1.0.2

This Maven Central artifact declares Apache-2.0, but its implementation has not been updated since 2021, pulls Commons IO 2.2, performs file-cache work during coordinate-table initialization, and has quality issues in that initialization path. It is not suitable for the Android performance core.

Sources:

- https://repo.maven.apache.org/maven2/io/github/toger2021/cube-kociemba/1.0.2/cube-kociemba-1.0.2.pom
- https://github.com/toger2021/demo-cube/tree/b6580362c8edb248ee35a980cc24ea018c6e3b32

## Integration rules

1. Load the packaged full tables on a background dispatcher before requesting optimized solutions.
2. Create a new `Search` instance per solve; do not share mutable search state across concurrent calls.
3. Reuse a `Search` workspace only through its synchronized `solution` entry point. Concurrent requests must remain serialized, and superseded work must use cooperative cancellation before the next request acquires the workspace.
4. Validate notation before `Tools.fromScramble`, because its parser ignores unsupported characters.
5. Replay every deterministic test solution back to the solved facelet state.
6. Verify that the packaged table resource matches the current solver layout and pinned digest.
7. Treat emulator measurements as trend evidence only and repeat final performance acceptance on physical Android hardware.
