package com.rabbitclient

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.graphics.BitmapFactory
import android.graphics.Bitmap
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgState = mutableStateOf(Color(0xFF080B10))
private val AccentState = mutableStateOf(Color(0xFFB7F7D5))
private val AccentDeepState = mutableStateOf(Color(0xFF62D9A0))
private val Bg: Color get() = BgState.value
private val Panel = Color(0xB8171D26)
private val PanelRaised = Color(0xD6222B36)
private val Accent: Color get() = AccentState.value
private val AccentDeep: Color get() = AccentDeepState.value
private val TextMuted = Color(0xFF91A0B2)
private val Line = Color(0x80506070)

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

// Decode a bounded thumbnail away from Compose's UI thread so high-resolution wallpapers
// do not cause avoidable memory pressure on tablets.
private fun loadWallpaperThumbnail(context: Context, uriString: String?): ImageBitmap? {
    if (uriString.isNullOrBlank()) return null
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(Uri.parse(uriString))?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val sample = maxOf(1, maxOf(bounds.outWidth / 1600, bounds.outHeight / 1600))
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        context.contentResolver.openInputStream(Uri.parse(uriString))?.use {
            BitmapFactory.decodeStream(it, null, options)?.asImageBitmap()
        }
    } catch (_: Exception) {
        null
    }
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
        // Match Android system splash to Rabbit Client's Obsidian palette.
        window.statusBarColor = android.graphics.Color.rgb(8, 11, 16)
        window.navigationBarColor = android.graphics.Color.rgb(8, 11, 16)
        super.onCreate(savedInstanceState)
        setContent { RabbitApp() }
    }
}

@Composable
private fun RabbitSplashCard() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            color = Color(0xFF080B10),
            shape = CircleShape,
            border = androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = 0.35f)),
            modifier = Modifier.size(118.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.rabbit_logo),
                    contentDescription = "Rabbit Client rabbit logo",
                    modifier = Modifier.size(92.dp)
                )
            }
        }
        Text("RABBIT CLIENT", color = Color.White, fontSize = 21.sp,
            fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
        Text("MINECRAFT BEDROCK", color = Accent, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 2.5.sp)
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            modifier = Modifier.width(148.dp).height(3.dp),
            color = Accent,
            trackColor = Color(0xFF202635)
        )
        Text("Preparing your launcher", color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun RabbitApp() {
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        showSplash = false
    }
    if (showSplash) {
        Box(
            Modifier.fillMaxSize().background(Color(0xFF080B10)),
            contentAlignment = Alignment.Center
        ) { RabbitSplashCard() }
        return
    }
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
    var fullWallpaper by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(wallpaperUri) {
        fullWallpaper = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            loadWallpaperThumbnail(context, wallpaperUri)
        }
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
            val wallpaperBitmap = fullWallpaper
            if (wallpaperBitmap != null) {
                // One proportional crop fills the app viewport without stretching or letterboxing.
                Image(
                    bitmap = wallpaperBitmap,
                    contentDescription = "Rabbit Client background wallpaper",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Keep the picture visible; panels provide most contrast instead of a page-wide dark veil.
                Box(Modifier.fillMaxSize().background(Color(0x1A060A10)))
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
                        color = Color(0xB8080B10),
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
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PanelRaised,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = 0.42f)),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.rabbit_logo),
                                    contentDescription = "Rabbit Client rabbit logo",
                                    modifier = Modifier.size(43.dp)
                                )
                            }
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
                    "CurseForge" -> CurseForgeScreen(onBack = { page = "Home" }, minecraftPackage = selectedGamePackage)
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
            val label = info.applicationInfo?.let {
                context.packageManager.getApplicationLabel(it).toString()
            }.orEmpty()
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
                            Image(
                                painter = painterResource(id = R.drawable.rabbit_logo),
                                contentDescription = "Rabbit Client mascot",
                                modifier = Modifier.size(42.dp)
                            )
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
                    Surface(
                        color = PanelRaised,
                        shape = RoundedCornerShape(15.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = 0.42f))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.rabbit_logo),
                            contentDescription = "Rabbit Client rabbit logo",
                            modifier = Modifier.size(58.dp).padding(5.dp)
                        )
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

private data class CurseForgeProject(
    val id: Int,
    val name: String,
    val slug: String,
    val author: String,
    val summary: String,
    val downloads: Long,
    val imageUrl: String,
    val projectUrl: String,
    val fileName: String,
    val downloadUrl: String?
)

