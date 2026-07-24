# Twilight Timer

Twilight Timer is an independently developed Android speedcubing timer. The first alpha focuses on a clear timing surface, a fast local state loop, and a visual system that will grow into themes and custom wallpapers.

## Foundation

- Kotlin and Jetpack Compose
- One-way UI state through `TimerViewModel`
- Pure timer state reducer with unit tests
- Local 3x3 scramble generator
- Independent package: `io.github.nanima1.twilight`

## Build

Requirements:

- JDK 17
- Android SDK Platform 35
- PowerShell UTF-8 output

```powershell
$OutputEncoding = [Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$env:JAVA_TOOL_OPTIONS = '-Dfile.encoding=UTF-8'
./gradlew.bat testDebugUnitTest assembleDebug
```

## Roadmap

1. Persist sessions and build the solve-history workflow.
2. Add wallpaper import, theme packs, and readability controls.
3. Build a dedicated solver module with repeatable performance benchmarks.
4. Add timer inspection, statistics, and accessibility QA.

## License

Apache-2.0. See [LICENSE](LICENSE).
