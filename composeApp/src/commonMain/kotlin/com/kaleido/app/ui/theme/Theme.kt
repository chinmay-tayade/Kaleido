package com.kaleido.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Kaleido brand + commerce palette
// Indigo brand, a Flipkart-style green for ratings/savings, an Amazon-style
// amber "Add to cart" and a hot orange "Buy now".
// ---------------------------------------------------------------------------

private val Indigo = Color(0xFF3D3AF0)
private val IndigoDark = Color(0xFFB9B6FF)
private val Coral = Color(0xFFFF5470)

/** Semantic accent colours that aren't part of the M3 scheme. */
data class CommerceColors(
    val ctaBuy: Color,        // "Buy now"
    val ctaCart: Color,       // "Add to cart"
    val add: Color,           // the "ADD" button / +q- stepper on cards
    val addSurface: Color,    // tinted background behind an ADD button
    val rating: Color,        // rating chips
    val savings: Color,       // "62% off", "You save …"
    val priceStrike: Color,   // struck MRP text
    val badge: Color,         // "Bestseller" etc.
    val surfaceSunken: Color, // page background behind cards
    val cardBorder: Color,
    val star: Color,
)

private val LightCommerce = CommerceColors(
    ctaBuy = Color(0xFFFB641D),
    ctaCart = Color(0xFFFF9F00),
    add = Color(0xFF0C8A43),
    addSurface = Color(0xFFEAF7EF),
    rating = Color(0xFF1BA672),
    savings = Color(0xFF1BA672),
    priceStrike = Color(0xFF8B8D98),
    badge = Color(0xFF8A4B00),
    surfaceSunken = Color(0xFFF1F3F6),
    cardBorder = Color(0xFFE7E7EE),
    star = Color(0xFFFFB800),
)

private val DarkCommerce = CommerceColors(
    ctaBuy = Color(0xFFFF7A3D),
    ctaCart = Color(0xFFFFB53D),
    add = Color(0xFF3FD39E),
    addSurface = Color(0xFF12271D),
    rating = Color(0xFF3FD39E),
    savings = Color(0xFF3FD39E),
    priceStrike = Color(0xFF8B8D98),
    badge = Color(0xFFFFC98A),
    surfaceSunken = Color(0xFF0F0F14),
    cardBorder = Color(0xFF2A2A33),
    star = Color(0xFFFFC542),
)

val LocalCommerceColors = staticCompositionLocalOf { LightCommerce }

val MaterialTheme.commerce: CommerceColors
    @Composable get() = LocalCommerceColors.current

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7E6FF),
    onPrimaryContainer = Color(0xFF1A1064),
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE1E6),
    onSecondaryContainer = Color(0xFF4C0016),
    tertiary = Color(0xFF1BA672),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC7F3E2),
    onTertiaryContainer = Color(0xFF00341F),
    background = Color(0xFFF1F3F6),
    onBackground = Color(0xFF16171D),
    surface = Color.White,
    onSurface = Color(0xFF16171D),
    surfaceVariant = Color(0xFFEDEEF3),
    onSurfaceVariant = Color(0xFF5B5D67),
    outline = Color(0xFFD5D6DE),
    outlineVariant = Color(0xFFE7E7EE),
    error = Color(0xFFD03232),
)

private val DarkColors = darkColorScheme(
    primary = IndigoDark,
    onPrimary = Color(0xFF17106A),
    primaryContainer = Color(0xFF2C24B8),
    onPrimaryContainer = Color(0xFFE7E6FF),
    secondary = Color(0xFFFFB1BE),
    onSecondary = Color(0xFF5E0020),
    secondaryContainer = Color(0xFF8E0B2C),
    onSecondaryContainer = Color(0xFFFFE1E6),
    tertiary = Color(0xFF3FD39E),
    onTertiary = Color(0xFF00341F),
    background = Color(0xFF0F0F14),
    onBackground = Color(0xFFE7E7EC),
    surface = Color(0xFF17171E),
    onSurface = Color(0xFFE7E7EC),
    surfaceVariant = Color(0xFF25252E),
    onSurfaceVariant = Color(0xFFC2C3CC),
    outline = Color(0xFF43434D),
    outlineVariant = Color(0xFF2A2A33),
    error = Color(0xFFFF6B6B),
)

/** Extra hues for category avatars, chips and charts — index by hash for stability. */
val KaleidoSpectrum = listOf(
    Color(0xFF3D3AF0), Color(0xFFFF5470), Color(0xFF1BA672), Color(0xFFF59E0B),
    Color(0xFF2FA8FF), Color(0xFFB84BFF), Color(0xFF00B8A9), Color(0xFFFF7A3D),
)

fun spectrumFor(key: String): Color =
    KaleidoSpectrum[(key.hashCode().let { if (it < 0) -it else it }) % KaleidoSpectrum.size]

private val KaleidoTypography: Typography
    @Composable get() {
        val base = MaterialTheme.typography
        return base.copy(
            headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.6).sp),
            headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp),
            titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
            titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Bold),
            titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            labelLarge = base.labelLarge.copy(fontWeight = FontWeight.Bold),
            labelMedium = base.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }

@Composable
fun KaleidoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalCommerceColors provides if (darkTheme) DarkCommerce else LightCommerce,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = KaleidoTypography,
            content = content,
        )
    }
}
