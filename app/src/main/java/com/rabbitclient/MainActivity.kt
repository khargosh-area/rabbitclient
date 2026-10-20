package com.rabbitclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF0B0E12)
private val Surface = Color(0xFF151A21)
private val Mint = Color(0xFF9BE7C4)
private val Muted = Color(0xFF9AA5B1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RabbitApp() }
    }
}

private data class ModuleItem(val name: String, val detail: String, val category: String)

private val modules = listOf(
    ModuleItem("FPS Counter", "Show frame rate", "HUD"),
    ModuleItem("Coordinates", "Position overlay", "HUD"),
    ModuleItem("CPS Counter", "Clicks per second", "HUD"),
    ModuleItem("Armor HUD", "Equipment status", "HUD"),
    ModuleItem("Potion HUD", "Active effects", "HUD"),
    ModuleItem("Fullbright", "Brightness option", "Visual"),
    ModuleItem("Zoom", "Quick zoom control", "Utility"),
    ModuleItem("Sprint Toggle", "Sprint control", "Utility"),
    ModuleItem("FOV Controls", "Field of view presets", "Visual")
)

@Composable
private fun RabbitApp() {
    var page by remember { mutableStateOf("Home") }
    var selectedCategory by remember { mutableStateOf("All") }
    var enabled by remember { mutableStateOf(setOf<String>()) }
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Background,
            surface = Surface,
            primary = Mint,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                NavigationBar(containerColor = Surface, contentColor = Muted) {
                    NavigationBarItem(selected = page == "Home", onClick = { page = "Home" },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) }, label = { Text("Home") })
                    NavigationBarItem(selected = page == "Modules", onClick = { page = "Modules" },
                        icon = { Icon(Icons.Default.ViewModule, contentDescription = null) }, label = { Text("Modules") })
                    NavigationBarItem(selected = page == "Instances", onClick = { page = "Instances" },
                        icon = { Icon(Icons.Default.Tune, contentDescription = null) }, label = { Text("Instances") })
                    NavigationBarItem(selected = page == "Settings", onClick = { page = "Settings" },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) }, label = { Text("Settings") })
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("RABBIT CLIENT", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(page, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    }
                    Surface(color = Surface, shape = RoundedCornerShape(14.dp)) {
                        Text("v0.1.0", color = Muted, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(22.dp))
                when (page) {
                    "Home" -> {
                        Surface(color = Surface, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp)) {
                                Text("Your Minecraft space.", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text("A fresh launcher foundation with an independent module framework.", color = Muted)
                                Spacer(Modifier.height(18.dp))
                                Button(onClick = { page = "Instances" }, colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Background), modifier = Modifier.fillMaxWidth()) {
                                    Text("Manage instances", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("QUICK OVERVIEW", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryCard("Modules", modules.size.toString(), Modifier.weight(1f))
                            SummaryCard("Instances", "0", Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(18.dp))
                        Text("BUILD STATUS", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Launcher UI prototype. In-game modules are not connected to Minecraft yet.", color = Muted, fontSize = 13.sp)
                    }
                    "Modules" -> {
                        Text("MODULE LIBRARY", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("All", "HUD", "Visual", "Utility").forEach { category ->
                                FilterChip(selected = selectedCategory == category, onClick = { selectedCategory = category }, label = { Text(category) })
                            }
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                            items(modules.filter { selectedCategory == "All" || it.category == selectedCategory }) { module ->
                                Surface(color = Surface, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(module.name, fontWeight = FontWeight.SemiBold)
                                            Text(module.detail, color = Muted, fontSize = 12.sp)
                                            Text(module.category.uppercase(), color = Mint, fontSize = 10.sp)
                                        }
                                        Switch(checked = module.name in enabled, onCheckedChange = { checked ->
                                            enabled = if (checked) enabled + module.name else enabled - module.name
                                        })
                                    }
                                }
                            }
                        }
                    }
                    "Instances" -> {
                        Surface(color = Surface, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp)) {
                                Text("No instances added", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text("Instance discovery, import/export, and launch integration are not implemented in this starter build.", color = Muted)
                            }
                        }
                    }
                    else -> {
                        Surface(color = Surface, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp)) {
                                Text("Appearance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Dark theme • Mint accent", color = Muted)
                                Spacer(Modifier.height(16.dp))
                                Text("Integration status", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Module switches currently store local demo state only; they do not affect gameplay.", color = Muted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(color = Surface, shape = RoundedCornerShape(20.dp), modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(value, color = Mint, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Muted, fontSize = 13.sp)
        }
    }
}
