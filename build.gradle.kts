plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}

val configuredBuildRoot = providers.environmentVariable("TWILIGHT_GRADLE_BUILD_ROOT").orNull
val defaultWindowsBuildRoot =
    providers.environmentVariable("LOCALAPPDATA").orNull
        ?.let { "$it/Temp/twilight-timer-gradle-build" }

(configuredBuildRoot ?: defaultWindowsBuildRoot)?.let { buildRootPath ->
    val buildRoot = file(buildRootPath)

    allprojects {
        layout.buildDirectory.set(
            buildRoot.resolve(if (path == ":") "root" else path.removePrefix(":"))
        )
    }
}
