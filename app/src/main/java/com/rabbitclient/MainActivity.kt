package com.rabbitclient

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF080B10)
private val Panel = Color(0xCC121821)
private val PanelRaised = Color(0xD919222D)
private val Accent = Color(0xFFB7F7D5)
private val AccentDeep = Color(0xFF62D9A0)
private val TextMuted = Color(0xFF91A0B2)
private val Line = Color(0xFF263341)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RabbitApp() }
    }
}

private data class ModuleItem(
    val id: String,
    val name: String,
    val detail: String,
    val category: String,
    val planned: Boolean = true
)

private val moduleList = listOf(
    ModuleItem("fps", "FPS Counter", "Frame-rate overlay", "HUD"),
    ModuleItem("coords", "Coordinates", "Player position overlay", "HUD"),
    ModuleItem("cps", "CPS Counter", "Click-rate display", "HUD"),
    ModuleItem("armor", "Armor HUD", "Equipment status", "HUD"),
    ModuleItem("potion", "Potion HUD", "Active effects display", "HUD"),
    ModuleItem("fullbright", "Fullbright", "Brightness control", "Visual"),
    ModuleItem("zoom", "Zoom", "Quick zoom control", "Utility"),
    ModuleItem("sprint", "Sprint Toggle", "Sprint control", "Utility"),
    ModuleItem("fov", "FOV Controls", "Field-of-view presets", "Visual")
)

