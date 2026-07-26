# 3x3 solver engine evaluation

Date: 2026-07-26

## Decision

Use a pinned, unmodified source snapshot of `cs0x7f/min2phase` behind the existing `CubeSolver` interface. Keep the inverse-scramble implementation as the immediate fallback for generated scrambles, and run two-phase optimization away from the Android main thread.

The selected revision is `4d183b9eff8119cac72bc50ef35a7d8990740e06`. The upstream README offers an MIT license option and documents the public `Search` and `Tools` interfaces. The source snapshot is kept local so builds do not depend on JitPack or mutable GitHub branches.

## Evidence

- The upstream usage guide documents full initialization, `Tools.fromScramble`, `Search.solution`, and an estimated full initialization time of about 200 ms: https://github.com/cs0x7f/min2phase/blob/4d183b9eff8119cac72bc50ef35a7d8990740e06/README.md
- The upstream benchmark reports about 1 MB memory, 195 ms full initialization, and 0.805 ms for a 21-move target on its reference desktop environment: https://github.com/cs0x7f/min2phase/blob/4d183b9eff8119cac72bc50ef35a7d8990740e06/Benchmark.md
- The upstream README includes both GPLv3 and MIT license options. This project selects MIT and packages its text with the solver module: https://github.com/cs0x7f/min2phase/blob/4d183b9eff8119cac72bc50ef35a7d8990740e06/README.md
- A local JDK probe on the selected revision measured 144.9 ms initialization, 5.73 ms first solve, and 1.45 ms average across 100 warm solves. The returned 21-move algorithm replayed to the solved facelet state.
- The API 30 x86_64 release benchmark measured 127.55 ms full initialization and 1.93 ms median warm solve time across 50 runs. The emulator had unlocked clocks, so these values are comparison baselines rather than physical-device acceptance numbers.

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

1. Prewarm full tables on a background dispatcher before requesting optimized solutions.
2. Create a new `Search` instance per solve; do not share mutable search state across concurrent calls.
3. Validate notation before `Tools.fromScramble`, because its parser ignores unsupported characters.
4. Replay every deterministic test solution back to the solved facelet state.
5. Treat emulator measurements as trend evidence only and repeat final performance acceptance on physical Android hardware.
