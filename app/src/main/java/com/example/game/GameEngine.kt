package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.audio.SoundManager
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.PlasmaOrange
import com.example.ui.theme.ShieldBlue
import com.example.ui.theme.SolarGold
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class GameEngine {
    var player = PlayerShip()
    val enemies = mutableListOf<Enemy>()
    val bullets = mutableListOf<Bullet>()
    val powerUps = mutableListOf<PowerUp>()
    val particles = mutableListOf<Particle>()
    val floatingTexts = mutableListOf<FloatingText>()
    val stars = mutableListOf<Star>()

    var score: Long = 0L
    var multiplier: Float = 1.0f
    var multiplierTimer: Float = 0f
    var wave: Int = 1
    var waveProgress: Float = 0f
    var enemiesKilled: Int = 0
    var energyCoresCollected: Int = 0
    var bombCount: Int = 2
    var boss: Enemy? = null
    var isGameOver: Boolean = false
    var isVictory: Boolean = false

    var screenShake: Float = 0f
    var novaFlashAlpha: Float = 0f
    var spreadBurstTimer: Float = 0f
    var playerShootTimer: Float = 0f
    var spawnTimer: Float = 0f
    var shieldRegenCooldown: Float = 0f

    private var nextEntityId = 1L
    private val random = Random(System.currentTimeMillis())

    fun initialize(
        width: Float,
        height: Float,
        blasterLvl: Int,
        shieldLvl: Int,
        magnetLvl: Int,
        bombs: Int,
        skinIdx: Int
    ) {
        val maxShld = 60f + (shieldLvl * 20f)
        player = PlayerShip(
            x = width / 2f,
            y = height * 0.8f,
            targetX = width / 2f,
            targetY = height * 0.8f,
            width = 54f,
            height = 64f,
            health = 100f,
            maxHealth = 100f,
            shield = maxShld,
            maxShield = maxShld,
            blasterLevel = blasterLvl,
            magnetLevel = magnetLvl,
            skinIndex = skinIdx
        )

        bombCount = bombs
        score = 0L
        multiplier = 1.0f
        wave = 1
        waveProgress = 0f
        enemiesKilled = 0
        energyCoresCollected = 0
        isGameOver = false
        isVictory = false
        boss = null
        screenShake = 0f
        novaFlashAlpha = 0f

        enemies.clear()
        bullets.clear()
        powerUps.clear()
        particles.clear()
        floatingTexts.clear()
        stars.clear()

        // Generate layered starfield
        for (i in 0 until 90) {
            val speed = 0.5f + random.nextFloat() * 3.5f
            val size = if (speed > 2.5f) 3f else if (speed > 1.5f) 2f else 1.2f
            val alpha = 0.3f + random.nextFloat() * 0.7f
            stars.add(Star(random.nextFloat() * width, random.nextFloat() * height, speed, size, alpha))
        }
    }

    fun update(
        dt: Float,
        width: Float,
        height: Float,
        soundManager: SoundManager
    ) {
        if (width <= 0f || height <= 0f) return

        // Screen shake decay
        if (screenShake > 0f) {
            screenShake = (screenShake - dt * 25f).coerceAtLeast(0f)
        }
        // Nova flash decay
        if (novaFlashAlpha > 0f) {
            novaFlashAlpha = (novaFlashAlpha - dt * 2.5f).coerceAtLeast(0f)
        }

        // Starfield scroll
        stars.forEach { star ->
            star.y += star.speed * (if (boss != null) 1.5f else 1.0f) * 60f * dt
            if (star.y > height) {
                star.y = 0f
                star.x = random.nextFloat() * width
            }
        }

        // Multiplier decay
        if (multiplier > 1.0f) {
            multiplierTimer += dt
            if (multiplierTimer > 3.5f) {
                multiplier = (multiplier - dt * 0.5f).coerceAtLeast(1.0f)
            }
        }

        if (isGameOver) {
            updateParticles(dt)
            updateFloatingTexts(dt)
            return
        }

        // Player Movement (smooth lerp toward target with screen bounds clamp)
        val lerpFactor = (15f * dt).coerceIn(0.1f, 1f)
        player.x += (player.targetX - player.x) * lerpFactor
        player.y += (player.targetY - player.y) * lerpFactor

        val halfW = player.width / 2f
        val halfH = player.height / 2f
        player.x = player.x.coerceIn(halfW, width - halfW)
        player.y = player.y.coerceIn(halfH, height - halfH)

        // Engine exhaust trail particles
        if (random.nextFloat() < 0.75f) {
            val skin = SHIP_SKINS.getOrElse(player.skinIndex) { SHIP_SKINS[0] }
            particles.add(
                Particle(
                    x = player.x + (random.nextFloat() - 0.5f) * 12f,
                    y = player.y + halfH - 4f,
                    vx = (random.nextFloat() - 0.5f) * 20f,
                    vy = 60f + random.nextFloat() * 60f,
                    color = skin.thrusterColor,
                    radius = 2.5f + random.nextFloat() * 2.5f,
                    decay = 2.2f
                )
            )
        }

        // Invulnerability timer
        if (player.invulnerableTimer > 0f) {
            player.invulnerableTimer -= dt
        }

        // Shield regeneration
        shieldRegenCooldown -= dt
        if (shieldRegenCooldown <= 0f && player.shield < player.maxShield) {
            player.shield = (player.shield + 8f * dt).coerceAtMost(player.maxShield)
        }

        // Spread burst powerup duration
        if (spreadBurstTimer > 0f) {
            spreadBurstTimer -= dt
        }

        // Player Auto-Firing
        val fireCooldown = when (player.blasterLevel) {
            1 -> 0.22f
            2 -> 0.19f
            3 -> 0.16f
            4 -> 0.14f
            else -> 0.11f
        }
        playerShootTimer += dt
        if (playerShootTimer >= fireCooldown) {
            playerShootTimer = 0f
            firePlayerWeapons(soundManager)
        }

        // Spawning system & Wave Logic
        updateWavesAndSpawning(dt, width, height, soundManager)

        // Update Bullets
        updateBullets(dt, width, height)

        // Update Enemies
        updateEnemies(dt, width, height, soundManager)

        // Update Powerups & Magnet
        updatePowerUps(dt, height, soundManager)

        // Check Collisions
        checkCollisions(soundManager)

        // Update Particles and Floating Texts
        updateParticles(dt)
        updateFloatingTexts(dt)
    }

    private fun firePlayerWeapons(soundManager: SoundManager) {
        val px = player.x
        val py = player.y - player.height / 2f
        val bulletSpeed = -650f
        val skin = SHIP_SKINS.getOrElse(player.skinIndex) { SHIP_SKINS[0] }

        when (player.blasterLevel) {
            1 -> {
                bullets.add(Bullet(nextEntityId++, px, py, 0f, bulletSpeed, false, 28f, skin.primaryColor, 4.5f))
            }
            2 -> {
                bullets.add(Bullet(nextEntityId++, px - 12f, py, 0f, bulletSpeed, false, 25f, skin.primaryColor, 4.5f))
                bullets.add(Bullet(nextEntityId++, px + 12f, py, 0f, bulletSpeed, false, 25f, skin.primaryColor, 4.5f))
            }
            3 -> {
                bullets.add(Bullet(nextEntityId++, px, py, 0f, bulletSpeed, false, 28f, skin.primaryColor, 5f))
                bullets.add(Bullet(nextEntityId++, px - 14f, py + 4f, -90f, bulletSpeed, false, 22f, skin.accentColor, 4f))
                bullets.add(Bullet(nextEntityId++, px + 14f, py + 4f, 90f, bulletSpeed, false, 22f, skin.accentColor, 4f))
            }
            4 -> {
                bullets.add(Bullet(nextEntityId++, px - 8f, py, 0f, bulletSpeed, false, 26f, skin.primaryColor, 4.5f))
                bullets.add(Bullet(nextEntityId++, px + 8f, py, 0f, bulletSpeed, false, 26f, skin.primaryColor, 4.5f))
                bullets.add(Bullet(nextEntityId++, px - 20f, py + 6f, -120f, bulletSpeed, false, 24f, skin.accentColor, 4.5f))
                bullets.add(Bullet(nextEntityId++, px + 20f, py + 6f, 120f, bulletSpeed, false, 24f, skin.accentColor, 4.5f))
            }
            else -> {
                // Hyper stream
                bullets.add(Bullet(nextEntityId++, px, py, 0f, bulletSpeed * 1.15f, false, 32f, SolarGold, 5.5f))
                bullets.add(Bullet(nextEntityId++, px - 12f, py, -60f, bulletSpeed, false, 26f, skin.primaryColor, 4.5f))
                bullets.add(Bullet(nextEntityId++, px + 12f, py, 60f, bulletSpeed, false, 26f, skin.primaryColor, 4.5f))
                bullets.add(Bullet(nextEntityId++, px - 24f, py + 6f, -150f, bulletSpeed * 0.95f, false, 24f, skin.accentColor, 4.5f))
                bullets.add(Bullet(nextEntityId++, px + 24f, py + 6f, 150f, bulletSpeed * 0.95f, false, 24f, skin.accentColor, 4.5f))
            }
        }

        if (spreadBurstTimer > 0f) {
            bullets.add(Bullet(nextEntityId++, px - 26f, py, -220f, bulletSpeed * 0.85f, false, 30f, PlasmaOrange, 5f))
            bullets.add(Bullet(nextEntityId++, px + 26f, py, 220f, bulletSpeed * 0.85f, false, 30f, PlasmaOrange, 5f))
        }

        soundManager.playLaser()
    }

    private fun updateWavesAndSpawning(dt: Float, width: Float, height: Float, soundManager: SoundManager) {
        if (boss != null) return // No random waves while fighting boss

        spawnTimer += dt
        waveProgress += dt

        // Check if wave finished and should summon boss
        if (wave % 5 == 0 && waveProgress > 12f && enemies.isEmpty()) {
            spawnBoss(width, height)
            return
        } else if (waveProgress > 22f && enemies.isEmpty()) {
            // Next wave
            wave++
            waveProgress = 0f
            floatingTexts.add(
                FloatingText(
                    id = nextEntityId++,
                    text = "WAVE $wave",
                    x = width / 2f - 40f,
                    y = height * 0.35f,
                    vy = -20f,
                    color = NeonCyan
                )
            )
            return
        }

        val spawnInterval = (2.2f - (wave * 0.15f)).coerceAtLeast(0.85f)
        if (spawnTimer >= spawnInterval && waveProgress < 18f) {
            spawnTimer = 0f
            spawnEnemySquadron(width)
        }
    }

    private fun spawnEnemySquadron(width: Float) {
        val r = random.nextFloat()
        when {
            r < 0.4f -> {
                // Drone squadron (3 drones)
                val startX = (random.nextFloat() * (width - 120f)) + 60f
                for (i in 0..2) {
                    enemies.add(
                        Enemy(
                            id = nextEntityId++,
                            type = EnemyType.DRONE,
                            x = startX + (i - 1) * 35f,
                            y = -50f - (i * 25f),
                            vx = if (random.nextBoolean()) 60f else -60f,
                            vy = 90f + wave * 6f,
                            health = 35f + wave * 5f,
                            maxHealth = 35f + wave * 5f,
                            width = 38f,
                            height = 38f,
                            shootCooldown = 1.2f + random.nextFloat() * 1.5f,
                            scoreValue = 100
                        )
                    )
                }
            }
            r < 0.7f -> {
                // Scout interceptor
                enemies.add(
                    Enemy(
                        id = nextEntityId++,
                        type = EnemyType.SCOUT,
                        x = random.nextFloat() * (width - 80f) + 40f,
                        y = -40f,
                        vx = 0f,
                        vy = 120f + wave * 8f,
                        health = 60f + wave * 10f,
                        maxHealth = 60f + wave * 10f,
                        width = 44f,
                        height = 48f,
                        shootCooldown = 0.9f,
                        scoreValue = 200
                    )
                )
            }
            else -> {
                // Asteroid hazard
                val rad = 30f + random.nextFloat() * 15f
                enemies.add(
                    Enemy(
                        id = nextEntityId++,
                        type = EnemyType.ASTEROID,
                        x = random.nextFloat() * (width - 80f) + 40f,
                        y = -50f,
                        vx = (random.nextFloat() - 0.5f) * 40f,
                        vy = 65f + wave * 4f,
                        health = 90f + wave * 15f,
                        maxHealth = 90f + wave * 15f,
                        width = rad * 2f,
                        height = rad * 2f,
                        shootCooldown = 9999f,
                        scoreValue = 150
                    )
                )
            }
        }
    }

    private fun spawnBoss(width: Float, height: Float) {
        val bossHealth = 800f + (wave * 300f)
        val newBoss = Enemy(
            id = nextEntityId++,
            type = EnemyType.BOSS,
            x = width / 2f,
            y = -120f,
            vx = 80f,
            vy = 35f,
            health = bossHealth,
            maxHealth = bossHealth,
            width = 120f,
            height = 100f,
            shootCooldown = 1.0f,
            scoreValue = 5000
        )
        enemies.add(newBoss)
        boss = newBoss
        floatingTexts.add(
            FloatingText(
                id = nextEntityId++,
                text = "WARNING: FLAGSHIP INCOMING",
                x = width / 2f - 90f,
                y = height * 0.3f,
                vy = -10f,
                color = NeonMagenta
            )
        )
        screenShake = 12f
    }

    private fun updateEnemies(dt: Float, width: Float, height: Float, soundManager: SoundManager) {
        val enemyIterator = enemies.iterator()
        while (enemyIterator.hasNext()) {
            val e = enemyIterator.next()
            e.patternTimer += dt

            when (e.type) {
                EnemyType.DRONE -> {
                    e.y += e.vy * dt
                    e.x += sin(e.patternTimer * 3f) * 110f * dt
                    // Drone shooting
                    e.shootCooldown -= dt
                    if (e.shootCooldown <= 0f && e.y > 40f && e.y < height * 0.7f) {
                        e.shootCooldown = 1.8f + random.nextFloat()
                        bullets.add(Bullet(nextEntityId++, e.x, e.y + 15f, 0f, 260f, true, 12f, NeonMagenta, 4f))
                    }
                }
                EnemyType.SCOUT -> {
                    if (e.y < height * 0.35f) {
                        e.y += e.vy * dt
                    } else {
                        // Strafe and fire targeted shots
                        e.x += (sin(e.patternTimer * 2f) * 130f) * dt
                        e.shootCooldown -= dt
                        if (e.shootCooldown <= 0f) {
                            e.shootCooldown = 1.2f
                            val angle = atan2(player.y - e.y, player.x - e.x)
                            val speed = 280f
                            bullets.add(
                                Bullet(
                                    nextEntityId++,
                                    e.x,
                                    e.y + 15f,
                                    cos(angle) * speed,
                                    sin(angle) * speed,
                                    true,
                                    16f,
                                    PlasmaOrange,
                                    4.5f
                                )
                            )
                        }
                        if (e.patternTimer > 6.0f) {
                            e.y += e.vy * 1.5f * dt
                        }
                    }
                }
                EnemyType.ASTEROID -> {
                    e.y += e.vy * dt
                    e.x += e.vx * dt
                    e.angle += 45f * dt
                }
                EnemyType.INTERCEPTOR -> {
                    e.y += e.vy * dt
                    e.x += e.vx * dt
                }
                EnemyType.BOSS -> {
                    // Boss entrance and strafe
                    if (e.y < 130f) {
                        e.y += e.vy * dt
                    } else {
                        e.x += e.vx * dt
                        if (e.x > width - 80f) {
                            e.x = width - 80f
                            e.vx = -80f
                        } else if (e.x < 80f) {
                            e.x = 80f
                            e.vx = 80f
                        }

                        // Boss multi-turret shooting patterns
                        e.shootCooldown -= dt
                        if (e.shootCooldown <= 0f) {
                            e.shootCooldown = 1.2f
                            // 5-bullet radial spray
                            for (i in -2..2) {
                                val spreadAngle = (Math.PI / 2.0) + (i * 0.22)
                                val speed = 260f
                                bullets.add(
                                    Bullet(
                                        nextEntityId++,
                                        e.x + (i * 20f),
                                        e.y + 40f,
                                        (cos(spreadAngle) * speed).toFloat(),
                                        (sin(spreadAngle) * speed).toFloat(),
                                        true,
                                        18f,
                                        SolarGold,
                                        5f
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Remove offscreen enemies
            if (e.y > height + 80f || e.x < -100f || e.x > width + 100f) {
                if (e == boss) boss = null
                enemyIterator.remove()
            }
        }
    }

    private fun updateBullets(dt: Float, width: Float, height: Float) {
        val bulletIter = bullets.iterator()
        while (bulletIter.hasNext()) {
            val b = bulletIter.next()
            b.x += b.vx * dt
            b.y += b.vy * dt

            if (b.y < -20f || b.y > height + 20f || b.x < -20f || b.x > width + 20f) {
                bulletIter.remove()
            }
        }
    }

    private fun updatePowerUps(dt: Float, height: Float, soundManager: SoundManager) {
        val magnetDist = 80f + (player.magnetLevel * 45f)
        val powerIter = powerUps.iterator()
        while (powerIter.hasNext()) {
            val p = powerIter.next()
            p.y += p.vy * dt

            // Magnet effect towards player
            val dx = player.x - p.x
            val dy = player.y - p.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < magnetDist && dist > 1f) {
                val pullSpeed = (400f / dist).coerceIn(60f, 450f)
                p.x += (dx / dist) * pullSpeed * dt
                p.y += (dy / dist) * pullSpeed * dt
            }

            // Pickup detection
            if (dist < player.width / 2f + p.radius) {
                applyPowerUp(p, soundManager)
                powerIter.remove()
                continue
            }

            if (p.y > height + 40f) {
                powerIter.remove()
            }
        }
    }

    private fun applyPowerUp(powerUp: PowerUp, soundManager: SoundManager) {
        soundManager.playPowerup()
        when (powerUp.type) {
            PowerUpType.ENERGY_CORE -> {
                energyCoresCollected += 5
                addScore(150)
                floatingTexts.add(FloatingText(nextEntityId++, "+5 CORES", powerUp.x, powerUp.y, -30f, color = SolarGold))
            }
            PowerUpType.SHIELD_RESTORE -> {
                player.shield = (player.shield + 40f).coerceAtMost(player.maxShield)
                floatingTexts.add(FloatingText(nextEntityId++, "SHIELD REPAIR", powerUp.x, powerUp.y, -30f, color = ShieldBlue))
            }
            PowerUpType.SPREAD_BURST -> {
                spreadBurstTimer = 10f
                floatingTexts.add(FloatingText(nextEntityId++, "OVERCHARGE!", powerUp.x, powerUp.y, -30f, color = PlasmaOrange))
            }
            PowerUpType.NOVA_ORB -> {
                bombCount = (bombCount + 1).coerceAtMost(5)
                floatingTexts.add(FloatingText(nextEntityId++, "+1 NOVA BOMB", powerUp.x, powerUp.y, -30f, color = NeonMagenta))
            }
        }
    }

    private fun checkCollisions(soundManager: SoundManager) {
        // 1. Player Bullets vs Enemies
        val bulletIter = bullets.iterator()
        while (bulletIter.hasNext()) {
            val b = bulletIter.next()
            if (b.isEnemy) continue

            var hit = false
            for (enemy in enemies) {
                val dx = b.x - enemy.x
                val dy = b.y - enemy.y
                val distSq = dx * dx + dy * dy
                val hitRad = (enemy.width + enemy.height) / 4f + b.radius

                if (distSq < hitRad * hitRad) {
                    enemy.health -= b.damage
                    hit = true

                    // Spark particle
                    createHitSparks(b.x, b.y, b.color)

                    if (enemy.health <= 0f) {
                        onEnemyDestroyed(enemy, soundManager)
                    }
                    break
                }
            }
            if (hit) {
                bulletIter.remove()
            }
        }

        // 2. Enemy Bullets vs Player
        val enemyBulletIter = bullets.iterator()
        while (enemyBulletIter.hasNext()) {
            val b = enemyBulletIter.next()
            if (!b.isEnemy) continue

            val dx = b.x - player.x
            val dy = b.y - player.y
            val distSq = dx * dx + dy * dy
            val hitRad = player.width / 2.2f + b.radius

            if (distSq < hitRad * hitRad) {
                applyDamageToPlayer(b.damage, soundManager)
                enemyBulletIter.remove()
            }
        }

        // 3. Enemy Ship vs Player (Ramming)
        if (player.invulnerableTimer <= 0f) {
            for (enemy in enemies) {
                val dx = enemy.x - player.x
                val dy = enemy.y - player.y
                val distSq = dx * dx + dy * dy
                val hitRad = (player.width + enemy.width) / 2.8f

                if (distSq < hitRad * hitRad) {
                    applyDamageToPlayer(30f, soundManager)
                    enemy.health -= 60f
                    if (enemy.health <= 0f) {
                        onEnemyDestroyed(enemy, soundManager)
                    }
                    break
                }
            }
        }
    }

    private fun applyDamageToPlayer(damage: Float, soundManager: SoundManager) {
        if (player.invulnerableTimer > 0f) return

        shieldRegenCooldown = 3.5f
        soundManager.playHit()
        screenShake = 15f
        player.invulnerableTimer = 0.6f

        if (player.shield > 0f) {
            if (player.shield >= damage) {
                player.shield -= damage
            } else {
                val remaining = damage - player.shield
                player.shield = 0f
                player.health = (player.health - remaining).coerceAtLeast(0f)
            }
        } else {
            player.health = (player.health - damage).coerceAtLeast(0f)
        }

        multiplier = 1.0f

        if (player.health <= 0f) {
            isGameOver = true
            soundManager.playExplosion()
            createExplosion(player.x, player.y, NeonCyan, 45)
            screenShake = 30f
        }
    }

    private fun onEnemyDestroyed(enemy: Enemy, soundManager: SoundManager) {
        soundManager.playExplosion()
        enemiesKilled++
        addScore(enemy.scoreValue)

        // Increment combo multiplier
        multiplier = (multiplier + 0.1f).coerceAtMost(4.0f)
        multiplierTimer = 0f

        // Screen shake on boss / heavy explosion
        screenShake = if (enemy.type == EnemyType.BOSS) 25f else 6f

        // Explosion particles
        val color = when (enemy.type) {
            EnemyType.DRONE -> NeonMagenta
            EnemyType.SCOUT -> PlasmaOrange
            EnemyType.ASTEROID -> Color(0xFF8D99AE)
            EnemyType.BOSS -> SolarGold
            else -> NeonCyan
        }
        val count = if (enemy.type == EnemyType.BOSS) 65 else 20
        createExplosion(enemy.x, enemy.y, color, count)

        // Power-Up drops
        dropPowerUp(enemy.x, enemy.y, enemy.type)

        if (enemy == boss) {
            boss = null
            wave++
            waveProgress = 0f
            floatingTexts.add(FloatingText(nextEntityId++, "BOSS DESTROYED! +5000", enemy.x, enemy.y, vy = -30f, color = SolarGold))
            energyCoresCollected += 25
        }
    }

    private fun dropPowerUp(x: Float, y: Float, type: EnemyType) {
        val roll = random.nextFloat()
        val dropChance = if (type == EnemyType.BOSS) 1.0f else 0.28f

        if (roll < dropChance) {
            val powerType = when {
                type == EnemyType.BOSS -> PowerUpType.NOVA_ORB
                roll < 0.12f -> PowerUpType.ENERGY_CORE
                roll < 0.20f -> PowerUpType.SHIELD_RESTORE
                roll < 0.26f -> PowerUpType.SPREAD_BURST
                else -> PowerUpType.ENERGY_CORE
            }
            powerUps.add(PowerUp(nextEntityId++, powerType, x, y, 70f))
        }
    }

    fun triggerNovaBomb(soundManager: SoundManager): Boolean {
        if (bombCount <= 0 || isGameOver) return false

        bombCount--
        soundManager.playBomb()
        novaFlashAlpha = 0.95f
        screenShake = 35f

        // Wipe all enemy bullets
        bullets.removeAll { it.isEnemy }

        // Damage all enemies on screen
        val enemyIterator = enemies.iterator()
        while (enemyIterator.hasNext()) {
            val e = enemyIterator.next()
            e.health -= 500f
            createExplosion(e.x, e.y, SolarGold, 18)
            if (e.health <= 0f) {
                onEnemyDestroyed(e, soundManager)
            }
        }
        return true
    }

    private fun addScore(points: Int) {
        val earned = (points * multiplier).toLong()
        score += earned
    }

    private fun createHitSparks(x: Float, y: Float, color: Color) {
        for (i in 0..4) {
            val angle = random.nextFloat() * Math.PI.toFloat() * 2f
            val speed = 40f + random.nextFloat() * 60f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    radius = 2.5f,
                    decay = 4.5f
                )
            )
        }
    }

    private fun createExplosion(x: Float, y: Float, color: Color, count: Int) {
        for (i in 0 until count) {
            val angle = random.nextFloat() * Math.PI.toFloat() * 2f
            val speed = 60f + random.nextFloat() * 200f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = if (i % 2 == 0) color else SolarGold,
                    radius = 3f + random.nextFloat() * 4f,
                    decay = 1.6f + random.nextFloat() * 1.2f
                )
            )
        }
    }

    private fun updateParticles(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.alpha -= p.decay * dt
            if (p.alpha <= 0f) {
                pIter.remove()
            }
        }
    }

    private fun updateFloatingTexts(dt: Float) {
        val fIter = floatingTexts.iterator()
        while (fIter.hasNext()) {
            val f = fIter.next()
            f.y += f.vy * dt
            f.alpha -= 0.9f * dt
            if (f.alpha <= 0f) {
                fIter.remove()
            }
        }
    }
}