@Composable
private fun RabbitApp() {
    var page by remember { mutableStateOf("Home") }
    var category by remember { mutableStateOf("All") }
    val context = LocalContext.current
    var enabled by remember(context) { mutableStateOf(loadEnabledModules(context)) }
    var wallpaperUri by remember(context) { mutableStateOf(context.getSharedPreferences("rabbit_ui_preferences", Context.MODE_PRIVATE).getString("wallpaper_uri", null)) }
    val fullWallpaper: ImageBitmap? = remember(wallpaperUri) {
        try { wallpaperUri?.let { raw -> context.contentResolver.openInputStream(Uri.parse(raw))?.use { BitmapFactory.decodeStream(it)?.asImageBitmap() } } } catch (_: Exception) { null }
    }

    // Preview preferences survive app restarts. These are UI preferences only;
    // they are not sent to Minecraft until a real runtime adapter exists.
    LaunchedEffect(enabled) {
        saveEnabledModules(context, enabled)
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            onPrimary = Bg,
            secondary = AccentDeep,
            background = Bg,
            surface = Panel,
            onBackground = Color(0xFFF4F7FA),
            onSurface = Color(0xFFF4F7FA),
            surfaceVariant = PanelRaised,
            onSurfaceVariant = TextMuted
        )
    ) {
        Box(Modifier.fillMaxSize().background(Bg)) {
            if (fullWallpaper != null) {
                Image(bitmap = fullWallpaper, contentDescription = "Rabbit Client background wallpaper", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Color(0x66060A10)))
            }
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xCC0D1219),
                    contentColor = TextMuted,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = page == "Home",
                        onClick = { page = "Home" },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") },
                        colors = navColors()
                    )
                    NavigationBarItem(
                        selected = page == "Modules",
                        onClick = { page = "Modules" },
                        icon = { Icon(Icons.Default.ViewModule, contentDescription = null) },
                        label = { Text("Modules") },
                        colors = navColors()
                    )
                    NavigationBarItem(
                        selected = page == "Instances",
                        onClick = { page = "Instances" },
                        icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                        label = { Text("Instances") },
                        colors = navColors()
                    )
                    NavigationBarItem(
                        selected = page == "Settings",
                        onClick = { page = "Settings" },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Settings") },
                        colors = navColors()
                    )
                }
            }
        ) { insets ->
            Column(
                Modifier.fillMaxSize()
                    .padding(insets)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                if (page != "Home") {
                    Spacer(Modifier.height(22.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(16.dp), color = Accent) {
                            Text("R", modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
                                color = Bg, fontWeight = FontWeight.Black, fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("RABBIT CLIENT", color = Accent, fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold, letterSpacing = 2.1.sp)
                            Text(page, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }
                        Surface(color = PanelRaised, shape = RoundedCornerShape(12.dp)) {
                            Text("v0.1.0", color = TextMuted,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                } else {
                    Spacer(Modifier.height(8.dp))
                }

                when (page) {
                    "Home" -> HomeScreen(
                        enabledCount = enabled.size,
                        onModules = { page = "Modules" },
                        onInstances = { page = "Instances" }
                    )
                    "Modules" -> ModulesScreen(
                        category = category,
                        onCategory = { category = it },
                        enabled = enabled,
                        onEnabled = { id, isOn ->
                            enabled = if (isOn) enabled + id else enabled - id
                        }
                    )
                    "Instances" -> InstancesScreen()
                    "CurseForge" -> CurseForgeScreen(onBack = { page = "Settings" })
                    else -> SettingsScreen(wallpaperUri = wallpaperUri, onWallpaperChange = { wallpaperUri = it }, onCurseForge = { page = "CurseForge" })
                }
                Spacer(Modifier.height(24.dp))
            }
        }
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Bg,
    selectedTextColor = Accent,
    indicatorColor = Accent,
    unselectedIconColor = TextMuted,
    unselectedTextColor = TextMuted
)

private const val MODULE_PREFERENCES = "rabbit_module_preferences"
private const val ENABLED_MODULES_KEY = "enabled_preview_modules"

private fun loadEnabledModules(context: Context): Set<String> =
    context.getSharedPreferences(MODULE_PREFERENCES, Context.MODE_PRIVATE)
        .getStringSet(ENABLED_MODULES_KEY, emptySet())
        ?.toSet()
        .orEmpty()
        .intersect(moduleList.map { it.id }.toSet())

private fun saveEnabledModules(context: Context, enabled: Set<String>) {
    context.getSharedPreferences(MODULE_PREFERENCES, Context.MODE_PRIVATE)
        .edit()
        .putStringSet(ENABLED_MODULES_KEY, enabled.toSet())
        .apply()
}

private data class InstalledGame(val packageName: String, val versionName: String)

private fun detectInstalledGame(context: Context): InstalledGame? {
    val candidates = listOf(
        "com.mojang.minecraftpe",
        "com.mojang.minecraftpe.beta",
        "com.mojang.minecraftpe.preview"
    )
    for (packageName in candidates) {
        try {
            val info = context.packageManager.getPackageInfo(packageName, 0)
            val version = info.versionName ?: "Unknown"
            return InstalledGame(packageName, version)
        } catch (_: Exception) {
            // Package is not installed or isn't visible to this app.
        }
    }
    return null
}

@Composable
private fun HomeScreen(enabledCount: Int, onModules: () -> Unit, onInstances: () -> Unit) {
    val context = LocalContext.current
    val installedGame = remember { detectInstalledGame(context) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(color = Panel, shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Minecraft", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                        Text("Bedrock Edition", color = TextMuted, fontSize = 13.sp)
                    }
                    Surface(color = PanelRaised, shape = RoundedCornerShape(12.dp), modifier = Modifier.widthIn(max = 150.dp)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("▣", color = Accent, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text("CURRENT INSTANCE", color = TextMuted, fontSize = 8.sp, letterSpacing = 0.5.sp)
                                Text(installedGame?.versionName ?: "Not imported", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(onClick = {
                    val launchIntent = installedGame?.let { context.packageManager.getLaunchIntentForPackage(it.packageName) }
                    if (launchIntent != null) context.startActivity(launchIntent) else onInstances()
                }, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)) {
                    Text(if (installedGame != null) "▶  Launch" else "Import / Set up game", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Text("TIP", color = AccentDeep, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(if (installedGame != null) "Detected ${installedGame.versionName}. Runtime isolation is not enabled in this build yet." else "Install the official Minecraft Bedrock app, or open Profiles to check the current setup.", color = TextMuted, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Surface(color = Panel, shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.weight(1f)) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Modules", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = onModules, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) { Text("Manage", color = Accent, fontSize = 10.sp) }
                    }
                    Spacer(Modifier.height(5.dp))
                    Text("Preview controls", color = TextMuted, fontSize = 10.sp)
                    Spacer(Modifier.height(9.dp))
                    listOf("FPS Counter", "Coordinates", "CPS Counter").forEachIndexed { index, label ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                            Box(Modifier.size(6.dp).background(if (index == 0 && enabledCount > 0) AccentDeep else Line, CircleShape))
                            Spacer(Modifier.width(7.dp))
                            Text(label, color = Color.White, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Demo only · no in-game effect", color = TextMuted, fontSize = 9.sp, lineHeight = 13.sp)
                }
            }
            Surface(color = Panel, shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.weight(1.15f)) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Content", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = onInstances, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) { Text("View all", color = TextMuted, fontSize = 10.sp) }
                    }
                    Spacer(Modifier.height(5.dp))
                    ContentPreviewRow("Worlds", "Manage saves", "W")
                    HorizontalDivider(color = Line)
                    ContentPreviewRow("Resource packs", "Visual packs", "R")
                    HorizontalDivider(color = Line)
                    ContentPreviewRow("Behavior packs", "Add-ons", "B")
                }
            }
        }
        Surface(color = Panel, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Rabbit Client", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("By KHARGOSH & CHATGPT · isolated runtime in development", color = TextMuted, fontSize = 10.sp, lineHeight = 14.sp)
                }
                Text("v0.1.0", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ContentPreviewRow(title: String, subtitle: String, glyph: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(color = PanelRaised, shape = RoundedCornerShape(8.dp)) {
            Text(glyph, color = Accent, modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextMuted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun ModulesScreen(
    category: String,
    onCategory: (String) -> Unit,
    enabled: Set<String>,
    onEnabled: (String, Boolean) -> Unit
) {
    SectionLabel("MODULE LIBRARY")
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf("All", "HUD", "Visual", "Utility").forEach {
            FilterChip(
                selected = category == it,
                onClick = { onCategory(it) },
                label = { Text(it) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Accent,
                    selectedLabelColor = Bg,
                    labelColor = TextMuted
                )
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    Text("Preview switches only. Real module support needs a verified in-game integration.",
        color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
    Spacer(Modifier.height(12.dp))
    moduleList.filter { category == "All" || it.category == category }.forEach { item ->
        Surface(color = Panel, shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp)) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = PanelRaised, shape = RoundedCornerShape(12.dp)) {
                    Text(item.category.take(1), color = Accent, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.name, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(3.dp))
                    Text(item.detail, color = TextMuted, fontSize = 12.sp)
                    Text("${if (item.id in enabled) "PREVIEW ON" else "PLANNED"} • ${item.category.uppercase()}", color = AccentDeep,
                        fontSize = 9.sp, letterSpacing = 1.sp)
                }
                Switch(checked = item.id in enabled,
                    onCheckedChange = { onEnabled(item.id, it) })
            }
        }
    }
}

@Composable
private fun InstancesScreen() {
    val context = LocalContext.current
    val installedGame = remember { detectInstalledGame(context) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text("Game profiles", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(5.dp))
            Text("Keep each Minecraft setup organized and easy to identify.", color = TextMuted, fontSize = 13.sp, lineHeight = 19.sp)
        }
        Surface(color = Panel, shape = RoundedCornerShape(24.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFF20392F), shape = RoundedCornerShape(15.dp)) { Text("MC", color = Accent, modifier = Modifier.padding(horizontal = 15.dp, vertical = 14.dp), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Installed Minecraft", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(installedGame?.versionName ?: "Not detected", color = TextMuted, fontSize = 12.sp)
                    }
                    Surface(color = if (installedGame != null) Color(0xFF20392F) else PanelRaised, shape = RoundedCornerShape(9.dp)) { Text(if (installedGame != null) "FOUND" else "MISSING", color = if (installedGame != null) Accent else TextMuted, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold) }
                }
                Spacer(Modifier.height(16.dp))
                Text("Current installation is detected from Android's installed packages. Launching it does not create an isolated copy.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(14.dp))
                Button(onClick = { installedGame?.let { context.packageManager.getLaunchIntentForPackage(it.packageName) }?.let { context.startActivity(it) } }, enabled = installedGame != null, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg, disabledContainerColor = PanelRaised, disabledContentColor = TextMuted)) { Text("Launch installed game", fontWeight = FontWeight.Bold) }
            }
        }
        SectionLabel("ISOLATED PROFILES")
        Surface(color = Panel, shape = RoundedCornerShape(24.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Box(Modifier.size(48.dp).background(PanelRaised, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) { Text("+", color = Accent, fontSize = 28.sp, fontWeight = FontWeight.Light) }
                Spacer(Modifier.height(15.dp))
                Text("Create a separate game profile", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                Text("A real isolated profile needs a managed Minecraft runtime, separate game data, and a verified launch path. This is not implemented in the current build yet.", color = TextMuted, fontSize = 12.sp, lineHeight = 19.sp)
                Spacer(Modifier.height(14.dp))
                Surface(color = Color(0xFF2C2519), shape = RoundedCornerShape(10.dp)) { Text("RUNTIME INTEGRATION REQUIRED", color = Color(0xFFFFD98A), modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp) }
            }
        }
        SectionLabel("PLANNED PROFILE TOOLS")
        Surface(color = Panel, shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                WorkspaceRow("Import official game", "Prepare a user-provided game package", "I")
                HorizontalDivider(color = Line)
                WorkspaceRow("Worlds & packs", "Separate content by profile", "W")
                HorizontalDivider(color = Line)
                WorkspaceRow("Backup & restore", "Protect profile data before changes", "B")
            }
        }
    }
}
@Composable
private fun WorkspaceRow(title: String, subtitle: String, glyph: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = PanelRaised, shape = RoundedCornerShape(10.dp)) {
            Text(
                glyph,
                color = Accent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Icon(Icons.Default.Tune, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SettingsScreen(wallpaperUri: String?, onWallpaperChange: (String?) -> Unit, onCurseForge: () -> Unit) {
    val context = LocalContext.current
    val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
            onWallpaperChange(uri.toString())
            context.getSharedPreferences("rabbit_ui_preferences", Context.MODE_PRIVATE).edit().putString("wallpaper_uri", uri.toString()).apply()
        }
    }
    val wallpaperBitmap: ImageBitmap? = remember(wallpaperUri) {
        try { wallpaperUri?.let { raw -> context.contentResolver.openInputStream(Uri.parse(raw))?.use { BitmapFactory.decodeStream(it)?.asImageBitmap() } } } catch (_: Exception) { null }
    }
    SectionLabel("PERSONALIZATION")
    Spacer(Modifier.height(10.dp))
    Surface(color = Panel, shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Text("Obsidian + Mint", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Current visual direction", color = TextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(Bg, Panel, Accent, AccentDeep).forEach { color -> Box(Modifier.weight(1f).height(34.dp).background(color, RoundedCornerShape(10.dp))) } }
            Spacer(Modifier.height(22.dp))
            Text("Launcher wallpaper", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Choose a background image for your launcher.", color = TextMuted, fontSize = 12.sp)
            if (wallpaperBitmap != null) { Spacer(Modifier.height(10.dp)); Image(bitmap = wallpaperBitmap, contentDescription = "Selected wallpaper", modifier = Modifier.fillMaxWidth().height(145.dp), contentScale = ContentScale.Crop) }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { wallpaperPicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)) { Icon(Icons.Default.Image, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Choose wallpaper", fontWeight = FontWeight.Bold) }
            if (wallpaperUri != null) { Spacer(Modifier.height(8.dp)); OutlinedButton(onClick = { onWallpaperChange(null); context.getSharedPreferences("rabbit_ui_preferences", Context.MODE_PRIVATE).edit().remove("wallpaper_uri").apply() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Reset wallpaper", color = Color.White) } }
            Spacer(Modifier.height(20.dp))
            Text("CurseForge", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Discover Minecraft Bedrock add-ons.", color = TextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onCurseForge, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Language, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Browse CurseForge", color = Color.White) }
            Text("CurseForge opens inside Rabbit Client. No .levipack support.", color = TextMuted, fontSize = 11.sp, lineHeight = 16.sp)
            Spacer(Modifier.height(20.dp))
            Text("Authors & credits", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Surface(color = PanelRaised, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Accent, shape = RoundedCornerShape(10.dp)) {
                        Text("R", color = Bg, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("KHARGOSH & CHATGPT", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        Text("Rabbit Client · UI implementation", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Compatibility", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text("Target: Bedrock 1.21.90 and newer. Future releases require runtime checks and testing; launcher support and in-game module support are separate.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(18.dp))
            Text("Integration status", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text("The current module controls are demo state only. They do not modify Minecraft or guarantee performance improvements.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}
@Composable
private fun CurseForgeScreen(onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(12.dp)) { Text("← Back", color = Color.White) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("CurseForge", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Minecraft Bedrock add-ons", color = TextMuted, fontSize = 11.sp)
            }
        }
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(620.dp),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    loadUrl("https://www.curseforge.com/minecraft-bedrock")
                }
            },
            update = { view ->
                if (view.url == null) view.loadUrl("https://www.curseforge.com/minecraft-bedrock")
            }
        )
        Text("CurseForge content is displayed from its website. Downloads and licensing follow CurseForge and each creator's terms.", color = TextMuted, fontSize = 10.sp, lineHeight = 14.sp)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
}

@Composable
private fun MetricCard(label: String, value: String, caption: String, modifier: Modifier = Modifier) {
    Surface(color = Panel, shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(7.dp))
            Text(value, color = Accent, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(caption, color = TextMuted, fontSize = 11.sp)
        }
    }
}
