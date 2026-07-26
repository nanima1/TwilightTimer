package io.github.nanima1.twilight.data.appearance

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface WallpaperStore {
    suspend fun import(sourceUri: String): String

    suspend fun removeManaged(uriValue: String?)
}

class FileWallpaperStore(context: Context) : WallpaperStore {
    private val appContext = context.applicationContext
    private val wallpaperDirectory = File(appContext.filesDir, "wallpapers")
    private val wallpaperFile = File(wallpaperDirectory, "current")

    override suspend fun import(sourceUri: String): String = withContext(Dispatchers.IO) {
        wallpaperDirectory.mkdirs()
        val pendingFile = File(wallpaperDirectory, "pending")
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
        if (uri.scheme == "file" && File(uri.path.orEmpty()).canonicalFile == wallpaperFile.canonicalFile) {
            wallpaperFile.delete()
        }
    }

    private companion object {
        const val MAX_WALLPAPER_BYTES = 32L * 1024L * 1024L
    }
}
