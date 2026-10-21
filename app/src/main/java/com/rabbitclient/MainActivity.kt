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
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import android.webkit.WebChromeClient
import android.webkit.CookieManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgState = mutableStateOf(Color(0xFF080B10))
private val AccentState = mutableStateOf(Color(0xFFB7F7D5))
private val AccentDeepState = mutableStateOf(Color(0xFF62D9A0))
private val Bg: Color get() = BgState.value
private val Panel = Color(0x6610141A)
private val PanelRaised = Color(0x80141A22)
private val Accent: Color get() = AccentState.value
private val AccentDeep: Color get() = AccentDeepState.value
private val TextMuted = Color(0xFF91A0B2)
private val Line = Color(0xFF263341)

private const val RABBIT_REPOSITORY_URL = "https://github.com/khargosh-area/rabbitclient"

private data class AccentPreset(val name: String, val primary: Color, val deep: Color)

private val RABBIT_ACCENT_PRESETS = listOf(
    AccentPreset("Mint", Color(0xFFB7F7D5), Color(0xFF62D9A0)),
    AccentPreset("Blue", Color(0xFF8EB8FF), Color(0xFF4C82E8)),
    AccentPreset("Violet", Color(0xFFD1B3FF), Color(0xFF9B70E8)),
    AccentPreset("Rose", Color(0xFFFFAFC8), Color(0xFFE86C96)),
    AccentPreset("Amber", Color(0xFFFFD08A), Color(0xFFE7A33F)),
    AccentPreset("Cyan", Color(0xFF8DEBFF), Color(0xFF36B7D8)),
    AccentPreset("Red", Color(0xFFFF9B91), Color(0xFFDF5B52)),
    AccentPreset("White", Color(0xFFF0F3F8), Color(0xFFB7C0CE))
)

private fun applyRabbitTheme(name: String) {
    val accent = RABBIT_ACCENT_PRESETS.firstOrNull { it.name == name } ?: RABBIT_ACCENT_PRESETS.first()
    BgState.value = Color(0xFF080B10)
    AccentState.value = accent.primary
    AccentDeepState.value = accent.deep
}

private fun curseForgeUrl(query: String, category: String): String {
    val params = mutableListOf<String>()
    if (query.isNotBlank()) params += "search=" + Uri.encode(query.trim())
    val className = when (category) {
        "Addons" -> "addons"
        "Maps" -> "maps"
        "Texture Packs" -> "texture-packs"
        "Scripts" -> "scripts"
        "Skins" -> "skins"
        else -> null
    }
    if (className != null) params += "class=" + className
    params += "page=1"
    params += "pageSize=20"
    params += "sortBy=relevancy"
    return "https://www.curseforge.com/minecraft-bedrock/search?" + params.joinToString("&")
}

private data class DockDestination(val label: String, val page: String, val icon: ImageVector)

