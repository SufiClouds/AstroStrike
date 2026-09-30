package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.Bullet
import com.example.game.Enemy
import com.example.game.EnemyType
import com.example.game.GameEngine
import com.example.game.PlayerShip
import com.example.game.PowerUp
import com.example.game.PowerUpType
import com.example.game.SHIP_SKINS
import com.example.game.ScreenState
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.PlasmaOrange
import com.example.ui.theme.ShieldBlue
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceCard
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GamePlayScreen(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isNewHighScore by viewModel.isNewHighScore.collectAsState()
    val engine = viewModel.gameEngine

    BackHandler {
        if (currentScreen == ScreenState.PLAYING) {
            viewModel.pauseGame()
        } else {
            viewModel.navigateTo(ScreenState.MENU)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(SpaceBlack)) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // 60 FPS Game Loop
        var frameTick by remember { mutableLongStateOf(0L) }
        LaunchedEffect(currentScreen) {
            if (currentScreen == ScreenState.PLAYING) {
                var lastTime = System.nanoTime()
                while (true) {
                    withFrameNanos { now ->
                        val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                        lastTime = now
                        engine.update(dt, screenWidth, screenHeight, viewModel.soundManager)
                        if (engine.isGameOver && currentScreen == ScreenState.PLAYING) {
                            viewModel.onGameOver()
                        }
                        frameTick = now
                    }
                }
            }
        }

        // Canvas Rendering
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_canvas")
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            engine.player.targetX = offset.x
                            engine.player.targetY = offset.y - 70f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            engine.player.targetX += dragAmount.x
                            engine.player.targetY += dragAmount.y
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        engine.player.targetX = offset.x
                        engine.player.targetY = offset.y - 70f
                    }
                }
        ) {
            // Read frameTick to trigger recomposition per frame
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTick

            // Screen Shake offset
            val shakeX = if (engine.screenShake > 0f) (Math.random().toFloat() - 0.5f) * engine.screenShake else 0f
            val shakeY = if (engine.screenShake > 0f) (Math.random().toFloat() - 0.5f) * engine.screenShake else 0f

            // 1. Draw Starfield
            engine.stars.forEach { star ->
                drawCircle(
                    color = Color.White.copy(alpha = star.alpha),
                    radius = star.size,
                    center = Offset(star.x + shakeX, star.y + shakeY)
                )
            }

            // 2. Draw Particles
            engine.particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    radius = p.radius,
                    center = Offset(p.x + shakeX, p.y + shakeY)
                )
            }

            // 3. Draw PowerUps
            engine.powerUps.forEach { p ->
                drawPowerUp(p, shakeX, shakeY)
            }

            // 4. Draw Bullets
            engine.bullets.forEach { b ->
                drawBullet(b, shakeX, shakeY)
            }

            // 5. Draw Enemies
            engine.enemies.forEach { e ->
                drawEnemy(e, shakeX, shakeY)
            }

            // 6. Draw Player Ship
            if (!engine.isGameOver) {
                drawPlayer(engine.player, shakeX, shakeY)
            }

            // 7. Draw Nova Bomb screen flash
            if (engine.novaFlashAlpha > 0f) {
                drawRect(
                    color = Color.White.copy(alpha = engine.novaFlashAlpha.coerceIn(0f, 1f)),
                    size = size
                )
            }
        }

        // Top HUD Overlay
        TopHudOverlay(
            engine = engine,
            onPauseClick = { viewModel.pauseGame() },
            onBombClick = { viewModel.triggerBomb() }
        )

        // Pause Menu Dialog
        if (currentScreen == ScreenState.PAUSED) {
            PauseDialog(
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.startMission(screenWidth, screenHeight) },
                onQuit = { viewModel.navigateTo(ScreenState.MENU) }
            )
        }

        // Game Over Overlay
        if (currentScreen == ScreenState.GAME_OVER) {
            GameOverDialog(
                engine = engine,
                isNewRecord = isNewHighScore,
                onRetry = { viewModel.startMission(screenWidth, screenHeight) },
                onHangar = { viewModel.navigateTo(ScreenState.HANGAR) },
                onMenu = { viewModel.navigateTo(ScreenState.MENU) }
            )
        }
    }
}

