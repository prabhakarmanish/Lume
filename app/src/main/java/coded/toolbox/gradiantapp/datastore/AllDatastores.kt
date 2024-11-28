package coded.toolbox.gradiantapp.datastore

import android.content.Context
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.ui.graphics.Color
import coded.toolbox.gradiantapp.datastore.toHex
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore(name = "gradient_preferences")
val Context.colorDataStore by preferencesDataStore(name = "color_preferences")

// Save a gradient to the favorites list
suspend fun saveGradientToFavorites(context: Context, colors: List<Color>) {
    val gradientColorsKey = stringSetPreferencesKey("favorite_gradients")
    val colorHexes = colors.joinToString(",") { it.toHex() }

    context.dataStore.edit { preferences ->
        val currentFavorites = preferences[gradientColorsKey]?.toMutableSet() ?: mutableSetOf()
        currentFavorites.add(colorHexes)
        preferences[gradientColorsKey] = currentFavorites
    }
}

// Remove a gradient from the favorites list
suspend fun removeGradientFromFavorites(context: Context, gradientHexes: String) {
    val gradientColorsKey = stringSetPreferencesKey("favorite_gradients")

    context.dataStore.edit { preferences ->
        val currentFavorites = preferences[gradientColorsKey]?.toMutableSet() ?: mutableSetOf()
        currentFavorites.remove(gradientHexes)
        preferences[gradientColorsKey] = currentFavorites
    }
}

// Retrieve all favorite gradients
suspend fun getFavoriteGradients(context: Context): List<List<Color>> {
    val gradientColorsKey = stringSetPreferencesKey("favorite_gradients")
    val preferences = context.dataStore.data.first()
    val favoriteHexes = preferences[gradientColorsKey] ?: emptySet()
    return favoriteHexes.mapNotNull { hexString ->
        hexString.split(",").mapNotNull { hexToColor(it) }
    }
}



// Convert a Hex String to a Color
fun hexToColor(hex: String): Color? {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: IllegalArgumentException) {
        null
    }
}
data class GradientProperties(
    val colors: List<Int>, // Color values as ARGB integers
    val type: String,      // Gradient type (linear, radial, etc.)
    val angle: Float,      // Gradient angle (for linear gradients)
    val radius: Float,     // Radius (for radial gradients)
    val tileMode: String   // Tile mode (e.g., CLAMP, REPEAT)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GradientProperties) return false
        return colors == other.colors &&
                type == other.type &&
                angle == other.angle &&
                radius == other.radius &&
                tileMode == other.tileMode
    }

    override fun hashCode(): Int {
        return 31 * colors.hashCode() + type.hashCode() + angle.hashCode() + radius.hashCode() + tileMode.hashCode()
    }
}

// Save a single solid color to DataStore (all colors list)
suspend fun saveColorsToDataStore(context: Context, colors: List<Color>) {
    val colorString = colors.joinToString(",") { it.toHex() }
    val dataStoreKey = stringPreferencesKey("favorite_colors")
    context.colorDataStore.edit { preferences ->
        preferences[dataStoreKey] = colorString
    }
}

// Retrieve colors saved as a single list of solid colors
suspend fun getColorsFromDataStore(context: Context): List<Color>? {
    val dataStoreKey = stringPreferencesKey("favorite_colors")
    val preferences = context.colorDataStore.data.first()
    val colorString = preferences[dataStoreKey]
    return colorString?.split(",")?.mapNotNull { hexToColor(it) }
}

// Save a solid color to the favorites list (same as gradients)
suspend fun saveSolidColorToFavorites(context: Context, color: Color) {
    val favoriteColorsKey = stringSetPreferencesKey("favorite_colors")
    val colorHex = color.toHex()

    context.dataStore.edit { preferences ->
        val currentFavorites = preferences[favoriteColorsKey]?.toMutableSet() ?: mutableSetOf()
        currentFavorites.add(colorHex)
        preferences[favoriteColorsKey] = currentFavorites
    }
}

// Remove a solid color from the favorites list
suspend fun removeSolidColorFromFavorites(context: Context, colorHex: String) {
    val favoriteColorsKey = stringSetPreferencesKey("favorite_colors")

    context.dataStore.edit { preferences ->
        val currentFavorites = preferences[favoriteColorsKey]?.toMutableSet() ?: mutableSetOf()
        currentFavorites.remove(colorHex)
        preferences[favoriteColorsKey] = currentFavorites
    }
}

