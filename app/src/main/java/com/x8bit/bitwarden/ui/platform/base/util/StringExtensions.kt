package com.x8bit.bitwarden.ui.platform.base.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.core.graphics.toColorInt
import java.net.URI
import java.util.Locale

/**
 * Whether or not string is a valid email address.
 *
 * This just checks if the string contains the "@" symbol.
 */
fun String.isValidEmail(): Boolean = contains("@")

/**
 * Returns `true` if the given [String] is a non-blank, valid URI and `false` otherwise.
 *
 * Note that this does not require the URI to contain a URL scheme like `https://`.
 */
fun String.isValidUri(): Boolean =
    try {
        URI.create(this)
        this.isNotBlank()
    } catch (_: IllegalArgumentException) {
        false
    }

/**
 * Returns the given [String] in a lowercase form using the primary [Locale] from the current
 * context.
 */
@Composable
fun String.lowercaseWithCurrentLocal(): String {
    return lowercase(LocalContext.current.resources.configuration.locales[0])
}

/**
 * Returns the [String] as an [AnnotatedString].
 */
fun String.toAnnotatedString(): AnnotatedString = AnnotatedString(text = this)

/**
 * Converts a hex string to a [Color].
 *
 * Supported formats:
 * - "rrggbb" / "#rrggbb"
 * - "aarrggbb" / "#aarrggbb"
 */
fun String.hexToColor(): Color = if (startsWith("#")) {
    Color(toColorInt())
} else {
    Color("#$this".toColorInt())
}

/**
 * Creates a new [String] that represents a unique color in the hex representation (`"#AARRGGBB"`).
 * This can be applied to any [String] in order to provide some deterministic color value based on
 * arbitrary [String] properties.
 */
@OptIn(ExperimentalStdlibApi::class)
@Suppress("MagicNumber")
fun String.toHexColorRepresentation(): String {
    // Produces a string with exactly two hexadecimal digits.
    // Ex:
    // 0 -> "00"
    // 10 -> "0a"
    // 1000 -> "e8"
    fun Int.toTwoDigitHexString(): String =
        this.toHexString().takeLast(2)

    // Calculates separate red, blue, and green values from different positions in the hash and then
    // combines then into a single color.
    val hash = this.hashCode()
    val red = (hash and 0x0000FF).toTwoDigitHexString()
    val green = ((hash and 0x00FF00) shr 8).toTwoDigitHexString()
    val blue = ((hash and 0xFF0000) shr 16).toTwoDigitHexString()
    return "#ff$red$green$blue"
}