private data class CurseForgeDownload(val url: String?, val fileName: String)

private fun normalizeCurseForgeBackend(rawUrl: String): String {
    val normalized = rawUrl.trim().trimEnd('/')
    if (normalized.isBlank()) throw IllegalArgumentException("Enter your deployed CurseForge API service URL.")
    val parsed = Uri.parse(normalized)
    val secure = parsed.scheme.equals("https", ignoreCase = true)
    val localDevelopment = parsed.scheme.equals("http", ignoreCase = true) &&
        parsed.host in setOf("localhost", "127.0.0.1", "10.0.2.2")
    if (parsed.host.isNullOrBlank() || (!secure && !localDevelopment)) {
        throw IllegalArgumentException("Use an HTTPS backend URL. HTTP is allowed only for localhost development.")
    }
    return normalized
}

private fun requestCurseForgeBackendJson(baseUrl: String, route: String): org.json.JSONObject {
    val connection = (java.net.URL(baseUrl + route).openConnection() as java.net.HttpURLConnection)
    return try {
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 20000
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "RabbitClient-Android/0.2")
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        if (status !in 200..299) {
            val serverMessage = runCatching { org.json.JSONObject(body).optString("error") }.getOrNull()
            val message = serverMessage?.takeIf { it.isNotBlank() }
                ?: when (status) {
                    401, 403 -> "The API service rejected its CurseForge key. Check CURSEFORGE_API_KEY on the server."
                    429 -> "The API service is rate-limited. Please wait and retry."
                    else -> "CurseForge service returned HTTP " + status + "."
                }
            throw IllegalStateException(message)
        }
        org.json.JSONObject(body)
    } finally {
        connection.disconnect()
    }
}

private fun fetchCurseForgeProjects(
    backendUrl: String,
    query: String,
    category: String,
    sort: String
): List<CurseForgeProject> {
    val baseUrl = normalizeCurseForgeBackend(backendUrl)
    val endpoint = Uri.parse(baseUrl + "/api/curseforge/search").buildUpon()
        .appendQueryParameter("query", query.trim())
        .appendQueryParameter("category", category)
        .appendQueryParameter("sort", sort)
        .build()
        .toString()
    val root = requestCurseForgeBackendJson(baseUrl, endpoint.removePrefix(baseUrl))
    val projects = root.optJSONArray("projects")
        ?: throw IllegalStateException("The API service returned an invalid project list.")
    val result = ArrayList<CurseForgeProject>(projects.length())
    for (index in 0 until projects.length()) {
        val item = projects.optJSONObject(index) ?: continue
        val id = item.optInt("id", 0)
        val name = item.optString("name").trim()
        if (id <= 0 || name.isBlank()) continue
        val slug = item.optString("slug").ifBlank { id.toString() }
        val projectUrl = item.optString("projectUrl")
            .takeIf { it.startsWith("https://www.curseforge.com/") }
            ?: "https://www.curseforge.com/minecraft-bedrock/addons/" + slug
        result.add(
            CurseForgeProject(
                id = id,
                name = name,
                slug = slug,
                author = item.optString("author").ifBlank { "CurseForge creator" },
                summary = item.optString("summary").ifBlank { "Open the project to view its details." }.take(500),
                downloads = item.optLong("downloads", 0L).coerceAtLeast(0L),
                imageUrl = item.optString("imageUrl"),
                projectUrl = projectUrl,
                fileName = item.optString("fileName"),
                downloadUrl = item.optString("downloadUrl").takeIf { it.startsWith("https://") }
            )
        )
    }
    return result
}
private fun fetchCurseForgeDownload(backendUrl: String, projectId: Int): CurseForgeDownload {
    if (projectId <= 0) throw IllegalArgumentException("Invalid CurseForge project ID.")
    val baseUrl = normalizeCurseForgeBackend(backendUrl)
    val root = requestCurseForgeBackendJson(baseUrl, "/api/curseforge/download/" + projectId)
    val url = root.optString("url").takeIf { it.startsWith("https://") }
        ?: throw IllegalStateException("The creator has not provided a direct download URL for the latest file.")
    val fileName = root.optString("fileName").ifBlank { "rabbitclient-pack.mcpack" }
    return CurseForgeDownload(url, fileName)
}

