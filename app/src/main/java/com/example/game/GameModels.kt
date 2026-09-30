package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PlasmaOrange
import com.example.ui.theme.SolarGold

enum class ScreenState {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    HANGAR,
    LEADERBOARD
}

data class ShipSkin(
    val name: String,
    val primaryColor: Color,
    val accentColor: Color,
    val thrusterColor: Color
)

val SHIP_SKINS = listOf(
    ShipSkin("Cyan Striker", NeonCyan, Color(0xFF00BBF9), SolarGold),
    ShipSkin("Crimson Phantom", NeonMagenta, PlasmaOrange, Color(0xFFFF0054)),
    ShipSkin("Solar Phoenix", SolarGold, PlasmaOrange, NeonCyan),
    ShipSkin("Void Reaper", NeonPurple, NeonMagenta, Color(0xFF4CC9F0)),
    ShipSkin("Emerald Viper", Color(0xFF06D6A0), Color(0xFF118AB2), Color(0xFFFFD166))
)

data class PlayerShip(
    var x: Float = 0f,
    var y: Float = 0f,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var width: Float = 60f,
    var height: Float = 70f,
    var health: Float = 100f,
    var maxHealth: Float = 100f,
    var shield: Float = 100f,
    var maxShield: Float = 100f,
    var blasterLevel: Int = 1,
    var magnetLevel: Int = 1,
    var invulnerableTimer: Float = 0f,
    var skinIndex: Int = 0
)

enum class EnemyType {
    DRONE,
    SCOUT,
    ASTEROID,
    INTERCEPTOR,
    BOSS
}

data class Enemy(
    val id: Long,
    val type: EnemyType,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var health: Float,
    var maxHealth: Float,
    val width: Float,
    val height: Float,
    var shootCooldown: Float,
    val scoreValue: Int,
    var patternTimer: Float = 0f,
    var angle: Float = 0f
)

data class Bullet(
    val id: Long,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val isEnemy: Boolean,
    val damage: Float,
    val color: Color,
    val radius: Float = 5f
)

enum class PowerUpType {
    SPREAD_BURST,
    SHIELD_RESTORE,
    ENERGY_CORE,
    NOVA_ORB
}

data class PowerUp(
    val id: Long,
    val type: PowerUpType,
    var x: Float,
    var y: Float,
    var vy: Float,
    val radius: Float = 16f
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float = 1f,
    val color: Color,
    val radius: Float,
    val decay: Float
)

data class FloatingText(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    var vy: Float = -2f,
    var alpha: Float = 1f,
    val color: Color
)

data class Star(
    var x: Float,
    var y: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float
)
