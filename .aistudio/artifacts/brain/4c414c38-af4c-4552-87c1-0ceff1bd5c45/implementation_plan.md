# AstroStrike: Retro Arcade Space Combat

An action-packed retro-arcade space shooter built entirely in Jetpack Compose, featuring fluid 60 FPS Canvas rendering, responsive touch/drag ship controls, particle systems, weapon upgrades, boss battles, sound & haptic feedback, and local leaderboard persistence with Room.

## User Review & Critical Decisions

> [!IMPORTANT]
> Based on your request for a gaming app, we have designed **AstroStrike**, a high-octane 2D arcade shooter optimized for mobile touchscreens with smooth hardware-accelerated Canvas graphics and zero lag.

- **Game Genre**: 2D Vertical Arcade Space Combat (bullet-hell-lite with accessible difficulty ramp).
- **Control Scheme**: Smooth one-thumb drag steering with auto-fire or touch-to-charge special weapons (shield blast & plasma bomb).
- **Core Loop**: Survive enemy waves, collect energy cores, fight stage bosses, unlock ship skins and weapon enhancements in the Hangar, and compete for high scores.
- **Audio & Haptics**: Native Android tone generator (`AudioTrack` / `SoundPool`) for laser blasts and explosions, paired with tactile haptic vibration pulses on hits.
- **Offline Persistence**: Room SQLite database storing player pilot stats, best runs, high score leaderboards, and unlocked upgrades.

---

## 1. Overview & Core Concept

- **What It Does**: Puts the player in command of the AstroStrike starfighter against incoming alien squadrons, drifting asteroid belts, and formidable flagship bosses. Players navigate by dragging, collect weapon power-ups (scatter shot, beam laser, homing missiles, energy shields), trigger screen-clearing super bombs, and rack up score multipliers.
- **Target Audience**: Mobile arcade and retro gamers seeking instant, thrilling pick-up-and-play sessions with deep progression and zero latency.
- **Key Value**: A complete, polished native Android gaming experience with custom 60 FPS graphics, reactive sound and vibration, ship customization, and saved score tracking without requiring any heavy third-party game engines.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Title / Hangar Screen**:
   - Atmospheric starfield background with twinkling nebulae.
   - Quick "Play Now" action, plus tabs for "Ship Hangar" (upgrade blaster fire rate, shield capacity, magnet range) and "Hall of Fame" (local leaderboard and badges).
2. **Active Combat Arena**:
   - Top HUD: Current Score with dynamic multiplier gauge, remaining lives/shields bar, wave count, and super-bomb charge icon.
   - Playfield: Fluid 60 FPS canvas with parallax star layers, smooth player ship following finger drag, dynamic enemy trajectories (squadron swoops, kamikaze dives), laser projectiles, and neon particle burst animations.
   - Power-Up Drops: Floating gems and weapon crates with pulsing aura (Spread Shot, Laser Beam, Health Repair, Overcharge).
   - Boss Encounters: Warning sirens, colossal boss ship with dynamic health bar, phased attack patterns, and explosive multi-stage destruction.
3. **Game Over / Victory Debrief**:
   - Wave summary, total aliens defeated, accuracy percentage, and final score.
   - High score celebratory badge if personal record is beaten.
   - Instant "Play Again" and "Upgrade Ship" shortcuts.

### Visual Identity & Theme

- **Aesthetic Direction**: Vibrant Cyber-Arcade Neon. Deep cosmic space background accented by electric cyan, hot magenta, golden plasma, and laser green.
- **Color Palette**:
  - Cosmic Void: `#0A0E1A` / Deep Nebula: `#13192B`
  - Neon Cyan (Player Ship & Lasers): `#00F5D4` / `#00BBF9`
  - Plasma Magenta (Enemy Squads): `#F72585` / `#7209B7`
  - Warning Gold (Bosses & Power-Ups): `#FFBE0B` / `#FB5607`
  - Shield Blue: `#4CC9F0`
- **Typography & UI**: High-legibility monospaced score counter, bold futuristic headings, clean Material 3 cards for stats and hangar upgrades.
- **Micro-Interactions**: Screen shake on heavy impacts, trailing engine particle exhaust, floating hit damage numbers, and satisfying haptic rumble.

---

## 3. Key Product Decisions & Trade-Offs

- **Compose Canvas vs. External Game Engine**:
  - *Chosen Approach*: Pure Jetpack Compose `Canvas` driven by a fixed-timestep game loop using `withFrameNanos`.
  - *Why*: Eliminates heavy external dependencies, keeps the APK lightweight (< 15 MB), compiles rapidly, and integrates seamlessly with Material 3 UI for menus, dialogs, and stats.
  - *Alternatives Considered*: Unity or Godot (excessive overhead and external tooling incompatibility) or standard Compose View tree (too slow for 50+ bullets).
- **Touch Input Architecture**:
  - *Chosen Approach*: Relative offset finger tracking with smooth lerp interpolation.
  - *Why*: The ship stays slightly above the user's thumb so the player's own finger never obstructs their vision of incoming enemies or bullets.
- **Local Persistence via Room**:
  - *Chosen Approach*: Room SQLite database with tables for `HighScoreEntity`, `PlayerStatsEntity`, and `ShipUpgradeEntity`.
  - *Why*: 100% offline-ready, rock-solid persistence across app restarts, instant loading, and zero network requirement.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                      Android MainActivity                       │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                 ┌───────────────┴───────────────┐
                 │    GameState / AppNavigator   │
                 └───────┬───────────────┬───────┘
                         │               │
         ┌───────────────▼────────┐      └───────────────┐
         │   GameScreen (Canvas)  │                      │
         │ - 60 FPS Frame Loop    │        ┌─────────────▼─────────────┐
         │ - Player & Enemy Logic │        │   Hangar / Leaderboard    │
         │ - Particle System      │        │ - Ship Customization      │
         │ - Bullet Collision     │        │ - Room-backed High Scores │
         │ - Sound & Haptic FX    │        └─────────────┬─────────────┘
         └───────────────┬────────┘                      │
                         │                               │
         ┌───────────────▼───────────────────────────────▼─────────────┐
         │                       GameViewModel                         │
         │ - High-frequency state & score calculation                  │
         │ - Sound manager & vibrator service                          │
         │ - Upgrade economy & unlock validation                       │
         └───────────────────────────────┬─────────────────────────────┘
                                         │
                         ┌───────────────▼───────────────┐
                         │   GameRepository / Room DB    │
                         │ - HighScoreDao, UpgradeDao    │
                         └───────────────────────────────┘
```

### Data Model & Entities

- **`HighScoreEntity`**:
  - `id: Long` (Auto-generated)
  - `playerName: String`
  - `score: Long`
  - `waveReached: Int`
  - `enemiesDestroyed: Int`
  - `dateTimestamp: Long`
- **`PilotProgressEntity`**:
  - `id: Int = 1` (Single row singleton)
  - `energyCores: Int` (currency earned by playing)
  - `blasterLevel: Int`
  - `shieldLevel: Int`
  - `magnetLevel: Int`
  - `bombLevel: Int`
  - `selectedShipColor: String`

### Audio & Sound System

- Uses Android's `AudioTrack` and `SoundPool` to generate crisp retro 8-bit laser shot tones, explosion rumbles, and power-up jingles without needing large external audio files.

---