private fun downloadCurseForgePack(context: Context, downloadUrl: String, requestedName: String): java.io.File {
    val downloadUri = Uri.parse(downloadUrl)
    if (!downloadUri.scheme.equals("https", ignoreCase = true) || downloadUri.host.isNullOrBlank()) {
        throw IllegalArgumentException("Only secure HTTPS pack downloads are allowed.")
    }
    val connection = (java.net.URL(downloadUrl).openConnection() as java.net.HttpURLConnection)
    val maxBytes = 256L * 1024L * 1024L
    var destination: java.io.File? = null
    try {
        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("User-Agent", "RabbitClient-Android/0.2")
        val status = connection.responseCode
        if (status !in 200..299) throw IllegalStateException("Download failed with HTTP " + status + ".")
        if (!connection.url.protocol.equals("https", ignoreCase = true)) {
            throw IllegalStateException("The download redirected to a non-HTTPS URL.")
        }
        if (connection.contentLengthLong > maxBytes) {
            throw IllegalStateException("This pack is larger than Rabbit Client's 256 MB import limit.")
        }
        var safeName = requestedName.substringAfterLast('/').substringBefore('?')
            .replace(Regex("[^A-Za-z0-9._-]"), "_").take(120)
        if (safeName.isBlank() || safeName == "." || safeName == "..") safeName = "rabbitclient-pack.mcpack"
        val extension = safeName.substringAfterLast('.', "").lowercase(java.util.Locale.ROOT)
        if (extension !in setOf("mcpack", "mcaddon", "mcworld", "zip")) {
            throw IllegalStateException("Unsupported file type ." + extension + ". Only .mcpack, .mcaddon, .mcworld, and .zip files are accepted.")
        }
        val folder = java.io.File(context.cacheDir, "rabbit-imports")
        if (!folder.exists() && !folder.mkdirs()) throw IllegalStateException("Could not create the pack import cache.")
        destination = java.io.File(folder, java.lang.System.currentTimeMillis().toString() + "_" + safeName)
        var copied = 0L
        connection.inputStream.use { input ->
            java.io.FileOutputStream(destination).use { output ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    copied += count
                    if (copied > maxBytes) throw IllegalStateException("This pack is larger than Rabbit Client's 256 MB import limit.")
                    output.write(buffer, 0, count)
                }
                output.flush()
            }
        }
        if (copied == 0L) throw IllegalStateException("CurseForge returned an empty download.")
        return destination
    } catch (error: Exception) {
        destination?.delete()
        throw error
    } finally {
        connection.disconnect()
    }
}

private fun packMimeType(file: java.io.File): String = when (file.extension.lowercase(java.util.Locale.ROOT)) {
    "mcpack" -> "application/x-mcpack"
    "mcaddon" -> "application/x-mcaddon"
    "mcworld" -> "application/x-mcworld"
    else -> "application/zip"
}