private fun DrawScope.drawPlayer(player: PlayerShip, shakeX: Float, shakeY: Float) {
    val px = player.x + shakeX
    val py = player.y + shakeY
    val hw = player.width / 2f
    val hh = player.height / 2f
    val skin = SHIP_SKINS.getOrElse(player.skinIndex) { SHIP_SKINS[0] }

    // Invulnerability blink
    if (player.invulnerableTimer > 0f && ((player.invulnerableTimer * 15).toInt() % 2 == 0)) {
        return
    }

    // Shield Aura
    if (player.shield > 0f) {
        val shieldAlpha = (player.shield / player.maxShield * 0.35f).coerceIn(0.1f, 0.4f)
        drawCircle(
            color = ShieldBlue.copy(alpha = shieldAlpha),
            radius = hw * 1.5f,
            center = Offset(px, py)
        )
        drawCircle(
            color = ShieldBlue.copy(alpha = shieldAlpha * 1.5f),
            radius = hw * 1.5f,
            center = Offset(px, py),
            style = Stroke(width = 2.dp.toPx())
        )
    }

    // Ship Hull (Custom Path)
    val hullPath = Path().apply {
        moveTo(px, py - hh) // Nose
        lineTo(px + hw * 0.4f, py - hh * 0.2f)
        lineTo(px + hw, py + hh * 0.7f) // Right Wing tip
        lineTo(px + hw * 0.5f, py + hh * 0.5f)
        lineTo(px + hw * 0.3f, py + hh) // Right thruster
        lineTo(px, py + hh * 0.7f) // Center engine notch
        lineTo(px - hw * 0.3f, py + hh) // Left thruster
        lineTo(px - hw * 0.5f, py + hh * 0.5f)
        lineTo(px - hw, py + hh * 0.7f) // Left wing tip
        lineTo(px - hw * 0.4f, py - hh * 0.2f)
        close()
    }

    // Primary fill & glow
    drawPath(path = hullPath, color = skin.primaryColor)
    drawPath(path = hullPath, color = skin.accentColor, style = Stroke(width = 2.dp.toPx()))

    // Cockpit Canopy
    val cockpitPath = Path().apply {
        moveTo(px, py - hh * 0.7f)
        lineTo(px + hw * 0.22f, py - hh * 0.05f)
        lineTo(px, py + hh * 0.15f)
        lineTo(px - hw * 0.22f, py - hh * 0.05f)
        close()
    }
    drawPath(path = cockpitPath, color = Color.White)
}

