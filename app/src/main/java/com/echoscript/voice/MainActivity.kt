package com.echoscript.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.echoscript.voice.service.FloatingVoiceBubbleService
import com.echoscript.voice.ui.screens.HomeScreen
import com.echoscript.voice.ui.screens.PromptStudioScreen
import com.echoscript.voice.ui.screens.SettingsScreen
import com.echoscript.voice.ui.theme.EchoScriptVoiceTheme
import com.echoscript.voice.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EchoScriptVoiceTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Handle Audio Recording Permission Launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.onRecordPermissionGranted()
                    } else {
                        Toast.makeText(this, "برای ضبط صدا نیاز به دسترسی میکروفون است", Toast.LENGTH_SHORT).show()
                    }
                }

                // Overlay Permission Request for Floating Bubble
                val overlayPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    if (Settings.canDrawOverlays(this)) {
                        startFloatingBubbleService()
                    } else {
                        Toast.makeText(this, "مجوز نمایش روی سایر برنامه‌ها داده نشد", Toast.LENGTH_SHORT).show()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Mic, contentDescription = "ضبط صوت") },
                                label = { Text("گفتار به متن") },
                                selected = currentRoute == "home",
                                onClick = { navController.navigate("home") { launchSingleTop = true } }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Psychology, contentDescription = "استودیو پرامپت") },
                                label = { Text("پرامپت هوشمند") },
                                selected = currentRoute == "prompts",
                                onClick = { navController.navigate("prompts") { launchSingleTop = true } }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = "تنظیمات") },
                                label = { Text("تنظیمات") },
                                selected = currentRoute == "settings",
                                onClick = { navController.navigate("settings") { launchSingleTop = true } }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onRequestPermission = {
                                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        viewModel.startSpeechRecognition()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                onToggleFloatingBubble = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this@MainActivity)) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:$packageName")
                                        )
                                        overlayPermissionLauncher.launch(intent)
                                    } else {
                                        toggleFloatingBubbleService()
                                    }
                                }
                            )
                        }
                        composable("prompts") {
                            PromptStudioScreen(viewModel = viewModel)
                        }
                        composable("settings") {
                            SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    private fun startFloatingBubbleService() {
        val intent = Intent(this, FloatingVoiceBubbleService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "حباب شناور صوتی بر روی صفحه فعال شد ✨", Toast.LENGTH_SHORT).show()
    }

    private fun toggleFloatingBubbleService() {
        if (FloatingVoiceBubbleService.isRunning) {
            stopService(Intent(this, FloatingVoiceBubbleService::class.java))
            Toast.makeText(this, "حباب شناور متوقف شد", Toast.LENGTH_SHORT).show()
        } else {
            startFloatingBubbleService()
        }
    }
}
