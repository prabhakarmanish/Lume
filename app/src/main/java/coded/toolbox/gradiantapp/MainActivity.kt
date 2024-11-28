package coded.toolbox.gradiantapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coded.toolbox.gradiantapp.ui.screens.DetailScreen
import coded.toolbox.gradiantapp.ui.theme.GradiantAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GradiantAppTheme {
                GradientApp()
            }
        }
    }
}

@Composable
fun GradientApp() {
    val navController = rememberNavController()

    // Get the current route to determine whether to show the bottom bar
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Only show bottom bar if the current route is not 'solidColorDetail'
            if (currentRoute != "solidColorDetail/{gradientColors}") {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "collection", // Starting route
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("collection") {
                FirstScreen(navController = navController)
            }
            composable("create") {
                GradientMakerScreen()
            }
            composable("my_gradients") {
                MyGradientsScreen(navController = navController)
            }
            composable("solidColorDetail/{gradientColors}") { backStackEntry ->
                val gradientColors = backStackEntry.arguments?.getString("gradientColors")
                DetailScreen(gradientColors)
            }
        }
    }
}