private val dockDestinations = listOf(
    DockDestination("Launch", "Home", Icons.Default.Home),
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

@Composable
private fun RabbitApp() {
    var page by remember { mutableStateOf("Home") }
    val context = LocalContext.current
    val preferences = remember(context) { context.getSharedPreferences("rabbit_ui_preferences", Context.MODE_PRIVATE) }
    var installedGames by remember(context) { mutableStateOf(detectInstalledGames(context)) }
    var selectedGamePackage by remember(context) {
        val saved = preferences.getString("selected_game_package", null)
        mutableStateOf<String?>(saved?.takeIf { pkg -> installedGames.any { it.packageName == pkg } } ?: installedGames.firstOrNull()?.packageName)
    }
    LaunchedEffect(selectedGamePackage) {
        if (selectedGamePackage == null) preferences.edit().remove("selected_game_package").apply()
        else preferences.edit().putString("selected_game_package", selectedGamePackage).apply()
    }
    LaunchedEffect(installedGames) {
        if (installedGames.none { it.packageName == selectedGamePackage }) {
            selectedGamePackage = installedGames.firstOrNull()?.packageName
        }
    }
    var selectedTheme by remember(context) { mutableStateOf((preferences.getString("theme_name", "Mint") ?: "Mint").let { saved -> if (RABBIT_ACCENT_PRESETS.any { it.name == saved }) saved else "Mint" }) }
    LaunchedEffect(selectedTheme) {
        preferences.edit().putString("theme_name", selectedTheme).apply()
        applyRabbitTheme(selectedTheme)
    }
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
                Box(Modifier.fillMaxSize().background(Color(0x18060A10)))
            }
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.wrapContentWidth().padding(horizontal = 10.dp),
                        shape = RoundedCornerShape(34.dp),
                        color = Color(0xD9080B10),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                        shadowElevation = 16.dp
                    ) {
                        Row(
                            Modifier.wrapContentWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
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
            }
        ) { insets ->
            Column(
                Modifier.fillMaxSize()
                    .padding(insets)
                    .then(
                        if (page == "CurseForge") Modifier
                        else Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
                    )
            ) {
                if (page != "Home" && page != "CurseForge") {
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
                } else if (page == "Home") {
                    Spacer(Modifier.height(8.dp))
                }

                when (page) {
                    "Home" -> HomeScreen(
                        installedGames = installedGames,
                        selectedGamePackage = selectedGamePackage,
                        wallpaperSelected = wallpaperUri != null,
                        onWallpaperClick = { homeWallpaperPicker.launch(arrayOf("image/*")) },
                        onInstances = { page = "Instances" },
                        onCurseForge = { page = "CurseForge" },
                        onLaunch = {
                            val selected = installedGames.firstOrNull { it.packageName == selectedGamePackage }
                            val intent = selected?.let { context.packageManager.getLaunchIntentForPackage(it.packageName) }
                            if (intent != null) context.startActivity(intent) else page = "Instances"
                        }
                    )
                    "Instances" -> InstancesScreen(
                        installedGames = installedGames,
                        selectedGamePackage = selectedGamePackage,
                        onSelect = { selectedGamePackage = it },
                        onLaunch = { game ->
                            context.packageManager.getLaunchIntentForPackage(game.packageName)?.let { context.startActivity(it) }
                        },
                        onRefresh = { installedGames = detectInstalledGames(context) }
                    )
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
                        installedGames = installedGames,
                        selectedGamePackage = selectedGamePackage,
                        wallpaperSelected = wallpaperUri != null,
                        onWallpaperClick = { homeWallpaperPicker.launch(arrayOf("image/*")) },
                        onInstances = { page = "Instances" },
                        onCurseForge = { page = "CurseForge" },
                        onLaunch = {
                            val selected = installedGames.firstOrNull { it.packageName == selectedGamePackage }
                            val intent = selected?.let { context.packageManager.getLaunchIntentForPackage(it.packageName) }
                            if (intent != null) context.startActivity(intent) else page = "Instances"
                        }
                    )
                }
                if (page != "CurseForge") Spacer(Modifier.height(24.dp))
            }
        }
        }
    }
}