private fun DrawScope.drawEnemy(e: Enemy, shakeX: Float, shakeY: Float) {
    val ex = e.x + shakeX
    val ey = e.y + shakeY
    val hw = e.width / 2f
    val hh = e.height / 2f

    when (e.type) {
        EnemyType.DRONE -> {
            val path = Path().apply {
                moveTo(ex, ey + hh)
                lineTo(ex + hw, ey - hh * 0.5f)
                lineTo(ex, ey - hh)
                lineTo(ex - hw, ey - hh * 0.5f)
                close()
            }
            drawPath(path = path, color = NeonMagenta)
            drawPath(path = path, color = Color.White.copy(alpha = 0.6f), style = Stroke(width = 1.5.dp.toPx()))
        }
        EnemyType.SCOUT -> {
            val path = Path().apply {
                moveTo(ex, ey + hh)
                lineTo(ex + hw, ey - hh)
                lineTo(ex + hw * 0.3f, ey - hh * 0.4f)
                lineTo(ex - hw * 0.3f, ey - hh * 0.4f)
                lineTo(ex - hw, ey - hh)
                close()
            }
            drawPath(path = path, color = PlasmaOrange)
            drawPath(path = path, color = SolarGold, style = Stroke(width = 1.5.dp.toPx()))
        }
        EnemyType.ASTEROID -> {
            rotate(degrees = e.angle, pivot = Offset(ex, ey)) {
                drawCircle(
                    color = Color(0xFF64748B),
                    radius = hw,
                    center = Offset(ex, ey)
                )
                drawCircle(
                    color = Color(0xFF94A3B8),
                    radius = hw,
                    center = Offset(ex, ey),
                    style = Stroke(width = 2.dp.toPx())
                )
                // Craters
                drawCircle(
                    color = Color(0xFF475569),
                    radius = hw * 0.35f,
                    center = Offset(ex - hw * 0.25f, ey - hw * 0.2f)
                )
                drawCircle(
                    color = Color(0xFF475569),
                    radius = hw * 0.2f,
                    center = Offset(ex + hw * 0.35f, ey + hw * 0.3f)
                )
            }
        }
        EnemyType.INTERCEPTOR -> {
            val path = Path().apply {
                moveTo(ex, ey + hh)
                lineTo(ex + hw, ey - hh)
                lineTo(ex, ey - hh * 0.3f)
                lineTo(ex - hw, ey - hh)
                close()
            }
            drawPath(path = path, color = NeonCyan)
        }
        EnemyType.BOSS -> {
            // Flagship Dreadnought
            val bossPath = Path().apply {
                moveTo(ex, ey + hh)
                lineTo(ex + hw * 0.4f, ey + hh * 0.6f)
                lineTo(ex + hw, ey - hh * 0.3f)
                lineTo(ex + hw * 0.8f, ey - hh)
                lineTo(ex, ey - hh * 0.5f)
                lineTo(ex - hw * 0.8f, ey - hh)
                lineTo(ex - hw, ey - hh * 0.3f)
                lineTo(ex - hw * 0.4f, ey + hh * 0.6f)
                close()
            }
            drawPath(path = bossPath, color = Color(0xFF7209B7))
            drawPath(path = bossPath, color = SolarGold, style = Stroke(width = 3.dp.toPx()))

            // Boss Core Reactor
            val corePulse = (sin(e.patternTimer * 8.0) * 4f).toFloat()
            drawCircle(
                color = SolarGold,
                radius = 16f + corePulse,
                center = Offset(ex, ey)
            )

            // Boss Health Bar above boss
            val barW = 120f
            val barH = 8f
            val healthPercent = (e.health / e.maxHealth).coerceIn(0f, 1f)
            drawRect(
                color = Color.DarkGray,
                topLeft = Offset(ex - barW / 2f, ey - hh - 22f),
                size = Size(barW, barH)
            )
            drawRect(
                color = PlasmaOrange,
                topLeft = Offset(ex - barW / 2f, ey - hh - 22f),
                size = Size(barW * healthPercent, barH)
            )
        }
    }
}

