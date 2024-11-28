package coded.toolbox.gradiantapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coded.toolbox.gradiantapp.BottomTranslucentArea
import coded.toolbox.gradiantapp.datastore.getColorsFromDataStore
import coded.toolbox.gradiantapp.datastore.getFavoriteColors
import coded.toolbox.gradiantapp.datastore.removeSolidColorFromFavorites
import coded.toolbox.gradiantapp.datastore.saveColorsToDataStore
import coded.toolbox.gradiantapp.datastore.saveSolidColorToFavorites
import coded.toolbox.gradiantapp.datastore.toHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SolidColorScreen(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    var colorList by remember { mutableStateOf<List<Color>?>(null) }
    var favoriteColors by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Load color data and favorite colors from DataStore
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val savedColors = getColorsFromDataStore(context)
            val savedFavorites = getFavoriteColors(context)
                .map { it.toHex() }
                .toSet()

            // If colors are not saved yet, generate and save random colors
            if (savedColors.isNullOrEmpty()) {
                val randomColors = List(3000) { randomColor() }
                saveColorsToDataStore(context, randomColors)
                colorList = randomColors
            } else {
                colorList = savedColors
            }

            favoriteColors = savedFavorites
        }
    }

    // Show colors once they are loaded
    if (colorList != null) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(colorList!!.size) { index ->
                SolidColorCard(
                    navController,
                    colorList!![index],
                    favoriteColors,
                    onFavoritesUpdated = { updatedFavorites ->
                        favoriteColors = updatedFavorites
                    }
                )
            }
        }
    } else {
        // Show loading indicator while fetching colors
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}


@Composable
fun SolidColorCard(
    navController: NavController,
    color: Color,
    favoriteColors: Set<String>,
    onFavoritesUpdated: (Set<String>) -> Unit
) {
    val colorHex = color.toHex()

    Card(
        modifier = Modifier
            .height(350.dp)
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
            )
            BottomTranslucentArea(listOf(color, color), navController)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                SolidColorHeartIcon(
                    colorHex = colorHex,
                    favoriteColors = favoriteColors,
                    onFavoritesUpdated = onFavoritesUpdated
                )
            }
        }
    }
}

@Composable
fun SolidColorHeartIcon(
    colorHex: String,
    favoriteColors: Set<String>,
    onFavoritesUpdated: (Set<String>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isFavorited by remember { mutableStateOf(favoriteColors.contains(colorHex)) }

    Box(
        modifier = Modifier
            .size(25.dp)
            .clip(CircleShape)
            .clickable {
                isFavorited = !isFavorited
                coroutineScope.launch {
                    if (isFavorited) {
                        // Save solid color to favorites
                        saveSolidColorToFavorites(
                            context,
                            Color(android.graphics.Color.parseColor(colorHex))
                        )
                    } else {
                        // Remove solid color from favorites
                        removeSolidColorFromFavorites(context, colorHex)
                    }
                    // Update the favorite colors
                    val updatedFavorites = getFavoriteColors(context)
                        .map { it.toHex() }
                        .toSet()
                    onFavoritesUpdated(updatedFavorites)
                }
            }
            .background(Color.Transparent)
            .padding(2.dp)
    ) {
        Icon(
            imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorited) "Favorited" else "Not Favorited",
            tint = if (isFavorited) Color.Red.copy(0.5f) else Color.Gray
        )
    }
}
