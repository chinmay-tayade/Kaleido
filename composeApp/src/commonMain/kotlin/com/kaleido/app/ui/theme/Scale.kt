package com.kaleido.app.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Cross-platform responsive scaling — the Compose Multiplatform equivalent of the
 * Android-only `com.intuit.sdp` / `com.intuit.ssp` resource libraries (which are
 * XML-`dimen`-only and don't exist for iOS or Web).
 *
 * At the root we measure the window, derive a single scale factor relative to a
 * reference phone width, clamp it so a tablet or a desktop browser doesn't blow
 * everything up, and expose it through [LocalUiScale]. Then `12.sdp` / `14.ssp`
 * read like the sdp/ssp API but resolve identically on phone, tablet and web.
 */
private const val REFERENCE_WIDTH_DP = 380f
private const val MIN_SCALE = 0.85f
private const val MAX_SCALE = 1.30f

/** Widest the primary content column is allowed to get on large screens. */
val CONTENT_MAX_WIDTH: Dp = 720.dp

val LocalUiScale = staticCompositionLocalOf { 1f }
val LocalWindowWidth = staticCompositionLocalOf { REFERENCE_WIDTH_DP.dp }

enum class SizeClass { Compact, Medium, Expanded }

val LocalSizeClass = staticCompositionLocalOf { SizeClass.Compact }

/** Wrap the app content once; everything below can use [sdp] / [ssp] and [LocalSizeClass]. */
@Composable
fun ProvideResponsiveScale(content: @Composable () -> Unit) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    val widthPx = windowInfo.containerSize.width
    val widthDp = if (widthPx > 0) widthPx / density.density else REFERENCE_WIDTH_DP
    val scale = (widthDp / REFERENCE_WIDTH_DP).coerceIn(MIN_SCALE, MAX_SCALE)
    val sizeClass = when {
        widthDp < 600f -> SizeClass.Compact
        widthDp < 900f -> SizeClass.Medium
        else -> SizeClass.Expanded
    }

    CompositionLocalProvider(
        LocalUiScale provides scale,
        LocalWindowWidth provides widthDp.dp,
        LocalSizeClass provides sizeClass,
        content = content,
    )
}

/** Centres the content and caps its width on tablet / desktop, like every real store's web layout. */
@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = CONTENT_MAX_WIDTH,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = maxWidth).fillMaxSize(), content = content)
    }
}

/** Scalable dp. */
val Int.sdp: Dp
    @Composable get() = (this * LocalUiScale.current).dp

/** Scalable dp. */
val Double.sdp: Dp
    @Composable get() = (this * LocalUiScale.current).dp

/** Scalable sp (font sizes). */
val Int.ssp: TextUnit
    @Composable get() = (this * LocalUiScale.current).sp

val Double.ssp: TextUnit
    @Composable get() = (this * LocalUiScale.current).sp