private fun DrawScope.drawBullet(b: Bullet, shakeX: Float, shakeY: Float) {
    val bx = b.x + shakeX
    val by = b.y + shakeY

    if (b.isEnemy) {
        drawCircle(
            color = b.color,
            radius = b.radius,
            center = Offset(bx, by)
        )
        drawCircle(
            color = Color.White,
            radius = b.radius * 0.5f,
            center = Offset(bx, by)
        )
    } else {
        // Laser bolt with trail
        drawLine(
            color = b.color,
            start = Offset(bx, by),
            end = Offset(bx, by + 18f),
            strokeWidth = b.radius * 1.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(bx, by),
            end = Offset(bx, by + 12f),
            strokeWidth = b.radius * 0.8f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawPowerUp(p: PowerUp, shakeX: Float, shakeY: Float) {
    val px = p.x + shakeX
    val py = p.y + shakeY
    val color = when (p.type) {
        PowerUpType.ENERGY_CORE -> SolarGold
        PowerUpType.SHIELD_RESTORE -> ShieldBlue
        PowerUpType.SPREAD_BURST -> PlasmaOrange
        PowerUpType.NOVA_ORB -> NeonMagenta
    }

    // Pulsing outer aura
    drawCircle(
        color = color.copy(alpha = 0.35f),
        radius = p.radius * 1.5f,
        center = Offset(px, py)
    )
    drawCircle(
        color = color,
        radius = p.radius,
        center = Offset(px, py)
    )
    drawCircle(
        color = Color.White,
        radius = p.radius * 0.4f,
        center = Offset(px, py)
    )
}

@Composable
private fun TopHudOverlay(
    engine: GameEngine,
    onPauseClick: () -> Unit,
    onBombClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // Top Row: Score, Wave, Pause
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Health & Shield Bars
            Column(modifier = Modifier.width(130.dp)) {
                // Shield bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SHD",
                        color = ShieldBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(30.dp)
                    )
                    LinearProgressIndicator(
                        progress = { (engine.player.shield / engine.player.maxShield).coerceIn(0f, 1f) },
                        modifier = Modifier.height(6.dp).fillMaxWidth(),
                        color = ShieldBlue,
                        trackColor = Color.DarkGray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Health bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "HULL",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(30.dp)
                    )
                    LinearProgressIndicator(
                        progress = { (engine.player.health / engine.player.maxHealth).coerceIn(0f, 1f) },
                        modifier = Modifier.height(6.dp).fillMaxWidth(),
                        color = if (engine.player.health > 30f) NeonCyan else PlasmaOrange,
                        trackColor = Color.DarkGray
                    )
                }
            }

            // Score & Multiplier
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%,d", engine.score),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                if (engine.multiplier > 1.0f) {
                    Text(
                        text = String.format("%.1fx COMBO", engine.multiplier),
                        color = SolarGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Wave & Pause
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = SpaceCard,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "W${engine.wave}",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = Color.White
                    )
                }
            }
        }

        // Nova Bomb Action Button (Bottom Right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 8.dp)
        ) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = NeonMagenta,
                        contentColor = Color.White
                    ) {
                        Text(text = "${engine.bombCount}")
                    }
                }
            ) {
                FilledIconButton(
                    onClick = onBombClick,
                    enabled = engine.bombCount > 0,
                    modifier = Modifier
                        .size(60.dp)
                        .testTag("nova_bomb_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = PlasmaOrange,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Trigger Nova Bomb",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SpaceCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MISSION PAUSED",
                    color = NeonCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth().testTag("resume_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resume Flight", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth().testTag("restart_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restart Mission")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onQuit,
                    modifier = Modifier.fillMaxWidth().testTag("quit_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                ) {
                    Text("Abort to Base")
                }
            }
        }
    }
}

@Composable
private fun GameOverDialog(
    engine: GameEngine,
    isNewRecord: Boolean,
    onRetry: () -> Unit,
    onHangar: () -> Unit,
    onMenu: () -> Unit
) {
    Dialog(onDismissRequest = onMenu) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SpaceCard),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, if (isNewRecord) SolarGold else NeonMagenta),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isNewRecord) "NEW HIGH SCORE!" else "MISSION DEBRIEF",
                    color = if (isNewRecord) SolarGold else NeonMagenta,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Score card
                Surface(
                    color = SpaceBlack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("FINAL SCORE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = String.format("%,d", engine.score),
                            color = NeonCyan,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Stats breakdown
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("WAVE", color = TextMuted, fontSize = 11.sp)
                        Text("${engine.wave}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ENEMIES", color = TextMuted, fontSize = 11.sp)
                        Text("${engine.enemiesKilled}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CORES", color = TextMuted, fontSize = 11.sp)
                        Text("+${engine.energyCoresCollected}", color = SolarGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("play_again_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deploy Again", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onHangar,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("upgrade_hangar_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SolarGold, contentColor = Color.Black)
                ) {
                    Text("Visit Ship Hangar", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onMenu,
                    modifier = Modifier.fillMaxWidth().testTag("return_menu_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                ) {
                    Text("Main Command")
                }
            }
        }
    }
}

private val TextWhite = Color(0xFFF1F5F9)
private val TextMuted = Color(0xFF94A3B8)
