package com.leonoretech.marianas.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leonoretech.marianas.data.repository.FormatStyle
import com.leonoretech.marianas.data.repository.Provider
import com.leonoretech.marianas.ui.theme.BgDark
import com.leonoretech.marianas.ui.theme.BgPanel
import com.leonoretech.marianas.ui.theme.BorderColor
import com.leonoretech.marianas.ui.theme.Cyan
import com.leonoretech.marianas.ui.theme.DangerRed
import com.leonoretech.marianas.ui.theme.Magenta
import com.leonoretech.marianas.ui.theme.MonoFontFamily
import com.leonoretech.marianas.ui.theme.SuccessGreen
import com.leonoretech.marianas.ui.theme.TextDim
import com.leonoretech.marianas.ui.theme.TextPrimary
import com.leonoretech.marianas.viewmodel.ChatViewModel
import com.leonoretech.marianas.viewmodel.ConnectionTestResult
import com.leonoretech.marianas.viewmodel.CustomSlotUiState

@Composable
fun ProviderDashboard(viewModel: ChatViewModel, onBack: () -> Unit) {
    val state by viewModel.configState.collectAsState()

    var openrouterKey by remember(state.openrouterKey) { mutableStateOf(state.openrouterKey) }
    var openrouterModel by remember(state.openrouterModel) { mutableStateOf(state.openrouterModel) }
    var groqKey by remember(state.groqKey) { mutableStateOf(state.groqKey) }
    var groqModel by remember(state.groqModel) { mutableStateOf(state.groqModel) }
    var googleKey by remember(state.googleKey) { mutableStateOf(state.googleKey) }
    var googleModel by remember(state.googleModel) { mutableStateOf(state.googleModel) }
    var custom1 by remember(state.custom1) { mutableStateOf(state.custom1) }
    var custom2 by remember(state.custom2) { mutableStateOf(state.custom2) }
    var custom3 by remember(state.custom3) { mutableStateOf(state.custom3) }
    var systemPrompt by remember(state.systemPrompt) { mutableStateOf(state.systemPrompt) }
    var streamEnabled by remember(state.streamEnabled) { mutableStateOf(state.streamEnabled) }
    var timeoutText by remember(state.timeoutSeconds) { mutableStateOf(state.timeoutSeconds.toString()) }

    var selectedTab by remember { mutableStateOf(state.activeProvider) }

    Column(modifier = Modifier.fillMaxSize().background(BgDark)) {
        DashboardTopBar(title = "PROVIDER", onBack = onBack)

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                ProviderTabRow(
                    selected = selectedTab,
                    onSelect = {
                        selectedTab = it
                        viewModel.setActiveProvider(it)
                    }
                )
            }

            item {
                when (selectedTab) {
                    Provider.OPENROUTER -> ProviderFields(
                        keyLabel = "API Key", keyValue = openrouterKey, onKeyChange = { openrouterKey = it },
                        keyHint = "Ambil gratis di openrouter.ai/keys",
                        modelValue = openrouterModel, onModelChange = { openrouterModel = it },
                        modelHint = "Contoh: meta-llama/llama-3.1-8b-instruct:free"
                    )
                    Provider.GROQ -> ProviderFields(
                        keyLabel = "API Key", keyValue = groqKey, onKeyChange = { groqKey = it },
                        keyHint = "Ambil gratis di console.groq.com/keys",
                        modelValue = groqModel, onModelChange = { groqModel = it },
                        modelHint = "Isi manual model id yang tersedia di akun Groq lo"
                    )
                    Provider.GOOGLE -> ProviderFields(
                        keyLabel = "API Key", keyValue = googleKey, onKeyChange = { googleKey = it },
                        keyHint = "Ambil di aistudio.google.com/apikey",
                        modelValue = googleModel, onModelChange = { googleModel = it },
                        modelHint = "Default: gemini-3.5-flash"
                    )
                    Provider.CUSTOM1 -> CustomSlotFields(custom1) { custom1 = it }
                    Provider.CUSTOM2 -> CustomSlotFields(custom2) { custom2 = it }
                    Provider.CUSTOM3 -> CustomSlotFields(custom3) { custom3 = it }
                }
            }

            item {
                FieldLabel("SYSTEM PROMPT (OPSIONAL)")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Contoh: Kamu asisten teknis yang langsung ke intinya...", fontFamily = MonoFontFamily, fontSize = 12.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = MonoFontFamily, fontSize = 12.sp, color = TextPrimary),
                    minLines = 3
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel("STREAMING RESPONSE")
                        Text("Tampilkan jawaban sambil ditulis", fontFamily = MonoFontFamily, fontSize = 10.sp, color = TextDim)
                    }
                    Switch(
                        checked = streamEnabled,
                        onCheckedChange = { streamEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Cyan.copy(alpha = 0.4f), checkedThumbColor = Cyan)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel("FALLBACK OTOMATIS")
                        Text("Coba provider lain kalau yang aktif gagal", fontFamily = MonoFontFamily, fontSize = 10.sp, color = TextDim)
                    }
                    Switch(
                        checked = state.fallbackEnabled,
                        onCheckedChange = { viewModel.setFallbackEnabled(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = Magenta.copy(alpha = 0.4f), checkedThumbColor = Magenta)
                    )
                }
            }

            item {
                FieldLabel("TIMEOUT RESPON (DETIK)")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = timeoutText,
                    onValueChange = { timeoutText = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = MonoFontFamily, fontSize = 13.sp, color = TextPrimary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Text("Model berat bisa lama. Default 300s (5 menit).", fontFamily = MonoFontFamily, fontSize = 10.sp, color = TextDim)
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DashboardButton(
                        label = "🔌 TES KONEKSI",
                        modifier = Modifier.weight(1f),
                        accentColor = Cyan,
                        onClick = { viewModel.testConnection() }
                    )
                    DashboardButton(
                        label = "💾 SIMPAN",
                        modifier = Modifier.weight(1f),
                        accentColor = TextPrimary,
                        onClick = {
                            viewModel.saveProviderConfig(
                                openrouterKey, openrouterModel,
                                groqKey, groqModel,
                                googleKey, googleModel,
                                custom1, custom2, custom3,
                                systemPrompt, streamEnabled,
                                timeoutText.toIntOrNull() ?: 300
                            )
                        }
                    )
                }
            }

            item {
                ConnectionStatusLine(state.connectionTestResult)
            }
        }
    }
}

