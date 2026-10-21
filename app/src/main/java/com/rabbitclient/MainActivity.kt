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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.vector.ImageVector
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

private val BgState = mutableStateOf(Color(0xFF080B10))
private val AccentState = mutableStateOf(Color(0xFFB7F7D5))
private val AccentDeepState = mutableStateOf(Color(0xFF62D9A0))
private val Bg: Color get() = BgState.value
private val Panel = Color(0xA610141A)
private val PanelRaised = Color(0xB8141A22)
private val Accent: Color get() = AccentState.value
private val AccentDeep: Color get() = AccentDeepState.value
private val TextMuted = Color(0xFF91A0B2)
private val Line = Color(0xFF263341)

private const val RABBIT_REPOSITORY_URL = "https://github.com/khargosh-area/rabbitclient"
private val RABBIT_THEME_NAMES = listOf("Obsidian + Mint", "Midnight Blue", "Amethyst", "Ember")

private fun applyRabbitTheme(name: String) {
    val colors = when (name) {
        "Midnight Blue" -> Triple(Color(0xFF080B13), Color(0xFF8EB8FF), Color(0xFF4C82E8))
        "Amethyst" -> Triple(Color(0xFF100A16), Color(0xFFD8B5FF), Color(0xFFA17AE8))
        "Ember" -> Triple(Color(0xFF130A08), Color(0xFFFFC08A), Color(0xFFF08A50))
        else -> Triple(Color(0xFF080B10), Color(0xFFB7F7D5), Color(0xFF62D9A0))
    }
    BgState.value = colors.first
    AccentState.value = colors.second
    AccentDeepState.value = colors.third
}

private data class DockDestination(val label: String, val page: String, val icon: ImageVector)

private val dockDestinations = listOf(
    DockDestination("Launch", "Home", Icons.Default.Home),
    DockDestination("Modules", "Modules", Icons.Default.ViewModule),
    DockDestination("Instances", "Instances", Icons.Default.Tune),
    DockDestination("CurseForge", "CurseForge", Icons.Default.Language),
    DockDestination("About", "About", Icons.Default.Info),
    DockDestination("Settings", "Settings", Icons.Default.Settings)
)

private fun openExternalLink(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
        // A compatible browser may not be available on the device.
    }
}

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
    val preferences = remember(context) { context.getSharedPreferences("rabbit_ui_preferences", Context.MODE_PRIVATE) }
    var selectedTheme by remember(context) { mutableStateOf(preferences.getString("theme_name", "Obsidian + Mint") ?: "Obsidian + Mint") }
    LaunchedEffect(selectedTheme) {
        preferences.edit().putString("theme_name", selectedTheme).apply()
        applyRabbitTheme(selectedTheme)
    }
    var enabled by remember(context) { mutableStateOf(loadEnabledModules(context)) }
    var wallpaperUri by remember(context) { mutableStateOf(context.getSharedPreferences("rabbit_ui_preferences", Context.MODE_PRIVATE).getString("wallpaper_uri", null)) }
    val homeWallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
            wallpaperUri = uri.toString()
            context.getSharedPreferences("rabbit_ui_preferences", Context.MODE_PRIVATE).edit().putString("wallpaper_uri", uri.toString()).apply()
        }
    }
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
                Box(Modifier.fillMaxSize().background(Color(0x33060A10)))
            }
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 6.dp),
                    shape = RoundedCornerShape(30.dp),
                    color = Color(0xF2080B10),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                    shadowElevation = 12.dp
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        dockDestinations.forEach { destination ->
                            DockItem(
                                destination = destination,
                                selected = page == destination.page,
                                onClick = { page = destination.page }
                            )
                        }
                    }
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
                        wallpaperSelected = wallpaperUri != null,
                        onWallpaperClick = { homeWallpaperPicker.launch(arrayOf("image/*")) },
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
                    "CurseForge" -> CurseForgeScreen(onBack = { page = "Home" })
                    "About" -> AboutScreen()
                    "Settings" -> SettingsScreen(
                        wallpaperUri = wallpaperUri,
                        onWallpaperChange = { wallpaperUri = it },
                        onCurseForge = { page = "CurseForge" },
                        selectedTheme = selectedTheme,
                        onThemeChange = { selectedTheme = it }
                    )
                    else -> HomeScreen(
                        enabledCount = enabled.size,
                        wallpaperSelected = wallpaperUri != null,
                        onWallpaperClick = { homeWallpaperPicker.launch(arrayOf("image/*")) },
                        onModules = { page = "Modules" },
                        onInstances = { page = "Instances" }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
        }
    }
}

