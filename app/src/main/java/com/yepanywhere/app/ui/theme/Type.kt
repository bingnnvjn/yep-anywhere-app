package com.yepanywhere.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object YepType {
    val largeTitle = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.37.sp)
    val title1 = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.36.sp)
    val headline = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.4).sp)
    val body = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.4).sp)
    val callout = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.3).sp)
    val subheadline = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.2).sp)
    val footnote = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.1).sp)
    val caption1 = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp)
    val caption2 = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.1.sp)
}
