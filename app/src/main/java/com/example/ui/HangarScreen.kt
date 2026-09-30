package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.SHIP_SKINS
import com.example.game.ScreenState
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.PlasmaOrange
import com.example.ui.theme.ShieldBlue
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun HangarScreen(viewModel: GameViewModel) {
    val stats by viewModel.pilotStats.collectAsState()
    val currentCores = stats?.energyCores ?: 0
    val selectedSkin = stats?.shipColorIndex ?: 0

    BackHandler {
        viewModel.navigateTo(ScreenState.MENU)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(ScreenState.MENU) },
                modifier = Modifier.testTag("hangar_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Menu",
                    tint = TextWhite
                )
            }

            Text(
                text = "SHIP HANGAR",
                color = NeonCyan,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            // Energy Cores balance
            Surface(
                color = SpaceCard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SolarGold.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = SolarGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$currentCores",
                        color = SolarGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Ship Hull Paint Selector
            item {
                Text(
                    text = "SELECT CHASSIS PAINT",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(SHIP_SKINS) { index, skin ->
                        val isSelected = index == selectedSkin
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SpaceCard),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) skin.primaryColor else Color.DarkGray
                            ),
                            modifier = Modifier
                                .width(130.dp)
                                .clickable { viewModel.selectSkin(index) }
                                .testTag("skin_select_$index")
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Mini ship preview
                                Canvas(modifier = Modifier.size(50.dp)) {
                                    val cx = size.width / 2f
                                    val cy = size.height / 2f
                                    val hw = 18.dp.toPx()
                                    val hh = 22.dp.toPx()
                                    val path = Path().apply {
                                        moveTo(cx, cy - hh)
                                        lineTo(cx + hw * 0.4f, cy - hh * 0.2f)
                                        lineTo(cx + hw, cy + hh * 0.7f)
                                        lineTo(cx, cy + hh * 0.6f)
                                        lineTo(cx - hw, cy + hh * 0.7f)
                                        lineTo(cx - hw * 0.4f, cy - hh * 0.2f)
                                        close()
                                    }
                                    drawPath(path, skin.primaryColor)
                                    drawPath(path, skin.accentColor, style = Stroke(width = 1.5.dp.toPx()))
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = skin.name,
                                    color = if (isSelected) skin.primaryColor else TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Upgrades Section
            item {
                Text(
                    text = "SHIP SYSTEMS & WEAPONRY",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                )
            }

            // 1. Blaster
            item {
                val level = stats?.blasterLevel ?: 1
                val cost = level * 30
                UpgradeCard(
                    title = "Photon Blaster",
                    description = when (level) {
                        1 -> "Single plasma beam"
                        2 -> "Twin dual blasters"
                        3 -> "Triple spread trajectory"
                        4 -> "Quad high-velocity cannons"
                        else -> "MAX: Hyper stream cannon"
                    },
                    icon = Icons.Default.FlashOn,
                    iconColor = NeonCyan,
                    level = level,
                    maxLevel = 5,
                    cost = cost,
                    canAfford = currentCores >= cost && level < 5,
                    onUpgrade = { viewModel.upgradeBlaster() },
                    tag = "upgrade_blaster"
                )
            }

            // 2. Shield
            item {
                val level = stats?.shieldLevel ?: 1
                val cost = level * 25
                UpgradeCard(
                    title = "Deflector Shield",
                    description = "Max Shield: ${60 + level * 20} HP (Regenerates over time)",
                    icon = Icons.Default.Shield,
                    iconColor = ShieldBlue,
                    level = level,
                    maxLevel = 5,
                    cost = cost,
                    canAfford = currentCores >= cost && level < 5,
                    onUpgrade = { viewModel.upgradeShield() },
                    tag = "upgrade_shield"
                )
            }

            // 3. Magnet
            item {
                val level = stats?.magnetLevel ?: 1
                val cost = level * 20
                UpgradeCard(
                    title = "Core Collector Magnet",
                    description = "Attracts floating energy cores and weapon power-ups from ${80 + level * 45}px",
                    icon = Icons.Default.Sensors,
                    iconColor = SolarGold,
                    level = level,
                    maxLevel = 5,
                    cost = cost,
                    canAfford = currentCores >= cost && level < 5,
                    onUpgrade = { viewModel.upgradeMagnet() },
                    tag = "upgrade_magnet"
                )
            }

            // 4. Nova Bomb Arsenal
            item {
                val bombCount = stats?.bombCount ?: 0
                val cost = 40
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpaceCard),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.DarkGray),
                    modifier = Modifier.fillMaxWidth().testTag("bomb_arsenal_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PlasmaOrange.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = PlasmaOrange)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Nova Bomb Supply", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Current stock: $bombCount / 5", color = TextMuted, fontSize = 12.sp)
                                Text("Screen-clearing pulse", color = PlasmaOrange, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = { viewModel.buyNovaBomb() },
                            enabled = currentCores >= cost && bombCount < 5,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PlasmaOrange,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("buy_bomb_button")
                        ) {
                            if (bombCount >= 5) {
                                Text("FULL", fontWeight = FontWeight.Bold)
                            } else {
                                Text("$cost CORES", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    level: Int,
    maxLevel: Int,
    cost: Int,
    canAfford: Boolean,
    onUpgrade: () -> Unit,
    tag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SpaceCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.DarkGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "Rank $level / $maxLevel", color = iconColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = onUpgrade,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (level >= maxLevel) Color.DarkGray else iconColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag(tag)
                ) {
                    if (level >= maxLevel) {
                        Text("MAX", fontWeight = FontWeight.Bold)
                    } else {
                        Text("$cost CORES", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { level.toFloat() / maxLevel },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = iconColor,
                trackColor = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, color = TextMuted, fontSize = 12.sp)
        }
    }
}