@Composable
private fun CurseForgeScreen(onBack: () -> Unit, minecraftPackage: String?) {
    val context = LocalContext.current
    val preferences = remember(context) { context.getSharedPreferences("rabbit_ui_preferences", Context.MODE_PRIVATE) }
    var backendUrl by remember(context) { mutableStateOf(preferences.getString("curseforge_backend_url", "") ?: "") }
    var submittedBackendUrl by remember(context) { mutableStateOf(backendUrl.trim().trimEnd('/')) }
    val scope = rememberCoroutineScope()
    var installingDownload by remember { mutableStateOf(false) }
    val categories = listOf("Add-ons", "Maps", "Texture Packs", "Scripts", "Skins")
    var category by remember { mutableStateOf("Add-ons") }
    var query by remember { mutableStateOf("") }
    var submittedQuery by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf("Popular") }
    var refreshToken by remember { mutableIntStateOf(0) }
    var projects by remember { mutableStateOf<List<CurseForgeProject>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedProject by remember { mutableStateOf<CurseForgeProject?>(null) }
    var selectedDownload by remember { mutableStateOf<CurseForgeDownload?>(null) }
    var loadingDownload by remember { mutableStateOf(false) }
    var downloadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(submittedBackendUrl, category, submittedQuery, sort, refreshToken) {
        loading = true
        error = null
        if (submittedBackendUrl.isBlank()) {
            projects = emptyList()
            error = "Connect an HTTPS backend URL below. The official CurseForge API key must stay on the server; see backend/README.md in the GitHub repository."
            loading = false
        } else {
            val result = runCatching {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    fetchCurseForgeProjects(submittedBackendUrl, submittedQuery, category, sort)
                }
            }
            result.onSuccess { projects = it }
            result.onFailure { failure ->
                projects = emptyList()
                error = when {
                    failure is java.net.UnknownHostException -> "Cannot reach the API service. Check the URL and your internet connection."
                    failure.message?.contains("CURSEFORGE_API_KEY", ignoreCase = true) == true ->
                        "The server has no valid CurseForge API key. Set CURSEFORGE_API_KEY in the backend host's secrets."
                    else -> failure.message ?: "Could not load the CurseForge catalogue."
                }
            }
            loading = false
        }
    }

    LaunchedEffect(selectedProject?.id, submittedBackendUrl) {
        val project = selectedProject ?: return@LaunchedEffect
        selectedDownload = null
        downloadError = null
        loadingDownload = true
        if (submittedBackendUrl.isBlank()) {
            downloadError = "Connect the official API service first."
            loadingDownload = false
        } else {
            val result = runCatching {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    project.downloadUrl?.let { CurseForgeDownload(it, project.fileName) }
                        ?: fetchCurseForgeDownload(submittedBackendUrl, project.id)
                }
            }
            result.onSuccess { selectedDownload = it }
            result.onFailure { downloadError = it.message ?: "Could not resolve a direct download for this project." }
            loadingDownload = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Accent.copy(alpha = 0.15f), shape = RoundedCornerShape(13.dp),
                modifier = Modifier.size(42.dp).clickable(onClick = onBack)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("←", color = Accent, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("CURSEFORGE", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                Text("Bedrock content", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
            Surface(color = PanelRaised, shape = RoundedCornerShape(12.dp)) {
                Text("NATIVE", color = Accent, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
            }
        }

        Surface(color = Panel, shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Discover community packs", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Native catalogue powered by the official CurseForge API.", color = TextMuted, fontSize = 11.sp)
                OutlinedTextField(
                    value = backendUrl,
                    onValueChange = { backendUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("HTTPS API service URL", fontSize = 11.sp) },
                    placeholder = { Text("https://your-service.onrender.com", fontSize = 11.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent, unfocusedBorderColor = Line,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Accent
                    )
                )
                Button(
                    onClick = {
                        val validated = runCatching { normalizeCurseForgeBackend(backendUrl) }
                        if (validated.isFailure) {
                            error = validated.exceptionOrNull()?.message ?: "Enter a valid HTTPS API service URL."
                        } else {
                            submittedBackendUrl = validated.getOrThrow()
                            backendUrl = submittedBackendUrl
                            preferences.edit().putString("curseforge_backend_url", submittedBackendUrl).apply()
                            refreshToken++
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)
                ) { Text("Connect API service", fontWeight = FontWeight.Bold) }
                Text("Deploy the repository's backend/ folder and set CURSEFORGE_API_KEY on that server. Never paste a private key here.",
                    color = TextMuted, fontSize = 10.sp, lineHeight = 15.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query, onValueChange = { query = it }, modifier = Modifier.weight(1f),
                        singleLine = true, placeholder = { Text("Search add-ons…", color = TextMuted, fontSize = 13.sp) },
                        shape = RoundedCornerShape(13.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent, unfocusedBorderColor = Line,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Accent
                        )
                    )
                    Button(onClick = { submittedQuery = query }, shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg),
                        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 14.dp)) {
                        Text("Search", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            categories.forEach { item ->
                val selected = category == item
                Surface(color = if (selected) Accent else Panel, shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Accent else Line),
                    modifier = Modifier.clickable { category = item }) {
                    Text(item, color = if (selected) Bg else TextMuted, fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp))
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (submittedQuery.isBlank()) "Explore $category" else "Results for “$submittedQuery”",
                    color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(if (sort == "Popular") "Popular projects" else "Recently updated", color = TextMuted, fontSize = 11.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf("Popular", "Latest").forEach { option ->
                    val active = sort == option
                    Surface(color = if (active) PanelRaised else Color.Transparent, shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Accent.copy(alpha = 0.6f) else Line),
                        modifier = Modifier.clickable { sort = option }) {
                        Text(option, color = if (active) Accent else TextMuted, fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp))
                    }
                }
                Surface(color = Panel, shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                    modifier = Modifier.clickable { refreshToken++ }) {
                    Text("↻", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }

        when {
            loading -> Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                    Text("Loading CurseForge projects…", color = TextMuted, fontSize = 12.sp)
                }
            }
            error != null -> Surface(color = Panel, shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Couldn’t load the catalogue", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(error ?: "", color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
                    OutlinedButton(onClick = { refreshToken++ }, shape = RoundedCornerShape(12.dp)) { Text("Retry", color = Accent) }
                }
            }
            projects.isEmpty() -> Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(21.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("No projects found", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Try another search or choose a different category.", color = TextMuted, fontSize = 12.sp)
                }
            }
            else -> projects.forEach { project ->
                Surface(color = Panel, shape = RoundedCornerShape(19.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Line.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth().clickable { selectedProject = project }) {
                    Row(Modifier.padding(13.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(color = Accent.copy(alpha = 0.12f), shape = RoundedCornerShape(14.dp), modifier = Modifier.size(52.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(project.name.take(1).uppercase(), color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(project.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                            Text("by ${project.author}", color = Accent, fontSize = 10.sp, maxLines = 1)
                            Text(project.summary, color = TextMuted, fontSize = 11.sp, lineHeight = 16.sp, maxLines = 3)
                            Text("${formatDownloadCount(project.downloads)} downloads · Tap for details", color = TextMuted, fontSize = 9.sp)
                        }
                        Text("›", color = Accent, fontSize = 27.sp, modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }
            }
        }

        Text("Content is supplied by CurseForge. Downloads are saved to your Downloads folder and may need to be imported into Minecraft manually.",
            color = TextMuted, fontSize = 10.sp, lineHeight = 15.sp, modifier = Modifier.padding(bottom = 8.dp))
    }

    val project = selectedProject
    if (project != null) {
        AlertDialog(
            onDismissRequest = { selectedProject = null },
            containerColor = Color(0xFF111720),
            title = { Text(project.name, color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("by ${project.author}", color = Accent, fontSize = 12.sp)
                    Text(project.summary, color = TextMuted, fontSize = 13.sp, lineHeight = 19.sp)
                    Text("${formatDownloadCount(project.downloads)} downloads", color = TextMuted, fontSize = 11.sp)
                    when {
                        loadingDownload -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            CircularProgressIndicator(color = Accent, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text("Resolving latest official download…", color = TextMuted, fontSize = 11.sp)
                        }
                        downloadError != null -> Text(downloadError ?: "", color = Color(0xFFFFB4A9), fontSize = 11.sp)
                        selectedDownload?.url == null -> Text("No direct download is available for this project.", color = TextMuted, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = !loadingDownload && !installingDownload && selectedDownload?.url != null, onClick = {
                    val download = selectedDownload ?: return@TextButton
                    val directUrl = download.url ?: return@TextButton
                    scope.launch {
                        installingDownload = true
                        downloadError = null
                        try {
                            val file = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                downloadCurseForgePack(context, directUrl, download.fileName)
                            }
                            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                                context, context.packageName + ".files", file
                            )
                            val mime = packMimeType(file)
                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(contentUri, mime)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                if (minecraftPackage != null) {
                                    context.startActivity(Intent(viewIntent).setPackage(minecraftPackage))
                                } else {
                                    context.startActivity(Intent.createChooser(viewIntent, "Import into Minecraft"))
                                }
                            } catch (_: Exception) {
                                context.startActivity(Intent.createChooser(viewIntent, "Open downloaded pack"))
                            }
                            android.widget.Toast.makeText(context, "Downloaded. Opening the pack in Minecraft…", android.widget.Toast.LENGTH_LONG).show()
                            selectedProject = null
                        } catch (failure: Exception) {
                            downloadError = failure.message ?: "Could not download or open this pack."
                        } finally {
                            installingDownload = false
                        }
                    }
                }) {
                    Text(if (installingDownload) "Downloading…" else "Install in Minecraft", color = Accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { openExternalLink(context, project.projectUrl) }) { Text("Open page", color = Accent) }
                    TextButton(onClick = { selectedProject = null }) { Text("Close", color = TextMuted) }
                }
            }
        )
    }
}

private fun formatDownloadCount(value: Long): String = when {
    value >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", value / 1_000_000.0)
    value >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", value / 1_000.0)
    else -> value.toString()
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
