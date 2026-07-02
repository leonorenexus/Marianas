package com.leonoretech.marianas.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leonoretech.marianas.ui.theme.BgDark
import com.leonoretech.marianas.ui.theme.BgPanel
import com.leonoretech.marianas.ui.theme.DangerRed
import com.leonoretech.marianas.ui.theme.Magenta
import com.leonoretech.marianas.ui.theme.MonoFontFamily
import com.leonoretech.marianas.ui.theme.TextDim
import com.leonoretech.marianas.ui.theme.TextPrimary
import com.leonoretech.marianas.viewmodel.ChatViewModel
import com.leonoretech.marianas.viewmodel.SessionUiItem

@Composable
fun DataDashboard(viewModel: ChatViewModel, onBack: () -> Unit, onSessionSelected: () -> Unit) {
    val state by viewModel.dataState.collectAsState()
    var showWipeConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(BgDark)) {
        DashboardTopBar(title = "DATA & SESI", onBack = onBack)

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            FieldLabel("RIWAYAT SESI (${state.sessions.size})")
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.sessions, key = { it.id }) { session ->
                SessionListRow(
                    session = session,
                    onClick = {
                        viewModel.switchSession(session.id)
                        onSessionSelected()
                    },
                    onDelete = { viewModel.deleteSession(session.id) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            DashboardButton(
                label = "🗑 HAPUS SEMUA DATA",
                accentColor = DangerRed,
                onClick = { showWipeConfirm = true }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Catatan: aplikasi ini menyimpan semua data secara lokal di perangkat (tidak ada cloud sync/backend).",
                fontFamily = MonoFontFamily,
                fontSize = 10.sp,
                color = TextDim,
                lineHeight = 15.sp
            )
        }
    }

    if (showWipeConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeConfirm = false },
            title = { Text("Hapus Semua Data?", fontFamily = MonoFontFamily) },
            text = { Text("Semua sesi, pesan, dan konfigurasi akan dihapus permanen. Tindakan ini tidak bisa dibatalkan.", fontFamily = MonoFontFamily, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.wipeAllData()
                    showWipeConfirm = false
                }) {
                    Text("HAPUS", color = DangerRed, fontFamily = MonoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirm = false }) {
                    Text("BATAL", fontFamily = MonoFontFamily)
                }
            }
        )
    }
}

@Composable
private fun SessionListRow(session: SessionUiItem, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (session.isActive) Magenta.copy(alpha = 0.08f) else BgPanel)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = session.title,
            fontFamily = MonoFontFamily,
            fontSize = 12.sp,
            color = if (session.isActive) Magenta else TextPrimary,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Hapus sesi", tint = TextDim)
        }
    }
}
