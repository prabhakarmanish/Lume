package coded.toolbox.gradiantapp.ui.screens

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coded.toolbox.gradiantapp.createGradientBitmap
import java.io.File
import java.io.IOException

private const val WRITE_PERMISSION_REQUEST_CODE = 100

@Composable
fun DetailScreen(gradientColors: String?) {
    var showSetWithDialog by remember { mutableStateOf(false) }
    var bitmapToSetWith by remember { mutableStateOf<Bitmap?>(null) }
    Log.d("DetailScreen", "Received colors: $gradientColors")
    val context = LocalContext.current
    val colors = parseGradientColors(gradientColors, context)
    val gradientBrush = Brush.linearGradient(colors)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showApplyDialog by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Set Home Screen") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomIconWithText(
                icon = Icons.Default.Info,
                text = "Info",
                onClick = { showBottomSheet = true }
            )
            BottomIconWithText(
                icon = Icons.Default.CheckCircle,
                text = "Apply",
                onClick = { showApplyDialog = true }

            )
            BottomIconWithText(
                icon = Icons.Default.ArrowDropDown,
                text = "Save",
                onClick = {
                    saveGradientImage(context, colors)
                }
            )
        }
    }

    if (showBottomSheet) {
        BottomSheetLayout(
            colors = colors,
            onDismiss = { showBottomSheet = false },
        )
    }

    if (showApplyDialog) {
        ApplyWallpaperDialog(
            selectedOption = selectedOption,
            onOptionSelected = { selectedOption = it },
            onCancel = { showApplyDialog = false },
            onApply = {
                applyWallpaper(
                    context,
                    colors,
                    selectedOption
                ) { bitmap ->
                    bitmapToSetWith = bitmap
                    showSetWithDialog = true
                }
                showApplyDialog = false
            }
        )
    }
}

@Composable
fun ApplyWallpaperDialog(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Set Wallpaper") },
        text = {
            Column {
                listOf(
                    "Set Home Screen",
                    "Set Lock Screen",
                    "Set Both",
                    "Set With..."
                ).forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = (selectedOption == option),
                            onClick = { onOptionSelected(option) }
                        )
                        Text(
                            text = option,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onApply) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}


fun applyWallpaper(
    context: Context,
    colors: List<Color>,
    option: String,
    onShowSetWithDialog: (Bitmap) -> Unit
) {
    val bitmap = createGradientBitmap(colors, 1080, 1920, angle = 45f)

    when (option) {
        "Set Home Screen" -> {
            Toast.makeText(context, "Setting wallpaper as Home Screen", Toast.LENGTH_SHORT).show()
            setWallpaper(context, bitmap, WallpaperManager.FLAG_SYSTEM)
        }

        "Set Lock Screen" -> {
            Toast.makeText(context, "Setting wallpaper as Lock Screen", Toast.LENGTH_SHORT).show()
            setWallpaper(context, bitmap, WallpaperManager.FLAG_LOCK)
        }

        "Set Both" -> {
            Toast.makeText(context, "Setting wallpaper for both screens", Toast.LENGTH_SHORT).show()
            setWallpaper(
                context, bitmap, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            )
        }

        "Set With..." -> {
            onShowSetWithDialog(bitmap)
            setWallpaperInWhatsApp(context, bitmap)
        }
    }
}


fun setWallpaperInWhatsApp(context: Context, bitmap: Bitmap) {
    val uri = saveBitmapToUri(context, bitmap)
    if (uri != null) {
        val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Set in WhatsApp"))
    } else {
        Toast.makeText(context, "Failed to prepare image for WhatsApp", Toast.LENGTH_SHORT).show()
    }
}

fun saveBitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val filename = "wallpaper_${System.currentTimeMillis()}.png"
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Wallpapers")
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
        uri
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun setWallpaper(context: Context, bitmap: Bitmap, flag: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        try {
            wallpaperManager.setBitmap(bitmap, null, true, flag)
            Toast.makeText(context, "Wallpaper applied successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to apply wallpaper.", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(
            context,
            "Setting wallpaper is not supported on this device version.",
            Toast.LENGTH_SHORT
        ).show()
    }
}


fun saveGradientImage(context: Context, colors: List<Color>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val bitmap = createGradientBitmap(colors, 1080, 1920, angle = 45f)
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "gradient_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Gradients")
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri).use { outputStream ->
                if (outputStream?.let {
                        bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            it
                        )
                    } == true) {
                    Toast.makeText(context, "Image saved successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to save image.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Failed to create file.", Toast.LENGTH_SHORT).show()
        }
    } else {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val bitmap = createGradientBitmap(colors, 1080, 1920, angle = 45f)
            val isSaved = saveBitmapToLegacyStorage(bitmap, context)
            if (isSaved) {
                Toast.makeText(context, "Image saved successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save image.", Toast.LENGTH_SHORT).show()
            }
        } else if (context is Activity) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_PERMISSION_REQUEST_CODE
            )
        }
    }
}

fun saveBitmapToLegacyStorage(bitmap: Bitmap, context: Context): Boolean {
    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val file = File(directory, "gradient_${System.currentTimeMillis()}.jpg")
    return try {
        file.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}

@Composable
fun BottomIconWithText(icon: ImageVector, text: String, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick?.invoke() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetLayout(colors: List<Color>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Function to copy color code to clipboard
    fun copyColorToClipboard(color: Color) {
        val colorCode = "#${Integer.toHexString(color.toArgb()).uppercase()}"
        clipboardManager.setText(AnnotatedString(colorCode))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Instructions
            Text("Colors", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Tap swatches to copy.", style = MaterialTheme.typography.bodySmall)

            // Display unique colors in LazyRow
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(colors.distinct()) { color ->
                    ColorBox(color = color, onClick = { copyColorToClipboard(color) })
                }
            }
        }
    }
}

@Composable
fun ColorBox(color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#${Integer.toHexString(color.toArgb()).uppercase()}",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(4.dp)
        )
    }
}

fun parseGradientColors(gradientColors: String?, context: Context): List<Color> {
    return if (!gradientColors.isNullOrEmpty()) {
        gradientColors.split(",").mapNotNull {
            try {
                Color(android.graphics.Color.parseColor(it.trim()))
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, "Invalid color format: $it", Toast.LENGTH_SHORT).show()
                null
            }
        }
    } else {
        listOf(Color.Gray, Color.Gray)
    }
}
