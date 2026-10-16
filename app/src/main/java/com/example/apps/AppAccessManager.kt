package com.example.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import com.example.data.model.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AppAccessManager(private val context: Context) {

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val commonPresetApps = listOf(
        InstalledAppInfo("com.google.android.youtube", "YouTube", false, 42, "Media"),
        InstalledAppInfo("com.whatsapp", "WhatsApp", false, 68, "Social"),
        InstalledAppInfo("com.android.chrome", "Chrome", false, 51, "Productivity"),
        InstalledAppInfo("com.spotify.music", "Spotify", false, 33, "Music"),
        InstalledAppInfo("com.google.android.apps.maps", "Google Maps", false, 24, "Navigation"),
        InstalledAppInfo("com.google.android.gm", "Gmail", false, 29, "Communication"),
        InstalledAppInfo("com.android.settings", "Settings", true, 19, "System"),
        InstalledAppInfo("com.android.camera", "Camera", true, 45, "Tools"),
        InstalledAppInfo("com.google.android.apps.photos", "Photos", false, 28, "Media"),
        InstalledAppInfo("com.android.dialer", "Phone", true, 37, "Communication"),
        InstalledAppInfo("com.android.calculator2", "Calculator", true, 15, "Tools"),
        InstalledAppInfo("com.android.vending", "Google Play Store", true, 22, "System")
    )

    init {
        _installedApps.value = commonPresetApps
    }

    suspend fun refreshInstalledApps() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = try {
            pm.queryIntentActivities(mainIntent, 0)
        } catch (e: Exception) {
            emptyList()
        }

        val apps = mutableListOf<InstalledAppInfo>()
        for (ri in resolved) {
            val pkg = ri.activityInfo.packageName
            val label = try {
                ri.loadLabel(pm).toString()
            } catch (e: Exception) {
                pkg
            }
            val isSystem = try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            } catch (e: Exception) {
                false
            }
            if (label.isNotBlank() && pkg != context.packageName) {
                apps.add(
                    InstalledAppInfo(
                        packageName = pkg,
                        appName = label,
                        isSystemApp = isSystem,
                        category = if (isSystem) "System" else "User App"
                    )
                )
            }
        }

        // Combine device apps with presets so that emulator / devices without certain apps still show a rich catalog
        val combined = (apps + commonPresetApps)
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }

        _installedApps.value = combined
    }

    // Launch any application by exact package name or fuzzy app name
    fun launchAppByName(nameQuery: String): LaunchResult {
        val cleanQuery = nameQuery.trim().lowercase()
        val pm = context.packageManager

        // 1. Direct hardware / system actions
        when {
            cleanQuery == "camera" || cleanQuery == "take photo" || cleanQuery == "कैमरा" -> {
                val camIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                return tryLaunch(camIntent, "Camera")
            }
            cleanQuery == "settings" || cleanQuery == "setting" || cleanQuery == "सेटिंग" -> {
                val setIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                return tryLaunch(setIntent, "Settings")
            }
            cleanQuery.contains("play store") || cleanQuery.contains("store") -> {
                val storeIntent = pm.getLaunchIntentForPackage("com.android.vending")
                if (storeIntent != null) return tryLaunch(storeIntent, "Google Play Store")
            }
        }

        // 2. Search installed apps by name or package match
        val matchedApp = _installedApps.value.firstOrNull { app ->
            val appLower = app.appName.lowercase()
            appLower == cleanQuery ||
                    appLower.contains(cleanQuery) ||
                    cleanQuery.contains(appLower) ||
                    app.packageName.lowercase().contains(cleanQuery)
        }

        if (matchedApp != null) {
            val intent = pm.getLaunchIntentForPackage(matchedApp.packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                return tryLaunch(intent, matchedApp.appName)
            }
        }

        // 3. Fallback: Search on Google Play Store or web
        return try {
            val playIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=${Uri.encode(nameQuery)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(playIntent)
            LaunchResult.MarketFallback(nameQuery)
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=${Uri.encode(nameQuery)}&c=apps")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                LaunchResult.MarketFallback(nameQuery)
            } catch (ex: Exception) {
                LaunchResult.NotFound(nameQuery)
            }
        }
    }

    fun launchPackage(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun tryLaunch(intent: Intent, appName: String): LaunchResult {
        return try {
            context.startActivity(intent)
            LaunchResult.Success(appName)
        } catch (e: Exception) {
            LaunchResult.NotFound(appName)
        }
    }

    sealed class LaunchResult {
        data class Success(val appName: String) : LaunchResult()
        data class MarketFallback(val appName: String) : LaunchResult()
        data class NotFound(val appName: String) : LaunchResult()
    }
}
