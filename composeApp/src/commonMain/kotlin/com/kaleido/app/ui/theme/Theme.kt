package com.kaleido.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Kaleido palette — a warm violet primary with a coral secondary and a mint
// tertiary, so category chips and charts have room to be colourful without
// clashing with the brand.
private val Violet = Color(0xFF6C4CE0)
private val VioletDark = Color(0xFFB9A5FF)
private val Coral = Color(0xFFFF5C7A)
private val CoralDark = Color(0xFFFFB3C0)
private val Mint = Color(0xFF12B981)
private val MintDark = Color(0xFF6EE7B7)

private val LightColors = lightColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBE3FF),
    onPrimaryContainer = Color(0xFF23124F),
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0E6),
    onSecondaryContainer = Color(0xFF4A0016),
    tertiary = Mint,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC7F5E4),
    background = Color(0xFFFBF9FF),
    onBackground = Color(0xFF1B1B21),
    surface = Color.White,
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFECE7F3),
    onSurfaceVariant = Color(0xFF49454E),
    outline = Color(0xFFCAC4D0),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = VioletDark,
    onPrimary = Color(0xFF2B1665),
    primaryContainer = Color(0xFF4B2FB0),
    onPrimaryContainer = Color(0xFFEBE3FF),
    secondary = CoralDark,
    onSecondary = Color(0xFF5E0021),
    secondaryContainer = Color(0xFFB3223F),
    onSecondaryContainer = Color(0xFFFFE0E6),
    tertiary = MintDark,
    onTertiary = Color(0xFF00382A),
    tertiaryContainer = Color(0xFF0B7F5B),
    background = Color(0xFF131218),
    onBackground = Color(0xFFE6E1E9),
    surface = Color(0xFF1B1A21),
    onSurface = Color(0xFFE6E1E9),
    surfaceVariant = Color(0xFF322F3A),
    onSurfaceVariant = Color(0xFFCBC4CF),
    outline = Color(0xFF938F99),
    error = Color(0xFFFFB4AB),
)

/** Extra hues for category chips, charts and badges — index by hash for stability. */
val KaleidoSpectrum = listOf(
    Color(0xFF6C4CE0), Color(0xFFFF5C7A), Color(0xFF12B981), Color(0xFFF59E0B),
    Color(0xFF3B82F6), Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFF14B8A6),
)

fun spectrumFor(key: String): Color =
    KaleidoSpectrum[(key.hashCode().let { if (it < 0) -it else it }) % KaleidoSpectrum.size]

private val KaleidoTypography: Typography
    @Composable get() {
        val base = MaterialTheme.typography
        return base.copy(
            headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
            titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
            titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }

@Composable
fun KaleidoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = KaleidoTypography,
        content = content,
    )
}
