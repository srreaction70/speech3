package com.echoscript.voice.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echoscript.voice.viewmodel.MainViewModel

data class PromptPreset(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val styleKey: String
)

@Composable
fun PromptStudioScreen(viewModel: MainViewModel) {
    val presets = listOf(
        PromptPreset("کدنویسی و توسعه نرم‌افزار", "تبدیل ایده به پرامپت دقیق برنامه‌نویسی، معماری و رفع باگ", Icons.Default.Code, "coding"),
        PromptPreset("نگارش ایمیل رسمی و اداری", "تبدیل صوت به ایمیل‌های اداری با لحن محترمانه و حرفه‌ای", Icons.Default.Email, "email"),
        PromptPreset("خلاصه‌سازی و مقاله علمی", "تنظیم پرامپت برای دریافت ساختار مقاله و پژوهش دانشگاهی", Icons.Default.Description, "academic"),
        PromptPreset("ایده‌پردازی و سناریونویسی", "تولید محتوا، سناریوی ویدیو و استراتژی شبکه‌های اجتماعی", Icons.Default.Psychology, "creative")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "استودیو مهندسی پرامپت هوش مصنوعی",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "سبک مورد نظر خود را انتخاب کرده و ایده صوتی خود را به پرامپت استاندارد تبدیل کنید",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(presets) { preset ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    onClick = { viewModel.professionalizeCurrentText(preset.styleKey) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = preset.icon,
                                    contentDescription = preset.title,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = preset.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
