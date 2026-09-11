package com.echoscript.voice.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
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
    val isRecording by viewModel.isRecording.collectAsState()
    val isProcessingAI by viewModel.isProcessingAI.collectAsState()
    val durationFormatted by viewModel.durationFormatted.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val finalText by viewModel.finalText.collectAsState()
    val promptEnhancerEnabled by viewModel.promptEnhancerEnabled.collectAsState()

    val displayText = finalText

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.22f else 1f,
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
                        text = "تایپ صوتی در تلگرام، واتساپ، ایتا و مرورگر بدون ترک برنامه",
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

        Spacer(modifier = Modifier.height(16.dp))

        // Recording Status & Live Duration Pill
        AnimatedVisibility(visible = isRecording) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    )
                    Text(
                        text = "در حال ضبط پیوسته: $durationFormatted",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isRecording) 
                                "🎙️ در حال ضبط صدای شما... با خیال راحت صحبت کنید.\n(صدا با مکث شما قطع نمی‌شود؛ پس از پایان دکمه توقف را بزنید)"
                            else if (isProcessingAI) 
                                "⏳ در حال پردازش صوت با هوش مصنوعی و تبدیل به متن فارسی..."
                            else 
                                "روی دکمه میکروفون زیر بزنید تا ضبط شروع شود.\nصحبت‌هایتان به صورت کامل ضبط شده و پس از زدن توقف، تبدیل به متن دقیق می‌شود.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        text = displayText,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isProcessingAI) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "هوش مصنوعی در حال تبدیل صوت به متن...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick AI Actions Row (Translate, Professionalize, Polish, Copy, Share)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedButton(
                onClick = { viewModel.translateCurrentText() },
                enabled = displayText.isNotBlank() && !isProcessingAI && !isRecording,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Translate, contentDescription = "ترجمه", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ترجمه", fontSize = 12.sp)
            }

            ElevatedButton(
                onClick = { viewModel.professionalizeCurrentText() },
                enabled = displayText.isNotBlank() && !isProcessingAI && !isRecording,
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
                        Toast.makeText(context, "متن کپی شد 📋", Toast.LENGTH_SHORT).show()
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

        Spacer(modifier = Modifier.height(28.dp))

        // Big Touch-Friendly Mic / Stop Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(150.dp)
        ) {
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.25f))
                )
            }

            FloatingActionButton(
                onClick = {
                    if (isRecording) {
                        viewModel.stopSpeechRecognition()
                    } else {
                        onRequestPermission()
                    }
                },
                shape = CircleShape,
                modifier = Modifier.size(92.dp),
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = Color.white
            ) {
                if (isProcessingAI) {
                    CircularProgressIndicator(
                        color = Color.white,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "توقف ضبط و تبدیل به متن" else "شروع ضبط صدا",
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isRecording) "🔴 در حال ضبط... پس از پایان صحبت، دکمه را لمس کنید تا به متن تبدیل شود"
                   else if (isProcessingAI) "⏳ در حال پردازش و استخراج متن با هوش مصنوعی..."
                   else "برای شروع ضبط صدا دکمه را لمس کنید",
            fontSize = 13.sp,
            fontWeight = if (isRecording) FontWeight.Bold else FontWeight.Normal,
            color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "ویژگی ضبط پیوسته: صدا با مکث‌های شما قطع نمی‌شود و تا زمان لمس توقف ادامه دارد.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
