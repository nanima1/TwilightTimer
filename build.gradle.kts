plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}

val configuredBuildRoot = providers.environmentVariable("TWILIGHT_GRADLE_BUILD_ROOT").orNull
val defaultBuildRoot =
    "${System.getenv("LOCALAPPDATA") ?: "C:/Temp"}/Temp/twilight-timer-gradle-build"
val buildRoot = file(
    configuredBuildRoot ?: defaultBuildRoot
)

allprojects {
    layout.buildDirectory.set(
        buildRoot.resolve(if (path == ":") "root" else path.removePrefix(":"))
    )
}
