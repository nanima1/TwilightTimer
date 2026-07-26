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
- Room-backed solve history with live session statistics
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

1. Add penalties, comments, and session filters to solve history.
2. Expand appearance with wallpaper positioning and curated art packs.
3. Integrate progressive solution results into the timer UI without blocking interaction.
4. Add timer inspection, statistics, and accessibility QA.

## License

Apache-2.0. See [LICENSE](LICENSE).
