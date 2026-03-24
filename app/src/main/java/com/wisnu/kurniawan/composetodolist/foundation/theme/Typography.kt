package com.wisnu.kurniawan.composetodolist.foundation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.wisnu.kurniawan.composetodolist.R

private val Sans = FontFamily(
    Font(R.font.sans_light, FontWeight.Light),
    Font(R.font.sans_regular, FontWeight.Normal),
    Font(R.font.sans_medium, FontWeight.Medium),
    Font(R.font.sans_bold, FontWeight.SemiBold)
)

private val Lato = FontFamily(
    Font(R.font.lato_regular, FontWeight.Normal),
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Normal,
        fontSize = 21.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 9.sp,
    )
)

fun scaledTypography(scaleFactor: Float): Typography {
    if (scaleFactor == 1f) return Typography

    fun TextStyle.scaled() = copy(
        fontSize = fontSize * scaleFactor,
        lineHeight = if (lineHeight != TextUnit.Unspecified) lineHeight * scaleFactor else lineHeight
    )

    return Typography.copy(
        displayLarge = Typography.displayLarge.scaled(),
        displayMedium = Typography.displayMedium.scaled(),
        displaySmall = Typography.displaySmall.scaled(),
        headlineLarge = Typography.headlineLarge.scaled(),
        headlineMedium = Typography.headlineMedium.scaled(),
        headlineSmall = Typography.headlineSmall.scaled(),
        titleLarge = Typography.titleLarge.scaled(),
        titleMedium = Typography.titleMedium.scaled(),
        titleSmall = Typography.titleSmall.scaled(),
        bodyLarge = Typography.bodyLarge.scaled(),
        bodyMedium = Typography.bodyMedium.scaled(),
        bodySmall = Typography.bodySmall.scaled(),
        labelLarge = Typography.labelLarge.scaled(),
        labelMedium = Typography.labelMedium.scaled(),
        labelSmall = Typography.labelSmall.scaled(),
    )
}
