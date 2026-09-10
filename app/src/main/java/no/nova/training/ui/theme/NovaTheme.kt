package no.nova.training.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NovaBackground = Color(0xFF08111B)
val NovaSurface = Color(0xFF0E1B29)
val NovaSurface2 = Color(0xFF132436)
val NovaBorder = Color(0xFF20384F)
val NovaBlue = Color(0xFF168BFF)
val NovaBlueSoft = Color(0xFF163E69)
val NovaGreen = Color(0xFF2ED9A3)
val NovaText = Color(0xFFEAF3FF)
val NovaMuted = Color(0xFF91A8C0)
val NovaDanger = Color(0xFFFF6D7A)

private val NovaColors = darkColorScheme(
    primary = NovaBlue,
    secondary = NovaGreen,
    background = NovaBackground,
    surface = NovaSurface,
    onPrimary = Color.White,
    onBackground = NovaText,
    onSurface = NovaText,
    outline = NovaBorder,
    error = NovaDanger
)

@Composable fun NovaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NovaColors, content = content)
}