@Composable
private fun ProviderTabRow(selected: Provider, onSelect: (Provider) -> Unit) {
    val tabs = listOf(
        Provider.OPENROUTER to "OpenRouter",
        Provider.GROQ to "Groq",
        Provider.GOOGLE to "Google AI",
        Provider.CUSTOM1 to "Custom 1",
        Provider.CUSTOM2 to "Custom 2",
        Provider.CUSTOM3 to "Custom 3"
    )
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tabs.forEach { (provider, label) ->
            val isSelected = provider == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) Cyan.copy(alpha = 0.1f) else Color.Transparent)
                    .clickable { onSelect(provider) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label.uppercase(),
                    fontFamily = MonoFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    color = if (isSelected) Cyan else TextDim
                )
            }
        }
    }
}

@Composable
private fun ProviderFields(
    keyLabel: String, keyValue: String, onKeyChange: (String) -> Unit, keyHint: String,
    modelValue: String, onModelChange: (String) -> Unit, modelHint: String
) {
    Column {
        FieldLabel(keyLabel.uppercase())
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = keyValue,
            onValueChange = onKeyChange,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = MonoFontFamily, fontSize = 13.sp, color = TextPrimary),
            singleLine = true
        )
        Text(keyHint, fontFamily = MonoFontFamily, fontSize = 10.sp, color = TextDim)

        Spacer(Modifier.height(14.dp))
        FieldLabel("MODEL ID")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = modelValue,
            onValueChange = onModelChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = MonoFontFamily, fontSize = 13.sp, color = TextPrimary),
            singleLine = true
        )
        Text(modelHint, fontFamily = MonoFontFamily, fontSize = 10.sp, color = TextDim)
    }
}

