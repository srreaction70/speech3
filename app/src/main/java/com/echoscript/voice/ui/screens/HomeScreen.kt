package com.echoscript.voice.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echoscript.voice.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onRequestPermission: () -> Unit,
    onToggleFloatingBubble: () -> Unit
) {
    val context = LocalContext.current
    val isListening by viewModel.isListening.collectAsState()
    val partialText by viewModel.partialText.collectAsState()
    val finalText by viewModel.finalText.collectAsState()
    val isProcessingAI by viewModel.isProcessingAI.collectAsState()
    val promptEnhancerEnabled by viewModel.promptEnhancerEnabled.collectAsState()

    val displayText = remember(finalText, partialText) {
        if (partialText.isNotBlank()) "$finalText $partialText".trim() else finalText
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Floating Bubble Quick Action Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "دکمه شناور روی همه برنامه‌ها",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "تایپ صوتی و پرامپت‌ساز روی تلگرام، واتساپ، کروم و چت‌بات‌ها",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                FilledTonalButton(
                    onClick = onToggleFloatingBubble,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("فعال‌سازی")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main Text Display Box
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (displayText.isEmpty()) {
                    Text(
                        text = if (isListening) "در حال شنیدن صدای شما... صحبت کنید" else "روی دکمه میکروفون زیر بزنید تا صحبت کنید...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 15.sp,
                        lineHeight = 24.sp
                    )
                } else {
                    Text(
                        text = displayText,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isProcessingAI) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick AI Actions Row (Translate, Professionalize, Polish, Copy, Share)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedButton(
                onClick = { viewModel.translateCurrentText() },
                enabled = displayText.isNotBlank() && !isProcessingAI,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Translate, contentDescription = "ترجمه", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ترجمه", fontSize = 12.sp)
            }

            ElevatedButton(
                onClick = { viewModel.professionalizeCurrentText() },
                enabled = displayText.isNotBlank() && !isProcessingAI,
                modifier = Modifier.weight(1.3f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "پرامپت", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("پرامپت هوشمند", fontSize = 12.sp)
            }

            IconButton(
                onClick = {
                    if (displayText.isNotBlank()) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("EchoScript", displayText))
                        Toast.makeText(context, "کپی شد 📋", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = displayText.isNotBlank()
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "کپی")
            }

            IconButton(
                onClick = {
                    if (displayText.isNotBlank()) {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, displayText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "ارسال متن به"))
                    }
                },
                enabled = displayText.isNotBlank()
            ) {
                Icon(Icons.Default.Share, contentDescription = "اشتراک‌گذاری")
            }

            IconButton(
                onClick = { viewModel.clearText() },
                enabled = displayText.isNotBlank()
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "پاک کردن")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Big Touch-Friendly Mic Button with Pulsing Animation
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(140.dp)
        ) {
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                )
            }

            FloatingActionButton(
                onClick = {
                    if (isListening) {
                        viewModel.stopSpeechRecognition()
                    } else {
                        onRequestPermission()
                    }
                },
                shape = CircleShape,
                modifier = Modifier.size(86.dp),
                containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = Color.white
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "توقف ضبط" else "شروع صحبت",
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isListening) "در حال ضبط... لمس کنید تا متوقف شود" else "برای شروع صحبت دکمه را لمس کنید",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
