package coded.toolbox.gradiantapp

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun GradientMakerScreen(modifier: Modifier = Modifier) {
    val selectedColorsFlow = remember { MutableStateFlow(mutableListOf<Color>()) }
    var isColorPickerOpen by remember { mutableStateOf(false) }
    var currentColor by remember { mutableStateOf(Color.Gray) }
    var selectedColorIndex by remember { mutableIntStateOf(0) }
    val colorPickerController = rememberColorPickerController()
    var gradientRotation by remember { mutableIntStateOf(45) }
    var verticalOffset by remember { mutableIntStateOf(1200) }

    // Collect selected colors from Flow
    val selectedColors by selectedColorsFlow.collectAsState()

    // Gradient Brush logic
    val gradientBrush by remember {
        derivedStateOf {
            if (selectedColors.size >= 2) {
                Brush.linearGradient(
                    colors = selectedColors,
                    start = Offset(0f, verticalOffset.toFloat()),
                    end = Offset(
                        1000f * kotlin.math.cos(gradientRotation * Math.PI / 180).toFloat(),
                        1000f * kotlin.math.sin(gradientRotation * Math.PI / 180).toFloat() + verticalOffset
                    ),
                    tileMode = TileMode.Clamp
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(Color.Gray, Color.LightGray),
                    start = Offset(0f, verticalOffset.toFloat()),
                    end = Offset(100f, 100f + verticalOffset),
                    tileMode = TileMode.Clamp
                )
            }
        }
    }



    // Scaffold layout
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Generate random colors between 2 to 5 colors
                    val randomColors = List((2..5).random()) {
                        Color( // Generate random colors
                            red = (0..255).random() / 255f,
                            green = (0..255).random() / 255f,
                            blue = (0..255).random() / 255f
                        )
                    }
                    // Convert to MutableList and update selectedColorsFlow
                    selectedColorsFlow.value = randomColors.toMutableList()
                },
                modifier = Modifier
                    .size(40.dp) // Define the size of the floating action button (circle size)
                    .offset(y = (-43).dp) // Adjust vertical position
                    .offset(x = (-16).dp) // Adjust horizontal position
                    .clip(CircleShape) // Ensure the FAB background is circular
                    .background(MaterialTheme.colorScheme.onBackground) // Make the outer circle transparent
                    .padding(0.dp) // Ensure no extra padding around the FAB
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.suffle),
                    contentDescription = "Shuffle Colors",
                    tint = MaterialTheme.colorScheme.onBackground, // Icon color
                    modifier = Modifier
                        .size(40.dp) // Set the exact size of the icon (35.dp)
                        .clip(CircleShape) // Ensure the icon is clipped into a circular shape
                        .padding(0.dp) // Ensure no extra padding around the icon
                )
            }
        }


    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // Gradient Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .fillMaxHeight(0.9f) // Adjust the height of the card
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = gradientBrush)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp), // Spacing between groups
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color Selector (LazyRow for icons)
                LazyRow(
                    modifier = Modifier
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // Maintain spacing
                ) {
                    items(5) { index ->
                        IconCard(
                            color = selectedColors.getOrNull(index) ?: Color.Transparent,
                            onClick = {
                                isColorPickerOpen = true
                                selectedColorIndex = index
                            },
                            showIcon = selectedColors.getOrNull(index) == null
                        )
                    }
                }

                // Vertical Divider (Line between the two groups of icons)
                Box(
                    modifier = Modifier
                        .width(2.dp) // Line width
                        .height(40.dp) // Height to match icon size
                        .background(Color.Gray) // Line color
                )

                Spacer(modifier = Modifier.width(0.dp)) // Padding after the divider

                // Rotation and Offset Controls (Icons)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // Space between the rotation icons
                ) {

                    IconControl(
                        iconRes = R.drawable.left,
                        onClick = { verticalOffset -= 100 }
                    )
                    IconControl(
                        iconRes = R.drawable.right,
                        onClick = { verticalOffset += 100 }
                    )
                    IconControl(iconRes = R.drawable.reloading,
                        onClick = { gradientRotation = (gradientRotation + 15) % 360 })
                }
            }
        }

        // Color Picker Dialog
        if (isColorPickerOpen) {
            Dialog(onDismissRequest = { isColorPickerOpen = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        HsvColorPicker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            controller = colorPickerController,
                            onColorChanged = { colorEnvelope ->
                                currentColor = colorEnvelope.color
                                // Update color in the flow, but prevent overwriting earlier colors
                                val updatedColors = selectedColors.toMutableList().apply {
                                    // Only update the selected index, do not overwrite previous colors
                                    if (selectedColorIndex >= size) {
                                        add(currentColor) // Add new color if index is out of bounds
                                    } else {
                                        this[selectedColorIndex] =
                                            currentColor // Modify selected color
                                    }
                                }
                                selectedColorsFlow.value = updatedColors
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        BrightnessSlider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(35.dp),
                            controller = colorPickerController
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        AlphaSlider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(35.dp),
                            controller = colorPickerController
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(currentColor, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    // Finalize color selection and close dialog
                                    val updatedColors = selectedColors.toMutableList().apply {
                                        if (selectedColorIndex >= size) {
                                            add(currentColor) // Add new color if index is out of bounds
                                        } else {
                                            this[selectedColorIndex] =
                                                currentColor // Modify selected color
                                        }
                                    }
                                    selectedColorsFlow.value = updatedColors
                                    isColorPickerOpen = false
                                }
                            ) {
                                Text("Apply Color")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IconCard(
    color: Color,
    onClick: () -> Unit,
    showIcon: Boolean,
    iconResourceId: Int = R.drawable.addicon
) {
    Box(
        modifier = Modifier
            .size(33.dp)
            .clip(CircleShape)
            .background(color = if (!showIcon) color else Color.Transparent)
            .clickable(
                onClick = { onClick() },
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (showIcon) {
            Image(
                painter = painterResource(id = iconResourceId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun IconControl(iconRes: Int, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = "Shift Icon",
        tint = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .size(33.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true), // Add ripple effect
                onClick = onClick // Action on click
            )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewGradientMakerScreen() {
    GradientMakerScreen(modifier = Modifier)
}


