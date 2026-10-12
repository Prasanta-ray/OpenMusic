package com.openmusic.app.ui.theme

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DynamicThemeColors(
    val topGradientColor: Color = Color(0xFF1E1B2E),
    val bottomGradientColor: Color = Color(0xFF121212),
    val accentColor: Color = Color(0xFFD0BCFF)
)

object DynamicThemeHelper {
    suspend fun extractColors(
        context: Context,
        imageUrl: String?,
        defaultTop: Color = Color(0xFF1E1B2E),
        defaultBottom: Color = Color(0xFF121212),
        defaultAccent: Color = Color(0xFFD0BCFF)
    ): DynamicThemeColors = withContext(Dispatchers.IO) {
        if (imageUrl.isNullOrBlank()) {
            return@withContext DynamicThemeColors(defaultTop, defaultBottom, defaultAccent)
        }

        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Required for software pixel access in Palette
                .size(128, 128) // Optimized tiny size for rapid Snapdragon 675 processing
                .build()

            val result = loader.execute(request)
            val bitmap = (result as? SuccessResult)?.drawable?.let { (it as? BitmapDrawable)?.bitmap }
                ?: return@withContext DynamicThemeColors(defaultTop, defaultBottom, defaultAccent)

            val palette = Palette.from(bitmap).generate()

            val dominant = palette.dominantSwatch?.rgb
            val vibrant = palette.vibrantSwatch?.rgb ?: palette.lightVibrantSwatch?.rgb ?: dominant
            val darkVibrant = palette.darkVibrantSwatch?.rgb ?: palette.darkMutedSwatch?.rgb ?: dominant

            val topColor = if (darkVibrant != null) {
                Color(darkVibrant)
            } else if (vibrant != null) {
                Color(vibrant).copy(alpha = 0.8f)
            } else {
                defaultTop
            }

            val accentColor = if (vibrant != null) Color(vibrant) else defaultAccent

            DynamicThemeColors(
                topGradientColor = topColor,
                bottomGradientColor = defaultBottom,
                accentColor = accentColor
            )
        } catch (e: Exception) {
            DynamicThemeColors(defaultTop, defaultBottom, defaultAccent)
        }
    }
}
