package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apps.AppAccessManager
import com.example.data.model.InstalledAppInfo
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDim
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNexusScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val apps by viewModel.appAccessManager.installedApps.collectAsState()
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = apps.filter {
        it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STARK APPLICATION NEXUS",
                        color = ArcCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Universal App Access & Voice Launcher",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            viewModel.appAccessManager.refreshInstalledApps()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("refresh_installed_apps_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Apps", color = ArcCyanGlow, fontSize = 11.sp)
                }
            }
        }

        // Voice instruction banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderCyan, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ArcCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "VOICE DISPATCH PROTOCOL",
                            color = ArcCyanGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Say: \"Open WhatsApp\", \"Launch Camera\", \"Open YouTube\", \"Settings खोलो\"",
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search any application on device...", fontSize = 12.sp, color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArcCyan,
                    unfocusedBorderColor = BorderCyan,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Quick Launch Hub (Common Apps)
        item {
            Text(
                text = "FREQUENT TARGETS",
                color = ArcCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val quickApps = listOf(
                Triple("YouTube", "com.google.android.youtube", Icons.Default.VideoLibrary),
                Triple("Camera", "camera", Icons.Default.CameraAlt),
                Triple("Chrome", "com.android.chrome", Icons.Default.Language),
                Triple("Spotify", "com.spotify.music", Icons.Default.MusicNote),
                Triple("Maps", "com.google.android.apps.maps", Icons.Default.Map),
                Triple("Settings", "settings", Icons.Default.Settings)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickApps.forEach { (name, target, icon) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.openApp(target)
                            }
                            .border(1.dp, BorderCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, contentDescription = name, tint = ArcCyanGlow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(name, color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        }
                    }
                }
            }
        }

        // App List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISCOVERED APPS (${filteredApps.size})",
                    color = ArcCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        // App Items
        items(filteredApps, key = { it.packageName }) { app ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderCyan, RoundedCornerShape(10.dp))
                    .testTag("app_item_${app.packageName}"),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisSurfaceElevated)
                                .border(1.dp, ArcCyanDim, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = ArcCyanGlow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = app.appName,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = app.packageName,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.openApp(app.packageName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("launch_button_${app.packageName}")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = "Launch", tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