@Composable
private fun DockItem(destination: DockDestination, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(52.dp).clickable(onClick = onClick).padding(horizontal = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier.size(if (selected) 44.dp else 42.dp)
                .background(
                    if (selected) Accent else Color(0xFF171D27),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                tint = if (selected) Bg else Color(0xFFB4C0CF),
                modifier = Modifier.size(if (selected) 21.dp else 19.dp)
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

private data class InstalledGame(
    val packageName: String,
    val label: String,
    val versionName: String,
    val channel: String
)

private fun detectInstalledGames(context: Context): List<InstalledGame> {
    val candidates = listOf(
        Triple("com.mojang.minecraftpe", "Minecraft Bedrock", "Stable"),
        Triple("com.mojang.minecraftpe.preview", "Minecraft Preview", "Preview"),
        Triple("com.mojang.minecraftpe.beta", "Minecraft Beta", "Beta")
    )
    return candidates.mapNotNull { candidate ->
        try {
            val info = context.packageManager.getPackageInfo(candidate.first, 0)
            val label = context.packageManager.getApplicationLabel(info.applicationInfo).toString()
            InstalledGame(candidate.first, label.ifBlank { candidate.second }, info.versionName ?: "Unknown", candidate.third)
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
private fun HomeScreen(
    installedGames: List<InstalledGame>,
    selectedGamePackage: String?,
    wallpaperSelected: Boolean,
    onWallpaperClick: () -> Unit,
    onInstances: () -> Unit,
    onCurseForge: () -> Unit,
    onLaunch: () -> Unit
) {
    val selectedGame = installedGames.firstOrNull { it.packageName == selectedGamePackage }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = Panel,
            shape = RoundedCornerShape(26.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("MINECRAFT  ·  BEDROCK", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp)
                        Text("Ready for your\nnext session", color = Color.White, fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold)
                        Text("A clean home for your Minecraft setup.", color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                    Surface(color = PanelRaised, shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line)) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("▦", fontSize = 31.sp, color = Accent)
                            Text("BEDROCK", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = TextMuted, letterSpacing = 0.8.sp)
                        }
                    }
                }
                HorizontalDivider(color = Line.copy(alpha = 0.55f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("CURRENT INSTANCE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                        Text(selectedGame?.label ?: "No install detected", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (selectedGame != null) selectedGame.channel + "  ·  " + selectedGame.versionName
                            else "Install Minecraft, then refresh Instances.",
                            color = TextMuted, fontSize = 11.sp
                        )
                    }
                    TextButton(onClick = onInstances) { Text("Change", color = Accent, fontWeight = FontWeight.Bold) }
                }
                Button(
                    onClick = onLaunch,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)
                ) {
                    Text(if (selectedGame != null) "▶   Launch Minecraft" else "Set up an instance", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = AccentDeep, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(if (wallpaperSelected) "Custom wallpaper enabled" else "Make it yours with a custom wallpaper", color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = onWallpaperClick, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                        Text(if (wallpaperSelected) "Change" else "Customize", color = Accent, fontSize = 11.sp)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Surface(
                color = Panel, shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                modifier = Modifier.weight(1f).clickable(onClick = onInstances)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(42.dp).background(PanelRaised, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = Accent, modifier = Modifier.size(21.dp))
                    }
                    Text("Instances", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Choose an installed Bedrock edition.", color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
                    Text(installedGames.size.toString() + " detected", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Surface(
                color = Panel, shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                modifier = Modifier.weight(1f).clickable(onClick = onCurseForge)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(42.dp).background(PanelRaised, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Accent, modifier = Modifier.size(21.dp))
                    }
                    Text("CurseForge", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Explore community add-ons and content.", color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
                    Text("Browse projects  →", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Surface(
            color = Color(0x4410141A), shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line.copy(alpha = 0.65f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("RABBIT CLIENT", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
                    Text("By KHARGOSH & CHATGPT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("v0.1.0", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InstancesScreen(
    installedGames: List<InstalledGame>,
    selectedGamePackage: String?,
    onSelect: (String) -> Unit,
    onLaunch: (InstalledGame) -> Unit,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Instances", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("Choose which installed Minecraft Bedrock edition Rabbit Client launches.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("INSTALLED GAME VERSIONS", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp)
                Text(installedGames.size.toString() + " installation(s) detected", color = TextMuted, fontSize = 11.sp)
            }
            OutlinedButton(onClick = onRefresh, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 13.dp, vertical = 9.dp)) {
                Text("↻  Refresh", color = Color.White, fontSize = 11.sp)
            }
        }

        if (installedGames.isEmpty()) {
            Surface(color = Panel, shape = RoundedCornerShape(24.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(58.dp).background(PanelRaised, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = Accent, modifier = Modifier.size(28.dp))
                    }
                    Text("No game instance found", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Install the official Minecraft Bedrock app or an official Preview edition first, then tap Refresh.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        } else {
            installedGames.forEach { game ->
                val isSelected = game.packageName == selectedGamePackage
                Surface(
                    color = if (isSelected) Color(0x6640514C) else Panel,
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) Accent else Line),
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(game.packageName) }
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = PanelRaised, shape = RoundedCornerShape(14.dp)) {
                                Text(
                                    when (game.channel) { "Preview" -> "P"; "Beta" -> "β"; else -> "MC" },
                                    color = Accent, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(game.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(game.channel + "  ·  Version " + game.versionName, color = TextMuted, fontSize = 11.sp)
                            }
                            if (isSelected) {
                                Surface(color = Accent, shape = RoundedCornerShape(20.dp)) {
                                    Text("SELECTED", color = Bg, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.7.sp)
                                }
                            } else {
                                OutlinedButton(onClick = { onSelect(game.packageName) }, contentPadding = PaddingValues(horizontal = 11.dp, vertical = 7.dp), shape = RoundedCornerShape(11.dp)) {
                                    Text("Select", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                        if (isSelected) {
                            Button(
                                onClick = { onLaunch(game) },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(13.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)
                            ) { Text("Launch this instance", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }

        Surface(
            color = Color(0x4410141A), shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line.copy(alpha = 0.65f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("ABOUT ANDROID INSTANCES", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
                Text("Rabbit Client can select among separate Minecraft editions already installed on Android, such as Stable and Preview.", color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
                Text("Flarial’s desktop launcher can manage separate game versions with its own runtime. That runtime cannot be reused directly on Android, so arbitrary Bedrock version downloads into isolated Android instances are not enabled in this build.", color = TextMuted, fontSize = 11.sp, lineHeight = 17.sp)
            }
        }
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
    SectionLabel("ACCENT COLOUR")
    Spacer(Modifier.height(10.dp))
    Surface(color = Panel, shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Pick your highlight colour", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("The dark Obsidian glass theme stays the same. Only highlights and active controls change.", color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
            RABBIT_ACCENT_PRESETS.chunked(4).forEach { rowPresets ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowPresets.forEach { preset ->
                        Column(
                            modifier = Modifier.weight(1f).clickable { onThemeChange(preset.name) }.padding(vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Box(
                                Modifier.size(42.dp)
                                    .background(preset.primary, CircleShape)
                                    .then(if (selectedTheme == preset.name) Modifier.border(2.dp, Color.White, CircleShape) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedTheme == preset.name) Text("✓", color = Bg, fontSize = 17.sp, fontWeight = FontWeight.Black)
                            }
                            Text(preset.name, color = if (selectedTheme == preset.name) preset.primary else TextMuted, fontSize = 10.sp, fontWeight = if (selectedTheme == preset.name) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    repeat(4 - rowPresets.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
    Spacer(Modifier.height(20.dp))
    SectionLabel("PERSONALIZATION")
    Spacer(Modifier.height(10.dp))
    Surface(color = Panel, shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Text("Live colour preview", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Your selected accent is applied across the launcher.", color = TextMuted, fontSize = 12.sp)
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
            Spacer(Modifier.height(20.dp))
            Text("Compatibility", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text("Target: Bedrock 1.21.90 and newer. Future releases require runtime checks and testing; launcher support and in-game module support are separate.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(18.dp))
            Text("In-game integration", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text("In-game modules are intentionally not shown in the launcher. They will be added only after a working Android game integration is verified.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
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
                Text("The current app detects installed Minecraft editions and launches the selected package. A standalone isolated runtime and direct in-game integration are still in development and need device testing.", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun CurseForgeScreen(onBack: () -> Unit) {
    val categories = listOf("All", "Addons", "Maps", "Texture Packs", "Scripts", "Skins")
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var pageUrl by remember { mutableStateOf(curseForgeUrl("", "All")) }
    var browser by remember { mutableStateOf<WebView?>(null) }
    LaunchedEffect(pageUrl) {
        browser?.loadUrl(pageUrl)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            color = Panel,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("CurseForge", color = Accent, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Minecraft Bedrock · Community projects", color = TextMuted, fontSize = 11.sp)
                    }
                    TextButton(onClick = {
                        if (browser?.canGoBack() == true) browser?.goBack() else onBack()
                    }) { Text("‹  Back", color = Accent, fontSize = 12.sp) }
                    TextButton(onClick = { browser?.reload() }) { Text("↻", color = Color.White, fontSize = 18.sp) }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Search Bedrock content…", fontSize = 12.sp) },
                        shape = RoundedCornerShape(13.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = Line,
                            cursorColor = Accent
                        )
                    )
                    Button(
                        onClick = { pageUrl = curseForgeUrl(query, selectedCategory) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)
                    ) { Text("Search", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                ) {
                    categories.forEach { item ->
                        val selected = selectedCategory == item
                        Surface(
                            color = if (selected) Accent else Color(0x5510141A),
                            shape = RoundedCornerShape(22.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Accent else Line),
                            modifier = Modifier.clickable {
                                selectedCategory = item
                                pageUrl = curseForgeUrl(query, item)
                            }
                        ) {
                            Text(item, color = if (selected) Bg else TextMuted, fontSize = 11.sp, maxLines = 1, modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp))
                        }
                    }
                }
            }
        }

        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { viewContext ->
                CookieManager.getInstance().setAcceptCookie(true)
                WebView(viewContext).apply {
                    browser = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    webChromeClient = WebChromeClient()
                    webViewClient = WebViewClient()
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    loadUrl(pageUrl)
                }
            },
            update = { /* URL changes are handled by LaunchedEffect above. */ }
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
