package com.leonoretech.marianas.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leonoretech.marianas.ui.theme.BgPanel
import com.leonoretech.marianas.ui.theme.BorderColor
import com.leonoretech.marianas.ui.theme.Cyan
import com.leonoretech.marianas.ui.theme.Magenta
import com.leonoretech.marianas.ui.theme.MonoFontFamily
import com.leonoretech.marianas.ui.theme.TextDim
import com.leonoretech.marianas.ui.theme.TextPrimary
import com.leonoretech.marianas.viewmodel.SessionUiItem

@Composable
fun SideMenuContent(
    sessions: List<SessionUiItem>,
    onNavigate: (MarianasRoute) -> Unit,
    onSelectSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onNewChat: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(BgPanel)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "MARIANAS",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = Cyan
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Tutup", tint = TextDim)
            }
        }

        Spacer(Modifier.height(16.dp))

        MenuButton(label = "+ OBROLAN BARU", isPrimary = true) { onNewChat() }

        Spacer(Modifier.height(20.dp))
        SectionLabel("DASHBOARD")
        Spacer(Modifier.height(8.dp))

        MarianasRoute.dashboardItems.forEach { route ->
            MenuButton(label = route.label.uppercase()) { onNavigate(route) }
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(20.dp))
        SectionLabel("RIWAYAT SESI")
        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(sessions, key = { it.id }) { session ->
                SessionRow(
                    session = session,
                    onClick = { onSelectSession(session.id) },
                    onDelete = { onDeleteSession(session.id) }
                )
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 2.sp,
        color = TextDim
    )
}

@Composable
private fun MenuButton(label: String, isPrimary: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPrimary) Cyan.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 14.dp)
    ) {
        Text(
            text = label,
            fontFamily = MonoFontFamily,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = if (isPrimary) Cyan else TextPrimary
        )
    }
}

@Composable
private fun SessionRow(session: SessionUiItem, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (session.isActive) Magenta.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = session.title,
            fontFamily = MonoFontFamily,
            fontSize = 11.sp,
            color = if (session.isActive) Magenta else TextDim,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = TextDim.copy(alpha = 0.6f))
        }
    }
}
