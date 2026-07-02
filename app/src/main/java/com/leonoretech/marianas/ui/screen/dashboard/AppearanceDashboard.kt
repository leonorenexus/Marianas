package com.leonoretech.marianas.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leonoretech.marianas.ui.theme.BgDark
import com.leonoretech.marianas.ui.theme.Cyan
import com.leonoretech.marianas.ui.theme.MonoFontFamily
import com.leonoretech.marianas.ui.theme.TextDim

/**
 * Visual/appearance settings. Currently a lightweight placeholder — the
 * cyberpunk theme, particle background, and logo watermark are fixed parts
 * of the app's identity (per design decision), so this dashboard mainly
 * exposes things that are reasonable to toggle without breaking that identity.
 */
@Composable
fun AppearanceDashboard(onBack: () -> Unit) {
    var particleEnabled by remember { mutableStateOf(true) }
    var watermarkEnabled by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().background(BgDark)) {
        DashboardTopBar(title = "TAMPILAN", onBack = onBack)

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            ToggleRow(
                label = "PARTICLE BACKGROUND",
                description = "Animasi partikel & grid di latar chat",
                checked = particleEnabled,
                onCheckedChange = { particleEnabled = it }
            )
            Spacer(Modifier.height(16.dp))
            ToggleRow(
                label = "LOGO WATERMARK",
                description = "Logo Leonore Tech Team redup di latar chat",
                checked = watermarkEnabled,
                onCheckedChange = { watermarkEnabled = it }
            )

            Spacer(Modifier.height(24.dp))
            Text(
                "Catatan: tema warna cyan/magenta dan font monospace adalah identitas tetap aplikasi ini, belum bisa diganti dari sini.",
                fontFamily = MonoFontFamily,
                fontSize = 10.sp,
                color = TextDim,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            FieldLabel(label)
            Text(description, fontFamily = MonoFontFamily, fontSize = 10.sp, color = TextDim)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Cyan.copy(alpha = 0.4f), checkedThumbColor = Cyan)
        )
    }
}
