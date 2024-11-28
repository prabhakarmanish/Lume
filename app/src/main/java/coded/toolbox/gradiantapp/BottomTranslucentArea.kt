package coded.toolbox.gradiantapp

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coded.toolbox.gradiantapp.datastore.toHex
import coded.toolbox.gradiantapp.ui.screens.copyToClipboard
import java.net.URLEncoder

@Composable
fun BottomTranslucentArea(
    colors: List<Color>,
    navController: NavController
) {
    val context = LocalContext.current
    val uniqueColors = colors.toSet()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                val colorString = colors.joinToString(",") { it.toHex() }
                val encodedString = URLEncoder.encode(colorString, Charsets.UTF_8.name())
                navController.navigate("solidColorDetail/$encodedString")
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                uniqueColors.forEach { color ->
                    val hexCode = color.toHex()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(color)
                                .clickable {
                                    copyToClipboard(context, hexCode)
                                    Toast
                                        .makeText(context, "Copied: $hexCode", Toast.LENGTH_SHORT)
                                        .show()
                                }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hexCode,
                            style = TextStyle(color = Color.White, fontSize = 10.sp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

