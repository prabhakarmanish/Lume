package coded.toolbox.gradiantapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coded.toolbox.gradiantapp.ui.screens.FavoriteScreen
import coded.toolbox.gradiantapp.ui.screens.SavedGradientsScreen
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun MyGradientsScreen(modifier: Modifier = Modifier,navController: NavController) {
    val pagerState = rememberPagerState() // State to control the pager
    var selectedTabIndex by remember { mutableIntStateOf(0) } // Track selected tab
    val coroutineScope = rememberCoroutineScope() // Coroutine scope for scrollToPage

    val tabs = listOf("Saved", "Favorites") // Tab names

    Scaffold(modifier = Modifier)
    { paddingValues ->
        val padding = paddingValues
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // TabRow at the top with titles for each screen
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                divider = {
                    Divider(
                        thickness = 0.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    ) // Optional divider
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            // Use coroutine to scroll to the page
                            coroutineScope.launch {
                                pagerState.scrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.tertiary else Color.Gray
                            )
                        }
                    )
                }
            }

            // HorizontalPager to enable swipe navigation between screens
            HorizontalPager(
                count = 2, // Number of pages (2 in this case: Saved and Favorites)
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> SavedGradientsScreen(modifier,context = LocalContext.current) // Show Saved gradients
                    1 -> FavoriteScreen(modifier
                        ,navController = navController) // Show Favorite gradients
                }
            }

            // LaunchedEffect to update selectedTabIndex when the pager page is changed by swiping
            LaunchedEffect(pagerState.currentPage) {
                selectedTabIndex = pagerState.currentPage
            }

            // Pager indicators to show which page is active
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                activeColor = MaterialTheme.colorScheme.tertiary,
                inactiveColor = Color.Gray,
                indicatorWidth = 8.dp,
                indicatorHeight = 8.dp
            )
        }
    }
}


