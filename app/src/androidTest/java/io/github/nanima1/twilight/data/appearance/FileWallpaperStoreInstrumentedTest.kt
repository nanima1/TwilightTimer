package io.github.nanima1.twilight.data.appearance

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.nanima1.twilight.domain.appearance.ThemePreset
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileWallpaperStoreInstrumentedTest {
    @Test
    fun importScalesAndCleansUpManagedWallpaper() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sourceFile = File(context.cacheDir, "wallpaper-import-${UUID.randomUUID()}.jpg")
        val sourceBitmap = Bitmap.createBitmap(3_000, 1_800, Bitmap.Config.RGB_565)
        sourceBitmap.eraseColor(Color.rgb(20, 40, 60))
        sourceFile.outputStream().use { output ->
            assertTrue(sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 95, output))
        }
        sourceBitmap.recycle()

        val wallpaperStore = FileWallpaperStore(context)
        val importedUri = wallpaperStore.import(
            sourceUri = Uri.fromFile(sourceFile).toString(),
            themePreset = ThemePreset.NEON_SHRINE
        )
        val importedFile = File(requireNotNull(Uri.parse(importedUri).path))

        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(importedFile.path, bounds)

            assertTrue(importedFile.exists())
            assertEquals("jpg", importedFile.extension)
            assertTrue(importedFile.name.startsWith("theme-neon_shrine-"))
            assertTrue(bounds.outWidth <= WallpaperImportSizing.MAX_WALLPAPER_EDGE_PX)
            assertTrue(bounds.outHeight <= WallpaperImportSizing.MAX_WALLPAPER_EDGE_PX)
        } finally {
            wallpaperStore.removeManaged(importedUri)
            sourceFile.delete()
        }

        assertTrue(!importedFile.exists())
    }
}