@Composable
private fun DockItem(destination: DockDestination, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(49.dp).clickable(onClick = onClick).padding(horizontal = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier.size(42.dp)
                .background(if (selected) Accent else PanelRaised, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                tint = if (selected) Bg else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            destination.label,
            color = if (selected) Accent else TextMuted,
            fontSize = 8.sp,
            maxLines = 1,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

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
private fun HomeScreen(enabledCount: Int, wallpaperSelected: Boolean, onWallpaperClick: () -> Unit, onModules: () -> Unit, onInstances: () -> Unit) {
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
                    OutlinedButton(
                        onClick = onWallpaperClick,
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 7.dp),
                        shape = RoundedCornerShape(11.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(if (wallpaperSelected) "Wallpaper" else "Set wallpaper", fontSize = 10.sp, maxLines = 1)
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
                        Text("Content Management", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
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
private fun SettingsScreen(wallpaperUri: String?, onWallpaperChange: (String?) -> Unit, onCurseForge: () -> Unit, selectedTheme: String, onThemeChange: (String) -> Unit) {
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
    SectionLabel("APPEARANCE")
    Spacer(Modifier.height(10.dp))
    Surface(color = Panel, shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("Choose your theme", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Accent colours apply across the launcher and are saved on this device.", color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
            RABBIT_THEME_NAMES.forEach { name ->
                val preview = when (name) {
                    "Midnight Blue" -> Color(0xFF8EB8FF)
                    "Amethyst" -> Color(0xFFD8B5FF)
                    "Ember" -> Color(0xFFFFC08A)
                    else -> Color(0xFFB7F7D5)
                }
                Surface(
                    color = if (selectedTheme == name) PanelRaised else Color(0x6610141A),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(if (selectedTheme == name) 1.5.dp else 1.dp, if (selectedTheme == name) preview else Line),
                    modifier = Modifier.fillMaxWidth().clickable { onThemeChange(name) }
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(30.dp).background(preview, CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(if (name == selectedTheme) "Currently active" else "Tap to apply", color = TextMuted, fontSize = 10.sp)
                        }
                        if (name == selectedTheme) Text("ACTIVE", color = preview, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(20.dp))
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
private fun AboutScreen() {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        Surface(color = Panel, shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Accent, shape = RoundedCornerShape(15.dp)) {
                        Text("R", color = Bg, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp))
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Rabbit Client", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Minecraft Bedrock companion launcher", color = TextMuted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("A focused launcher UI for managing your Minecraft entry point, exploring community content, and organizing planned tools.", color = TextMuted, fontSize = 13.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(12.dp))
                Surface(color = PanelRaised, shape = RoundedCornerShape(10.dp)) {
                    Text("VERSION 0.1.0  ·  ANDROID", color = Accent, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                }
            }
        }
        SectionLabel("AUTHORS")
        Surface(color = Panel, shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Accent, shape = CircleShape) {
                        Text("K", color = Bg, fontWeight = FontWeight.Black, modifier = Modifier.padding(13.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("KHARGOSH", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Project owner · product direction", color = TextMuted, fontSize = 11.sp)
                    }
                }
                HorizontalDivider(color = Line)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = PanelRaised, shape = CircleShape) {
                        Text("GPT", color = Accent, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(13.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("CHATGPT", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Development and UI collaboration", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        }
        SectionLabel("SOURCE CODE")
        Surface(color = Panel, shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Rabbit Client on GitHub", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text("Browse source, issues, build workflow, and project updates.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { openExternalLink(context, RABBIT_REPOSITORY_URL) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp)) {
                    Icon(Icons.Default.Language, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Open GitHub repository", color = Color.White)
                }
                Text(RABBIT_REPOSITORY_URL, color = Accent, fontSize = 10.sp, lineHeight = 15.sp)
            }
        }
        SectionLabel("INTEGRATION STATUS")
        Surface(color = Panel, shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Runtime isolation is not implemented yet.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("The current app detects and launches an installed Minecraft package. Module toggles are UI previews and do not change the game. Bedrock compatibility and any future runtime integration need device testing.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun CurseForgeScreen(onBack: () -> Unit) {
    val categories = listOf("All", "Addons", "Maps", "Texture Packs", "Scripts", "Skins")
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var pageUrl by remember { mutableStateOf("https://www.curseforge.com/minecraft-bedrock/search?page=1&pageSize=20&sortBy=relevancy") }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("CurseForge", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Explore Minecraft Bedrock community content", color = TextMuted, fontSize = 11.sp)
            }
            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)) {
                Text("Back", color = Color.White, fontSize = 11.sp)
            }
        }
        Surface(color = Panel, shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Search Bedrock content", fontSize = 12.sp) },
                        shape = RoundedCornerShape(13.dp)
                    )
                    Button(
                        onClick = {
                            val type = if (selectedCategory == "All") "" else "&class=" + when (selectedCategory) {
                                "Texture Packs" -> "texture-packs"
                                else -> selectedCategory.lowercase()
                            }
                            pageUrl = "https://www.curseforge.com/minecraft-bedrock/search?search=${Uri.encode(query)}$type&page=1&pageSize=20&sortBy=relevancy"
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 13.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)
                    ) {
                        Text("Search", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    categories.forEach { item ->
                        val selected = selectedCategory == item
                        Surface(
                            color = if (selected) Accent else PanelRaised,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Accent else Line),
                            modifier = Modifier.clickable {
                                selectedCategory = item
                                val type = if (item == "All") "" else "&class=" + when (item) {
                                    "Texture Packs" -> "texture-packs"
                                    else -> item.lowercase()
                                }
                                pageUrl = "https://www.curseforge.com/minecraft-bedrock/search?page=1&pageSize=20&sortBy=relevancy" + type
                            }
                        ) {
                            Text(item, color = if (selected) Bg else TextMuted, fontSize = 9.sp, maxLines = 1, modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp))
                        }
                    }
                }
                Text("Listings load from CurseForge directly. Downloads follow each creator's terms; .levipack files are not supported.", color = TextMuted, fontSize = 10.sp, lineHeight = 15.sp)
            }
        }
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(570.dp),
            factory = { viewContext ->
                WebView(viewContext).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    loadUrl(pageUrl)
                }
            },
            update = { view ->
                if (view.url != pageUrl) view.loadUrl(pageUrl)
            }
        )
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
