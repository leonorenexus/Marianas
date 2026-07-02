package com.leonoretech.marianas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leonoretech.marianas.ui.theme.BgDark
import com.leonoretech.marianas.ui.theme.BgMid
import com.leonoretech.marianas.ui.theme.BgPanel
import com.leonoretech.marianas.ui.theme.Blue
import com.leonoretech.marianas.ui.theme.Cyan
import com.leonoretech.marianas.ui.theme.DangerRed
import com.leonoretech.marianas.ui.theme.MonoFontFamily
import com.leonoretech.marianas.ui.theme.Purple
import com.leonoretech.marianas.ui.theme.PurpleLight
import com.leonoretech.marianas.ui.theme.TextDim
import com.leonoretech.marianas.ui.theme.TextPrimary
import com.leonoretech.marianas.viewmodel.ChatViewModel

@Composable
fun LoginScreen(viewModel: ChatViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error by viewModel.loginError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1A0835), Color(0xFF0A0520), BgDark),
                    radius = 1000f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Top bar label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            Brush.linearGradient(listOf(Purple, Blue)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", fontFamily = MonoFontFamily, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "MARIANAS",
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).background(Color(0xFF2DFFA0), CircleShape))
                    Spacer(Modifier.width(5.dp))
                    Text("ONLINE", fontFamily = MonoFontFamily, fontSize = 9.sp, color = Color(0xFF2DFFA0))
                }
            }

            Spacer(Modifier.height(36.dp))

            // Key icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.linearGradient(listOf(Purple.copy(alpha = 0.6f), Blue.copy(alpha = 0.6f))),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🔑", fontSize = 38.sp)
            }

            Spacer(Modifier.height(24.dp))

            // Title
            Text(
                text = "AKSES TERBATAS",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 26.sp,
                letterSpacing = 2.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Masukkan kredensial untuk melanjutkan",
                fontFamily = MonoFontFamily,
                fontSize = 12.sp,
                color = TextDim,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // Step cards (like Vitus)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgPanel, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column {
                    StepRow(1, "Masukkan username dan password", Purple)
                    Spacer(Modifier.height(12.dp))
                    StepRow(2, "Tekan tombol MASUK di bawah", Blue)
                    Spacer(Modifier.height(12.dp))
                    StepRow(3, "Selamat menggunakan MARIANAS AI", Cyan)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Input fields
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("USERNAME", fontFamily = MonoFontFamily, fontSize = 10.sp, color = TextDim) },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = MonoFontFamily, fontSize = 14.sp, color = TextPrimary
                ),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    unfocusedBorderColor = Purple.copy(alpha = 0.3f),
                    focusedContainerColor = BgMid,
                    unfocusedContainerColor = BgPanel
                )
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("PASSWORD", fontFamily = MonoFontFamily, fontSize = 10.sp, color = TextDim) },
                visualTransformation = PasswordVisualTransformation(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = MonoFontFamily, fontSize = 14.sp, color = TextPrimary
                ),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    unfocusedBorderColor = Purple.copy(alpha = 0.3f),
                    focusedContainerColor = BgMid,
                    unfocusedContainerColor = BgPanel
                )
            )

            if (error != null) {
                Spacer(Modifier.height(10.dp))
                Text(error ?: "", fontFamily = MonoFontFamily, fontSize = 12.sp, color = DangerRed, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { viewModel.attemptLogin(username, password) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(Purple, Blue)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("MASUK", fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 2.sp, color = Color.White)
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔒", fontSize = 12.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    "Akses hanya untuk pengguna terdaftar",
                    fontFamily = MonoFontFamily,
                    fontSize = 10.sp,
                    color = TextDim
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "MARIANAS · Powered by Leonore Tech Team",
                fontFamily = MonoFontFamily,
                fontSize = 9.sp,
                color = TextDim.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun StepRow(number: Int, text: String, color: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number.toString(),
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = color
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(text, fontFamily = MonoFontFamily, fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}