@Composable
private fun CustomSlotFields(slot: CustomSlotUiState, onChange: (CustomSlotUiState) -> Unit) {
    Column {
        FieldLabel("NAMA SLOT")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = slot.name,
            onValueChange = { onChange(slot.copy(name = it)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = MonoFontFamily, fontSize = 13.sp, color = TextPrimary),
            singleLine = true
        )

        Spacer(Modifier.height(14.dp))
        FieldLabel("FORMAT API")
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormatChip("OpenAI-style", slot.formatStyle == FormatStyle.OPENAI) {
                onChange(slot.copy(formatStyle = FormatStyle.OPENAI))
            }
            FormatChip("Gemini-style", slot.formatStyle == FormatStyle.GEMINI) {
                onChange(slot.copy(formatStyle = FormatStyle.GEMINI))
            }
        }
        Text(
            if (slot.formatStyle == FormatStyle.OPENAI)
                "Format messages[] ala OpenAI Chat Completions (kebanyakan provider pakai ini)."
            else
                "Format contents[] ala Gemini native. URL harus base endpoint sampai .../models (tanpa nama model).",
            fontFamily = MonoFontFamily, fontSize = 10.sp, color = TextDim
        )

        Spacer(Modifier.height(14.dp))
        FieldLabel("ENDPOINT URL")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = slot.url,
            onValueChange = { onChange(slot.copy(url = it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    if (slot.formatStyle == FormatStyle.OPENAI) "https://.../v1/chat/completions" else "https://.../v1beta/models",
                    fontFamily = MonoFontFamily, fontSize = 11.sp, color = TextDim
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = MonoFontFamily, fontSize = 12.sp, color = TextPrimary),
            singleLine = true
        )

        Spacer(Modifier.height(14.dp))
        FieldLabel("API KEY (OPSIONAL)")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = slot.key,
            onValueChange = { onChange(slot.copy(key = it)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = MonoFontFamily, fontSize = 13.sp, color = TextPrimary),
            singleLine = true
        )

        Spacer(Modifier.height(14.dp))
        FieldLabel("MODEL ID")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = slot.model,
            onValueChange = { onChange(slot.copy(model = it)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = MonoFontFamily, fontSize = 13.sp, color = TextPrimary),
            singleLine = true
        )
    }
}

@Composable
private fun FormatChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Magenta.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            fontFamily = MonoFontFamily,
            fontSize = 11.sp,
            color = if (isSelected) Magenta else TextDim
        )
    }
}

@Composable
private fun ConnectionStatusLine(result: ConnectionTestResult) {
    val (dotColor, text) = when (result) {
        is ConnectionTestResult.Idle -> TextDim to "belum dites"
        is ConnectionTestResult.Testing -> TextDim to "menghubungi server..."
        is ConnectionTestResult.Success -> SuccessGreen to result.message
        is ConnectionTestResult.Failure -> DangerRed to result.message
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).clip(RoundedCornerShape(50)).background(dotColor))
        Spacer(Modifier.width(8.dp))
        Text(text, fontFamily = MonoFontFamily, fontSize = 11.sp, color = TextDim)
    }
}

@Composable
fun FieldLabel(text: String) {
    Text(text, fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp, color = TextDim)
}

@Composable
fun DashboardTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = Cyan)
        }
        Spacer(Modifier.width(4.dp))
        Text(title, fontFamily = MonoFontFamily, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp, color = TextPrimary)
    }
}

@Composable
fun DashboardButton(label: String, modifier: Modifier = Modifier, accentColor: Color, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BgPanel)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontFamily = MonoFontFamily, fontSize = 11.sp, letterSpacing = 1.sp, color = accentColor)
    }
}
