package coded.toolbox.gradiantapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun createGradientBitmap(colors: List<Color>, width: Int, height: Int, angle: Float = 0f): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // Calculate gradient start and end points based on angle
    val radianAngle = Math.toRadians(angle.toDouble())
    val startX = (width / 2) - (width / 2) * Math.cos(radianAngle).toFloat()
    val startY = (height / 2) - (height / 2) * Math.sin(radianAngle).toFloat()
    val endX = (width / 2) + (width / 2) * Math.cos(radianAngle).toFloat()
    val endY = (height / 2) + (height / 2) * Math.sin(radianAngle).toFloat()

    val gradient = android.graphics.LinearGradient(
        startX, startY, endX, endY,
        colors.map { it.toArgb() }.toIntArray(),
        null,
        android.graphics.Shader.TileMode.CLAMP
    )
    paint.shader = gradient
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    return bitmap
}
