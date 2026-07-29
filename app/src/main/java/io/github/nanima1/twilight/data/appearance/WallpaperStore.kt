package io.github.nanima1.twilight.data.appearance

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
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
        val pendingSourceFile = File.createTempFile(
            "pending-${themePreset.id}-",
            ".source",
            wallpaperDirectory
        )
        val pendingWallpaperFile = File.createTempFile(
            "pending-${themePreset.id}-",
            ".wallpaper",
            wallpaperDirectory
        )
        val source = Uri.parse(sourceUri)

        try {
            copySourceToPendingFile(source, pendingSourceFile)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(pendingSourceFile.path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                throw IOException("The selected file is not a supported image.")
            }

            val decoded = BitmapFactory.decodeFile(
                pendingSourceFile.path,
                BitmapFactory.Options().apply {
                    inSampleSize = WallpaperImportSizing.calculateInSampleSize(
                        width = bounds.outWidth,
                        height = bounds.outHeight
                    )
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
            ) ?: throw IOException("The selected file is not a supported image.")
            var wallpaperBitmap = decoded
            try {
                val orientedBitmap = wallpaperBitmap.applyExifOrientation(
                    orientation = pendingSourceFile.readExifOrientation()
                )
                if (orientedBitmap !== wallpaperBitmap) wallpaperBitmap.recycle()
                wallpaperBitmap = orientedBitmap

                val scaledBitmap = wallpaperBitmap.scaleToWallpaperBounds()
                if (scaledBitmap !== wallpaperBitmap) wallpaperBitmap.recycle()
                wallpaperBitmap = scaledBitmap

                val compression = wallpaperBitmap.compression()
                pendingWallpaperFile.outputStream().buffered().use { output ->
                    check(wallpaperBitmap.compress(compression.format, compression.quality, output)) {
                        "The selected image could not be encoded."
                    }
                }
                val wallpaperFile = File(
                    wallpaperDirectory,
                    "$THEME_FILE_PREFIX${themePreset.id}-${UUID.randomUUID()}${compression.extension}"
                )
                Files.move(
                    pendingWallpaperFile.toPath(),
                    wallpaperFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                Uri.fromFile(wallpaperFile).toString()
            } finally {
                wallpaperBitmap.recycle()
            }
        } finally {
            pendingSourceFile.delete()
            pendingWallpaperFile.delete()
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
        const val THEME_FILE_PREFIX = "theme-"
        const val LEGACY_WALLPAPER_FILE = "current"
    }

    private fun copySourceToPendingFile(source: Uri, pendingSourceFile: File) {
        val input = requireNotNull(appContext.contentResolver.openInputStream(source)) {
            "The selected image could not be opened."
        }
        input.use { sourceStream ->
            pendingSourceFile.outputStream().buffered().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var totalBytes = 0L
                while (true) {
                    val read = sourceStream.read(buffer)
                    if (read < 0) break
                    totalBytes += read
                    if (totalBytes > WallpaperImportSizing.MAX_SOURCE_BYTES) {
                        throw IOException("The selected image is larger than 32 MB.")
                    }
                    output.write(buffer, 0, read)
                }
            }
        }
    }
}

private fun File.readExifOrientation(): Int = runCatching {
    ExifInterface(this).getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
}.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

private fun Bitmap.applyExifOrientation(orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(-90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
        else -> return this
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

private fun Bitmap.scaleToWallpaperBounds(): Bitmap {
    val dimensions = WallpaperImportSizing.scaledDimensions(width, height)
    if (dimensions.width == width && dimensions.height == height) return this
    return Bitmap.createScaledBitmap(this, dimensions.width, dimensions.height, true)
}

private fun Bitmap.compression(): WallpaperCompression = if (hasAlpha()) {
    WallpaperCompression(
        format = Bitmap.CompressFormat.PNG,
        quality = 100,
        extension = ".png"
    )
} else {
    WallpaperCompression(
        format = Bitmap.CompressFormat.JPEG,
        quality = 90,
        extension = ".jpg"
    )
}

private data class WallpaperCompression(
    val format: Bitmap.CompressFormat,
    val quality: Int,
    val extension: String
)