// Retrieve all favorite solid colors
suspend fun getFavoriteColors(context: Context): List<Color> {
    val favoriteColorsKey = stringSetPreferencesKey("favorite_colors")
    val preferences = context.dataStore.data.first()
    val favoriteHexes = preferences[favoriteColorsKey] ?: emptySet()
    return favoriteHexes.mapNotNull { hexToColor(it) }
}

// Retrieve gradients from DataStore
suspend fun getGradientsFromDataStore(context: Context): List<List<Color>>? {
    val dataStoreKey = stringPreferencesKey("gradient_list")
    val preferences = context.dataStore.data.first()
    val gradientString = preferences[dataStoreKey]
    return gradientString?.split(";")?.map { gradient ->
        gradient.split(",").mapNotNull { hexToColor(it) }
    }
}

// Save multiple gradients to DataStore
suspend fun saveGradientsToDataStore(context: Context, gradients: List<List<Color>>) {
    val dataStoreKey = stringPreferencesKey("gradient_list")
    val gradientStrings = gradients.joinToString(";") { gradient ->
        gradient.joinToString(",") { it.toHex() }
    }
    context.dataStore.edit { preferences ->
        preferences[dataStoreKey] = gradientStrings
    }
}

// Save multiple gradients with all properties to DataStore
suspend fun saveNewGradientsToDataStore(
    context: Context,
    gradients: List<GradientProperties>
) {
    val dataStoreKey = stringPreferencesKey("created_gradient_list")

    // Use the 'colors' property of GradientProperties and join it correctly
    val gradientStrings = gradients.joinToString(";") { gradient ->
        // Correctly access the 'colors' list within the 'gradient' object
        val colorString = gradient.colors.joinToString(",") { Color(it).toHex() } // Assuming 'colors' contains ARGB integer values
        "$colorString|${gradient.type}|${gradient.angle}|${gradient.radius}|${gradient.tileMode}"
    }

    context.dataStore.edit { preferences ->
        preferences[dataStoreKey] = gradientStrings
    }
}


fun Color.toHex(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return String.format("#%02X%02X%02X", red, green, blue)
}

// Retrieve saved gradients with all properties from DataStore
suspend fun getSavedGradientsFromDataStore(context: Context): List<GradientProperties> {
    val dataStoreKey = stringPreferencesKey("created_gradient_list")
    val preferences = context.dataStore.data.first()  // First gets the data (suspending function)
    val gradientString = preferences[dataStoreKey]  // Retrieve the stored gradients string

    // If there are no saved gradients, return an empty list
    return if (gradientString.isNullOrEmpty()) {
        emptyList()
    } else {
        // Convert the gradient string back into a list of GradientProperties objects
        gradientString.split(";").map { gradient ->
            val parts = gradient.split("|")
            val colors = parts[0].split(",").mapNotNull { hexToColor(it) }
            val type = parts[1]
            val angle = parts[2].toFloat()
            val radius = parts[3].toFloat()
            val tileMode = parts[4]

            GradientProperties(colors = colors.map { it.toArgb() }, type = type, angle = angle, radius = radius, tileMode = tileMode)
        }
    }
}

// Remove a gradient with all properties from DataStore
suspend fun removeGradientFromDataStore(
    context: Context,
    gradientToRemove: GradientProperties
) {
    val dataStoreKey = stringPreferencesKey("created_gradient_list")
    val preferences = context.dataStore.data.first()  // Retrieve the data from DataStore
    val gradientString = preferences[dataStoreKey]  // Get the stored gradient list string

    // If no gradients are stored, just return (nothing to remove)
    if (gradientString.isNullOrEmpty()) return

    // Convert the gradient string back into a list of GradientProperties objects
    val gradients = gradientString.split(";").map { gradient ->
        val parts = gradient.split("|")
        val colors = parts[0].split(",").mapNotNull { hexToColor(it) }
        val type = parts[1]
        val angle = parts[2].toFloat()
        val radius = parts[3].toFloat()
        val tileMode = parts[4]

        GradientProperties(colors = colors.map { it.toArgb() }, type = type, angle = angle, radius = radius, tileMode = tileMode)
    }

    // Remove the selected gradient from the list
    val updatedGradients = gradients.filterNot { it == gradientToRemove }

    // If the gradient was removed, save the updated list back to DataStore
    if (updatedGradients.size != gradients.size) {
        saveNewGradientsToDataStore(context, updatedGradients)  // Save the updated list of gradients
    }
}
