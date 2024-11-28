package coded.toolbox.gradiantapp.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coded.toolbox.gradiantapp.BottomTranslucentArea
import coded.toolbox.gradiantapp.datastore.getFavoriteGradients
import coded.toolbox.gradiantapp.datastore.getGradientsFromDataStore
import coded.toolbox.gradiantapp.datastore.removeGradientFromFavorites
import coded.toolbox.gradiantapp.datastore.saveGradientToFavorites
import coded.toolbox.gradiantapp.datastore.saveGradientsToDataStore
import coded.toolbox.gradiantapp.datastore.toHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

@Composable
fun AllGradientScreen(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    var gradientList by remember { mutableStateOf<List<List<Color>>?>(null) }
    var favoriteGradients by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Fetch gradients and favorites
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val savedGradients = getGradientsFromDataStore(context)
            val favorites = getFavoriteGradients(context)
                .map { gradient -> gradient.joinToString(",") { color -> color.toHex() } }
                .toSet()
            if (savedGradients.isNullOrEmpty()) {
                val generatedGradients = List(3000) { List(Random.nextInt(2, 5)) { randomColor() } }
                saveGradientsToDataStore(context, generatedGradients)
                gradientList = generatedGradients
            } else {
                gradientList = savedGradients
            }
            favoriteGradients = favorites
        }
    }

    if (gradientList != null) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(gradientList!!.size) { index ->
                GradientCard(
                    navController,
                    gradientList!![index],
                    favoriteGradients,
                    onFavoritesUpdated = { updatedFavorites ->
                        favoriteGradients = updatedFavorites
                    }
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
    }
}


@Composable
fun GradientCard(
    navController: NavController,
    gradientColors: List<Color>,
    favoriteGradients: Set<String>,
    onFavoritesUpdated: (Set<String>) -> Unit
) {
    val brush = Brush.linearGradient(gradientColors)

    Card(
        modifier = Modifier
            .height(350.dp)
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GradientBackground(brush)
            BottomTranslucentArea(gradientColors, navController)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                HeartIcon(
                    gradientColors = gradientColors,
                    favoriteGradients = favoriteGradients,
                    onFavoritesUpdated = onFavoritesUpdated
                )
            }
        }
    }
}


@Composable
fun HeartIcon(
    gradientColors: List<Color>,
    favoriteGradients: Set<String>,
    onFavoritesUpdated: (Set<String>) -> Unit // Callback for updating favorites
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val gradientHex = gradientColors.joinToString(",") { it.toHex() }
    var isFavorited by remember { mutableStateOf(favoriteGradients.contains(gradientHex)) }

    Box(
        modifier = Modifier
            .size(25.dp)
            .clip(CircleShape)
            .clickable {
                isFavorited = !isFavorited
                coroutineScope.launch {
                    if (isFavorited) {
                        saveGradientToFavorites(context, gradientColors)
                    } else {
                        removeGradientFromFavorites(context, gradientHex)
                    }
                    // Update the favorites set
                    val updatedFavorites = getFavoriteGradients(context)
                        .map { gradient -> gradient.joinToString(",") { color -> color.toHex() } }
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


@Composable
fun GradientBackground(brush: Brush) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)

    )
}

fun randomColor(): Color {
    return Color(
        red = Random.nextFloat(),
        green = Random.nextFloat(),
        blue = Random.nextFloat(),
        alpha = 1f
    )
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Color", text)
    clipboard.setPrimaryClip(clip)
}