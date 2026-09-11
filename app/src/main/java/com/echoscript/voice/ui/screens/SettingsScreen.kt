package com.echoscript.voice.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echoscript.voice.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val promptEnhancerEnabled by viewModel.promptEnhancerEnabled.collectAsState()
    val currentApiKey by viewModel.apiKey.collectAsState()
    var inputKey by remember(currentApiKey) { mutableStateOf(currentApiKey) }
    var keySavedMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "تنظیمات اپلیکیشن اندروید",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Gemini API Key Setting Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("کلید هوش مصنوعی (Gemini API Key)", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "برای تبدیل صوت ضبط‌شده به متن با هوش مصنوعی بدون قطعی و با بالاترین دقت، کلید خود را وارد کنید:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    placeholder = { Text("AIzaSy...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (keySavedMessage.isNotBlank()) {
                        Text(
                            text = keySavedMessage,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Button(
                        onClick = {
                            viewModel.setApiKey(inputKey.trim())
                            keySavedMessage = "کلید با موفقیت ذخیره شد ✅"
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ذخیره کلید")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language Setting
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("زبان تشخیص گفتار", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedLang == "fa-IR",
                        onClick = { viewModel.setLanguage("fa-IR") },
                        label = { Text("فارسی (ایران)") }
                    )
                    FilterChip(
                        selected = selectedLang == "en-US",
                        onClick = { viewModel.setLanguage("en-US") },
                        label = { Text("English (US)") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prompt Enhancer Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ارتقای پرامپت هوشمند", fontWeight = FontWeight.Bold)
                    Text(
                        "تبدیل گفتار به پرامپت‌های ساختاریافته به صورت پیش‌فرض",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = promptEnhancerEnabled,
                    onCheckedChange = { viewModel.togglePromptEnhancer(it) }
                )
            }
        }
    }
}
