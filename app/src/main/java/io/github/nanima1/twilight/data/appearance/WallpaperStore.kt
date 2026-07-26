package io.github.nanima1.twilight.data.appearance

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import io.github.nanima1.twilight.domain.appearance.ThemePreset
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface WallpaperStore {
    suspend fun import(sourceUri: String, themePreset: ThemePreset): String

    suspend fun removeManaged(uriValue: String?)
}

class FileWallpaperStore(context: Context) : WallpaperStore {
    private val appContext = context.applicationContext
    private val wallpaperDirectory = File(appContext.filesDir, "wallpapers")

    override suspend fun import(
        sourceUri: String,
        themePreset: ThemePreset
    ): String = withContext(Dispatchers.IO) {
        wallpaperDirectory.mkdirs()
        val pendingFile = File.createTempFile("pending-${themePreset.id}-", null, wallpaperDirectory)
        val wallpaperFile = File(
            wallpaperDirectory,
            "$THEME_FILE_PREFIX${themePreset.id}-${UUID.randomUUID()}"
        )
        val source = Uri.parse(sourceUri)

        try {
            appContext.contentResolver.openInputStream(source).use { input ->
                requireNotNull(input) { "The selected image could not be opened." }
                pendingFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var totalBytes = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        totalBytes += read
                        if (totalBytes > MAX_WALLPAPER_BYTES) {
                            throw IOException("The selected image is larger than 32 MB.")
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(pendingFile.path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                throw IOException("The selected file is not a supported image.")
            }
            Files.move(
                pendingFile.toPath(),
                wallpaperFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
            Uri.fromFile(wallpaperFile).toString()
        } finally {
            pendingFile.delete()
        }
    }

    override suspend fun removeManaged(uriValue: String?) = withContext(Dispatchers.IO) {
        val uri = uriValue?.let(Uri::parse) ?: return@withContext
        if (uri.scheme != "file") return@withContext

        val file = File(uri.path.orEmpty()).canonicalFile
        val managedDirectory = wallpaperDirectory.canonicalFile
        val isManagedWallpaper = file.parentFile == managedDirectory &&
            (file.name == LEGACY_WALLPAPER_FILE || file.name.startsWith(THEME_FILE_PREFIX))
        if (isManagedWallpaper) file.delete()
    }

    private companion object {
        const val MAX_WALLPAPER_BYTES = 32L * 1024L * 1024L
        const val THEME_FILE_PREFIX = "theme-"
        const val LEGACY_WALLPAPER_FILE = "current"
    }
}
