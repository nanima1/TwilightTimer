# Twilight Timer

Twilight Timer is an independently developed Android speedcubing timer. The first alpha focuses on a clear timing surface, a fast local state loop, and a visual system that will grow into themes and custom wallpapers.

## Foundation

- Kotlin and Jetpack Compose
- One-way UI state through `TimerViewModel`
- Pure timer state reducer with unit tests
- Local 3x3 scramble generator
- Dedicated 3x3 solver module with instant inverse and optimized two-phase strategies
- Three persisted theme packs with live switching
- Private custom-wallpaper import with readability and crop-position controls
- Room-backed solve history with +2/DNF penalties and live statistics
- WCA-style 15-second inspection with automatic +2/DNF assignment
- Independent package: `io.github.nanima1.twilight`

## Build

Requirements:

- JDK 17
- Android SDK Platform 35
- PowerShell UTF-8 output

```powershell
$OutputEncoding = [Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$env:JAVA_TOOL_OPTIONS = '-Dfile.encoding=UTF-8'
./gradlew.bat test testDebugUnitTest assembleDebug
```

## Solver benchmark

Run the release microbenchmark on a connected Android device or emulator:

```powershell
./gradlew.bat :benchmark:connectedReleaseAndroidTest
```

The inverse solver prioritizes predictable latency and correctness. The two-phase solver can produce shorter algorithms after its lookup tables are initialized; it must run away from the Android main thread.

## Roadmap

1. Add solve comments and session filters.
2. Add original curated art packs when final artwork is available.
3. Add deeper statistics and accessibility QA.
4. Continue profiling solver initialization and low-end device performance.

## License

Apache-2.0. See [LICENSE](LICENSE).
