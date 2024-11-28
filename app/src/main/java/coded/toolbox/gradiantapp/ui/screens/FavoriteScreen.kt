package coded.toolbox.gradiantapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coded.toolbox.gradiantapp.BottomTranslucentArea
import coded.toolbox.gradiantapp.datastore.getFavoriteColors
import coded.toolbox.gradiantapp.datastore.getFavoriteGradients
import coded.toolbox.gradiantapp.datastore.removeGradientFromFavorites
import coded.toolbox.gradiantapp.datastore.removeSolidColorFromFavorites
import coded.toolbox.gradiantapp.datastore.toHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class FavoriteItem {
    data class Gradient(val colors: List<Color>) : FavoriteItem()
    data class SolidColor(val color: Color) : FavoriteItem()
}

@Composable
fun FavoriteScreen(
    modifier: Modifier,
    navController: NavController
) {
    val context = LocalContext.current
    val favoriteItems = remember { mutableStateListOf<FavoriteItem>() }
    val scope = rememberCoroutineScope()

    // Fetch data
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            favoriteItems.clear()

            // Get both gradients and colors and combine them
            val gradients = getFavoriteGradients(context)
            val colors = getFavoriteColors(context)

            // Add to the list
            favoriteItems.addAll(gradients.map { FavoriteItem.Gradient(it) })
            favoriteItems.addAll(colors.map { FavoriteItem.SolidColor(it) })
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(favoriteItems.size) { index ->
            val item = favoriteItems[index]
            when (item) {
                is FavoriteItem.Gradient -> {
                    GradientCardWithRemove(
                        gradientColors = item.colors,
                        onRemove = {
                            val gradientHexes = item.colors.joinToString(",") { it.toHex() }
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    removeGradientFromFavorites(context, gradientHexes)
                                    favoriteItems.removeAt(index)
                                }
                            }
                        },
                        navController = navController
                    )
                }

                is FavoriteItem.SolidColor -> {
                    ColorCardWithRemove(
                        color = item.color,
                        onRemove = {
                            val colorHex = item.color.toHex()
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    removeSolidColorFromFavorites(context, colorHex)
                                    favoriteItems.removeAt(index)
                                }
                            }
                        },
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun ColorCardWithRemove(
    color: Color,
    onRemove: () -> Unit,
    navController: NavController
) {
    val SolidColors = remember { mutableListOf<Color>() }
    SolidColors.add(color)
    // Use a Card similar to the gradient card layout
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
            // Set a background color (like gradient but with a solid color)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = color)
            )


            // Add any translucent area at the bottom, as done with gradients
            BottomTranslucentArea(SolidColors, navController)

            // Position the remove heart icon in the top-end corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                HeartIconWithRemove(
                    onRemove = onRemove,
                    heartColor = color // Pass color to heart icon
                )
            }
        }
    }
}


@Composable
fun HeartIconWithRemove(
    onRemove: () -> Unit,
    heartColor: Color
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable(onClick = onRemove)
            .background(Color.Transparent)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Remove from Favorites",
            modifier = Modifier.fillMaxSize(),
            tint = Color.Red.copy(0.5f)
        )
    }
}

@Composable
fun GradientCardWithRemove(
    gradientColors: List<Color>,
    onRemove: () -> Unit,
    navController: NavController
) {
    val brush = Brush.linearGradient(gradientColors)
    val heartColor = gradientColors.first() // Use the first color for the heart icon color

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
            GradientBackground(brush) // Your custom composable for displaying gradient
            BottomTranslucentArea(gradientColors, navController)

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                HeartIconWithRemove(
                    onRemove = onRemove,
                    heartColor = heartColor
                )
            }
        }
    }
}
