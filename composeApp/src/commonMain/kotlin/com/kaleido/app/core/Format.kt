package com.kaleido.app.core

import kotlin.math.roundToLong

/**
 * Locale-free money + number formatting. `String.format` isn't available on
 * Kotlin/Wasm, so these do it by hand and stay identical on every platform.
 */
fun Double.asPrice(currency: String = "$"): String {
    val cents = (this * 100).roundToLong()
    val whole = cents / 100
    val frac = (cents % 100).toInt().let { if (it < 0) -it else it }
    val grouped = whole.toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
        .removePrefix(",")
    val fracStr = if (frac < 10) "0$frac" else "$frac"
    return "$currency$grouped.$fracStr"
}

fun Double.oneDecimal(): String {
    val x = (this * 10).roundToLong()
    return "${x / 10}.${(x % 10).let { if (it < 0) -it else it }}"
}

fun Double.asPercent(): String = "${(this * 100).roundToLong()}%"

/** 1240 -> "1.2k", 15300 -> "15k", 2_100_000 -> "2.1M" — for review counts. */
fun Int.compact(): String = when {
    this < 1_000 -> toString()
    this < 10_000 -> "${this / 1000}.${(this % 1000) / 100}k"
    this < 1_000_000 -> "${this / 1000}k"
    else -> "${this / 1_000_000}.${(this % 1_000_000) / 100_000}M"
}
