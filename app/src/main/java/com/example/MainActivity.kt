package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.StarkHudHologramBackground
import com.example.ui.screens.AppNexusScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CallsScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.DataVaultScreen
import com.example.ui.screens.DocumentScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveScreeningDialog
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.VoicePayScreen
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTheme
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisScreen
import com.example.ui.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JarvisTheme {
                val viewModel: JarvisViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                // Permission Launcher
                val permissionsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    if (permissions[Manifest.permission.READ_CONTACTS] == true) {
                        viewModel.refreshContactsFromDevice()
                    }
                    if (permissions[Manifest.permission.READ_CALENDAR] == true ||
                        permissions[Manifest.permission.WRITE_CALENDAR] == true
                    ) {
                        viewModel.refreshCalendarFromDevice()
                    }
                }

                val requiresSystemDialog by viewModel.voiceManager.requiresSystemDialog.collectAsState()

                // Speech Recognizer Intent Launcher Fallback
                val speechDialogLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == android.app.Activity.RESULT_OK) {
                        val matches = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            viewModel.voiceManager.processVoiceInput(text)
                        }
                    }
                    viewModel.voiceManager.resetSystemDialogFlag()
                }

                LaunchedEffect(requiresSystemDialog) {
                    if (requiresSystemDialog) {
                        try {
                            speechDialogLauncher.launch(viewModel.voiceManager.createSpeechRecognizerIntent())
                        } catch (e: Exception) {
                            viewModel.voiceManager.resetSystemDialogFlag()
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    permissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.CALL_PHONE
                        )
                    )
                }

                // Custom BackHandler: Return to Console
                BackHandler(enabled = currentScreen != JarvisScreen.Console) {
                    viewModel.navigateTo(JarvisScreen.Console)
                }

                val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
                val isListening by viewModel.voiceManager.isListening.collectAsState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JarvisBackground),
                    bottomBar = {
                        JarvisBottomNavigation(
                            currentScreen = currentScreen,
                            onScreenSelected = { screen ->
                                viewModel.navigateTo(screen)
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .statusBarsPadding()
                    ) {
                        // Ambient glowing and moving Stark HUD background
                        StarkHudHologramBackground(
                            modifier = Modifier.fillMaxSize(),
                            isVoiceActive = isSpeaking || isListening
                        )

                        Crossfade(targetState = currentScreen, label = "ScreenCrossfade") { screen ->
                            when (screen) {
                                JarvisScreen.Console -> HomeScreen(
                                    viewModel = viewModel,
                                    onRequestRecordAudioPermission = {
                                        permissionsLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                                    },
                                    onLaunchSpeechDialog = {
                                        try {
                                            speechDialogLauncher.launch(viewModel.voiceManager.createSpeechRecognizerIntent())
                                        } catch (e: Exception) {
                                            // Handle
                                        }
                                    }
                                )

                                JarvisScreen.Apps -> AppNexusScreen(
                                    viewModel = viewModel
                                )

                                JarvisScreen.VoicePay -> VoicePayScreen(
                                    viewModel = viewModel
                                )

                                JarvisScreen.DataVault -> DataVaultScreen(
                                    viewModel = viewModel
                                )

                                JarvisScreen.Calls -> CallsScreen(
                                    viewModel = viewModel
                                )

                                JarvisScreen.Messages -> MessagesScreen(
                                    viewModel = viewModel
                                )

                                JarvisScreen.Documents -> DocumentScreen(
                                    viewModel = viewModel
                                )

                                JarvisScreen.Schedule -> CalendarScreen(
                                    viewModel = viewModel,
                                    onRequestCalendarPermission = {
                                        permissionsLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.READ_CALENDAR,
                                                Manifest.permission.WRITE_CALENDAR
                                            )
                                        )
                                    }
                                )

                                JarvisScreen.Contacts -> ContactsScreen(
                                    viewModel = viewModel,
                                    onRequestContactsPermission = {
                                        permissionsLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS))
                                    }
                                )
                            }
                        }

                        // Top-level Live Incoming Call Screening Dialog
                        LiveScreeningDialog(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun JarvisBottomNavigation(
    currentScreen: JarvisScreen,
    onScreenSelected: (JarvisScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Pair(JarvisScreen.Console, Icons.Default.SmartToy),
        Pair(JarvisScreen.Apps, Icons.Default.Apps),
        Pair(JarvisScreen.VoicePay, Icons.Default.Payment),
        Pair(JarvisScreen.DataVault, Icons.Default.Security),
        Pair(JarvisScreen.Calls, Icons.Default.PhoneInTalk),
        Pair(JarvisScreen.Documents, Icons.Default.Description)
    )

    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .border(width = 1.dp, color = BorderCyan)
            .testTag("jarvis_bottom_navigation"),
        containerColor = JarvisSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { (screen, icon) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        text = when (screen) {
                            JarvisScreen.Console -> "JARVIS"
                            JarvisScreen.Apps -> "Apps"
                            JarvisScreen.VoicePay -> "Pay"
                            JarvisScreen.DataVault -> "Vault"
                            JarvisScreen.Calls -> "Calls"
                            JarvisScreen.Documents -> "Docs"
                            JarvisScreen.Messages -> "Comms"
                            JarvisScreen.Schedule -> "Cal"
                            JarvisScreen.Contacts -> "VIPs"
                        },
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = ArcCyanGlow,
                    indicatorColor = ArcCyan,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag("nav_item_${screen.route}")
            )
        }
    }
}
