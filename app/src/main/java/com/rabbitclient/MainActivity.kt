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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF080B10)
private val Panel = Color(0xFF121821)
private val PanelRaised = Color(0xFF19222D)
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
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF0D1219),
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
                    else -> SettingsScreen()
                }
                Spacer(Modifier.height(24.dp))
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            color = Panel, shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                Column(Modifier.padding(22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFF20392F), shape = RoundedCornerShape(10.dp)) {
                            Text("BEDROCK EDITION", color = Accent, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Text("RABBIT / 01", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Your game.\nYour setup.", color = Color.White, fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(10.dp))
                    Text("A cleaner home for Minecraft Bedrock, packs and profiles.", color = TextMuted, fontSize = 14.sp, lineHeight = 21.sp)
                    Spacer(Modifier.height(22.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = {
                            val launchIntent = installedGame?.let { context.packageManager.getLaunchIntentForPackage(it.packageName) }
                            if (launchIntent != null) context.startActivity(launchIntent) else onInstances()
                        }, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)) {
                            Text(if (installedGame != null) "▶  PLAY MINECRAFT" else "SET UP GAME", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = onInstances, modifier = Modifier.height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                            Text("PROFILES", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(if (installedGame != null) AccentDeep else Color(0xFFFFC66D), CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(if (installedGame != null) "Minecraft detected · ${installedGame.versionName}" else "Minecraft installation not detected", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickFeatureCard("INSTANCES", "Version profiles", "Keep setups organized", Modifier.weight(1f), onInstances)
            QuickFeatureCard("MODULES", "Client features", "Preview module library", Modifier.weight(1f), onModules)
        }
        SectionLabel("YOUR WORKSPACE")
        Surface(color = Panel, shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                WorkspaceRow("Worlds & saves", "Organize worlds and backups", "W")
                HorizontalDivider(color = Line)
                WorkspaceRow("Resource packs", "Manage visual customizations", "P")
                HorizontalDivider(color = Line)
                WorkspaceRow("Add-on library", "Discover Bedrock content", "A")
            }
        }
        Surface(color = Color(0xFF111B19), shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF28493B)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("RABBIT CLIENT · IN DEVELOPMENT", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(6.dp))
                Text("Instance isolation and native gameplay modules require a compatible runtime integration. Current Play opens the installed game.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun QuickFeatureCard(kicker: String, title: String, detail: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Panel, shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(kicker, color = AccentDeep, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp)
            Spacer(Modifier.height(13.dp))
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(detail, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun WorkspaceRow(title: String, detail: String, glyph: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = PanelRaised, shape = RoundedCornerShape(12.dp)) { Text(glyph, color = Accent, modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp), fontWeight = FontWeight.ExtraBold) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(detail, color = TextMuted, fontSize = 11.sp)
        }
        Text("›", color = TextMuted, fontSize = 24.sp)
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
    Surface(color = Panel, shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(22.dp)) {
            Text("No instances yet", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Instance detection, import/export, backups and launch actions are still being built.",
                color = TextMuted, lineHeight = 20.sp)
            Spacer(Modifier.height(16.dp))
            Text("PLANNED", color = Accent, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            listOf("Installed game detection", "Instance profiles", "Safe backup and restore", "Launch compatibility checks").forEach {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 5.dp)) {
                    Box(Modifier.size(6.dp).background(AccentDeep, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text(it, color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    val context = LocalContext.current
    var wallpaperUri by remember { mutableStateOf(context.getSharedPreferences("rabbit_ui_preferences", Context.MODE_PRIVATE).getString("wallpaper_uri", null)) }
    val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
            wallpaperUri = uri.toString()
            context.getSharedPreferences("rabbit_ui_preferences", Context.MODE_PRIVATE).edit().putString("wallpaper_uri", wallpaperUri).apply()
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
            if (wallpaperUri != null) { Spacer(Modifier.height(8.dp)); OutlinedButton(onClick = { wallpaperUri = null; context.getSharedPreferences("rabbit_ui_preferences", Context.MODE_PRIVATE).edit().remove("wallpaper_uri").apply() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Reset wallpaper", color = Color.White) } }
            Spacer(Modifier.height(20.dp))
            Text("CurseForge", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Discover Minecraft Bedrock add-ons. UI credit: Chat GPT implementer.", color = TextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.curseforge.com/minecraft-bedrock"))) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Language, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Browse CurseForge", color = Color.White) }
            Text("No .levipack support. CurseForge currently opens in your browser.", color = TextMuted, fontSize = 11.sp, lineHeight = 16.sp)
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
