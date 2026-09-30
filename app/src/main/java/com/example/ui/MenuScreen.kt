package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.random.Random

@Composable
fun MenuScreen(viewModel: GameViewModel) {
    val stats by viewModel.pilotStats.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingShip"
    )

    // Pre-generate stars for menu background
    val stars = remember {
        val rand = Random(1234)
        List(60) {
            Triple(rand.nextFloat(), rand.nextFloat(), 1.5f + rand.nextFloat() * 2f)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack)
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // Background Star Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEach { (normX, normY, size) ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = size,
                    center = Offset(normX * screenWidth, normY * screenHeight)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Title & Audio controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier.testTag("sound_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Sound",
                            tint = if (soundEnabled) NeonCyan else Color.Gray
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleHaptics() },
                        modifier = Modifier.testTag("haptics_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Toggle Vibration",
                            tint = if (hapticsEnabled) NeonMagenta else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title Banner
                Text(
                    text = "ASTROSTRIKE",
                    color = NeonCyan,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "RETRO ARCADE COMBAT",
                    color = NeonMagenta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                // High score badge
                stats?.let { s ->
                    if (s.highScore > 0L) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = SpaceCard,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SolarGold.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = SolarGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RECORD: ${String.format("%,d", s.highScore)}",
                                    color = SolarGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Center Area: Animated Starship Hologram
            val skinIdx = stats?.shipColorIndex ?: 0
            val activeSkin = SHIP_SKINS.getOrElse(skinIdx) { SHIP_SKINS[0] }

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulsing glowing ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f + floatOffset

                    drawCircle(
                        color = activeSkin.primaryColor.copy(alpha = 0.15f),
                        radius = 80.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = activeSkin.primaryColor.copy(alpha = 0.4f),
                        radius = 80.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Draw Ship Silhouette
                    val hw = 45.dp.toPx()
                    val hh = 55.dp.toPx()
                    val shipPath = Path().apply {
                        moveTo(cx, cy - hh)
                        lineTo(cx + hw * 0.4f, cy - hh * 0.2f)
                        lineTo(cx + hw, cy + hh * 0.7f)
                        lineTo(cx + hw * 0.5f, cy + hh * 0.5f)
                        lineTo(cx + hw * 0.3f, cy + hh)
                        lineTo(cx, cy + hh * 0.7f)
                        lineTo(cx - hw * 0.3f, cy + hh)
                        lineTo(cx - hw * 0.5f, cy + hh * 0.5f)
                        lineTo(cx - hw, cy + hh * 0.7f)
                        lineTo(cx - hw * 0.4f, cy - hh * 0.2f)
                        close()
                    }
                    drawPath(path = shipPath, color = activeSkin.primaryColor)
                    drawPath(path = shipPath, color = activeSkin.accentColor, style = Stroke(width = 2.dp.toPx()))

                    // Cockpit
                    val cockpit = Path().apply {
                        moveTo(cx, cy - hh * 0.7f)
                        lineTo(cx + hw * 0.22f, cy - hh * 0.05f)
                        lineTo(cx, cy + hh * 0.15f)
                        lineTo(cx - hw * 0.22f, cy - hh * 0.05f)
                        close()
                    }
                    drawPath(path = cockpit, color = Color.White)
                }
            }

            // Bottom Actions: Start, Hangar, Leaderboard
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Launch Mission (Big Primary)
                Button(
                    onClick = { viewModel.startMission(screenWidth, screenHeight) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("launch_mission_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "LAUNCH MISSION",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hangar & Upgrades
                Button(
                    onClick = { viewModel.navigateTo(ScreenState.HANGAR) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("hangar_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpaceCard,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SolarGold.copy(alpha = 0.7f))
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = SolarGold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SHIP HANGAR & UPGRADES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hall of Fame
                OutlinedButton(
                    onClick = { viewModel.navigateTo(ScreenState.LEADERBOARD) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("leaderboard_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ShieldBlue
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ShieldBlue.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ShieldBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HALL OF FAME",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
