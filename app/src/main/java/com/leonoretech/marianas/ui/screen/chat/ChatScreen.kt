package com.leonoretech.marianas.ui.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.leonoretech.marianas.R
import com.leonoretech.marianas.ui.components.ParticleBackground
import com.leonoretech.marianas.ui.theme.BgDark
import com.leonoretech.marianas.ui.theme.BorderColor
import com.leonoretech.marianas.ui.theme.Cyan
import com.leonoretech.marianas.ui.theme.DangerRed
import com.leonoretech.marianas.ui.theme.Magenta
import com.leonoretech.marianas.ui.theme.MonoFontFamily
import com.leonoretech.marianas.ui.theme.TextDim
import com.leonoretech.marianas.ui.theme.TextPrimary
import com.leonoretech.marianas.viewmodel.ChatUiMessage
import com.leonoretech.marianas.viewmodel.ChatViewModel
import com.leonoretech.marianas.viewmodel.MessageRole

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onOpenMenu: () -> Unit,
    onPickImage: () -> Unit
) {
    val state by viewModel.chatState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        // Layer 1: animated particle/grid background
        ParticleBackground(modifier = Modifier.fillMaxSize().alpha(0.4f))

        // Layer 2: dimmed logo watermark, centered, behind the chat content
        Image(
            painter = painterResource(id = R.drawable.bg_logo_watermark),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.05f),
            contentScale = ContentScale.Fit
        )

        // Layer 3: actual chat UI
        Column(modifier = Modifier.fillMaxSize()) {
            ChatTopBar(
                activeProviderLabel = state.activeProviderLabel,
                onOpenMenu = onOpenMenu
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (state.messages.isEmpty()) {
                    item { EmptyState() }
                }
                items(state.messages, key = { it.id.toString() + it.content.hashCode() }) { msg ->
                    PlainMessageRow(msg)
                }
            }

            if (state.pendingImagePaths.isNotEmpty()) {
                PendingImagesRow(
                    paths = state.pendingImagePaths,
                    onRemove = { viewModel.removePendingImage(it) }
                )
            }

            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank() || state.pendingImagePaths.isNotEmpty()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                onPickImage = onPickImage,
                statusText = state.statusText,
                providerLabel = state.activeProviderLabel
            )
        }
    }
}

@Composable
private fun ChatTopBar(activeProviderLabel: String, onOpenMenu: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOpenMenu) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Cyan)
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "MARIANAS",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
                color = TextPrimary
            )
            Text(
                text = "·AI",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = Cyan
            )
        }
        Text(
            text = activeProviderLabel,
            fontFamily = MonoFontFamily,
            fontSize = 10.sp,
            color = TextDim,
            maxLines = 1
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "[ M ]",
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            color = Cyan.copy(alpha = 0.18f)
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "BELUM ADA OBROLAN",
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.sp,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Buka menu untuk atur API key & model dulu,\nbaru mulai chat.",
            fontFamily = MonoFontFamily,
            fontSize = 11.sp,
            color = TextDim,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/**
 * Plain-text message row — NO rounded bubble container, per user request.
 * User/assistant are distinguished only by a small role label and color,
 * left-aligned for both (not the chat-bubble left/right split pattern).
 */
@Composable
private fun PlainMessageRow(msg: ChatUiMessage) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (msg.role == MessageRole.USER) Magenta else Cyan)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (msg.role == MessageRole.USER) "YOU" else if (msg.isError) "ERROR" else "MARIANAS",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = when {
                    msg.isError -> DangerRed
                    msg.role == MessageRole.USER -> Magenta.copy(alpha = 0.85f)
                    else -> Cyan.copy(alpha = 0.85f)
                }
            )
            if (msg.isStreaming) {
                Spacer(Modifier.width(6.dp))
                Text("●", color = Cyan, fontSize = 8.sp)
            }
        }
        Spacer(Modifier.height(4.dp))

        if (msg.imagePaths.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                msg.imagePaths.forEach { path ->
                    AsyncImage(
                        model = path,
                        contentDescription = "attached image",
                        modifier = Modifier
                            .size(140.dp)
                            .background(BgPanel),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        Text(
            text = msg.content,
            fontFamily = MonoFontFamily,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = if (msg.isError) Color(0xFFFF9AAC) else TextPrimary
        )
    }
}

@Composable
private fun PendingImagesRow(paths: List<String>, onRemove: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        paths.forEach { path ->
            Box(modifier = Modifier.size(56.dp)) {
                AsyncImage(
                    model = path,
                    contentDescription = "pending image",
                    modifier = Modifier.fillMaxSize().background(BgPanel),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { onRemove(path) },
                    modifier = Modifier.size(18.dp).align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Hapus", tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onPickImage: () -> Unit,
    statusText: String,
    providerLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onPickImage) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Upload foto", tint = TextDim)
            }
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Tulis pesan...", fontFamily = MonoFontFamily, fontSize = 13.sp, color = TextDim) },
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = MonoFontFamily, fontSize = 13.sp, color = TextPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                shape = RoundedCornerShape(14.dp),
                maxLines = 5
            )
            IconButton(onClick = onSend) {
                Icon(Icons.Filled.Send, contentDescription = "Kirim", tint = Cyan)
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("● $statusText", fontFamily = MonoFontFamily, fontSize = 9.sp, color = TextDim)
            Text(providerLabel, fontFamily = MonoFontFamily, fontSize = 9.sp, color = TextDim)
        }
    }
}
