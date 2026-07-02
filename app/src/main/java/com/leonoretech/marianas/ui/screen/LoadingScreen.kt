package com.leonoretech.marianas.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leonoretech.marianas.R
import com.leonoretech.marianas.ui.theme.BgDark
import com.leonoretech.marianas.ui.theme.Blue
import com.leonoretech.marianas.ui.theme.Cyan
import com.leonoretech.marianas.ui.theme.MonoFontFamily
import com.leonoretech.marianas.ui.theme.Purple
import com.leonoretech.marianas.ui.theme.PurpleLight
import com.leonoretech.marianas.ui.theme.TextDim
import com.leonoretech.marianas.ui.theme.TextPrimary

@Composable
fun LoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1A0A3A), Color(0xFF0A0520), BgDark),
                    radius = 900f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glow ring behind logo
        Box(
            modifier = Modifier
                .size(280.dp)
                .alpha(glowAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Purple.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.bg_logo_watermark),
                contentDescription = "MARIANAS logo",
                modifier = Modifier.size(180.dp).scale(pulse)
            )
            Spacer(Modifier.height(28.dp))

            // App name
            Text(
                text = "MARIANAS",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 38.sp,
                letterSpacing = 4.sp,
                color = TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "— AI —",
                fontFamily = MonoFontFamily,
                fontSize = 13.sp,
                letterSpacing = 6.sp,
                color = Purple
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Intelligent. Powerful. Limitless.",
                fontFamily = MonoFontFamily,
                fontSize = 12.sp,
                color = TextDim,
                textAlign = TextAlign.Center
            )
            Text(
                text = "AI untuk ide tanpa batas.",
                fontFamily = MonoFontFamily,
                fontSize = 11.sp,
                color = TextDim,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(48.dp))

            // Circular loading indicator like Vitus
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Purple,
                trackColor = Purple.copy(alpha = 0.15f),
                strokeWidth = 3.dp
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Memuat kecerdasan...",
                fontFamily = MonoFontFamily,
                fontSize = 11.sp,
                color = TextDim
            )

            Spacer(Modifier.height(40.dp))
            Text(
                text = "LEONORE TECH TEAM",
                fontFamily = MonoFontFamily,
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                color = TextDim.copy(alpha = 0.5f)
            )
        }
    }
}
