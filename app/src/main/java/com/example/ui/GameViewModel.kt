package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.AstroDatabase
import com.example.data.GameRepository
import com.example.data.HighScoreEntity
import com.example.data.PilotStatsEntity
import com.example.game.GameEngine
import com.example.game.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AstroDatabase.getDatabase(application)
    private val repository = GameRepository(database.gameDao())

    val soundManager = SoundManager(application)
    val gameEngine = GameEngine()

    private val _currentScreen = MutableStateFlow(ScreenState.MENU)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _isNewHighScore = MutableStateFlow(false)
    val isNewHighScore: StateFlow<Boolean> = _isNewHighScore.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    val pilotStats: StateFlow<PilotStatsEntity?> = repository.pilotStats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PilotStatsEntity()
        )

    val topScores: StateFlow<List<HighScoreEntity>> = repository.topScores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.ensureInitialStats()
        }
    }

    fun navigateTo(screen: ScreenState) {
        _currentScreen.value = screen
    }

    fun startMission(width: Float, height: Float) {
        val stats = pilotStats.value ?: PilotStatsEntity()
        gameEngine.initialize(
            width = width,
            height = height,
            blasterLvl = stats.blasterLevel,
            shieldLvl = stats.shieldLevel,
            magnetLvl = stats.magnetLevel,
            bombs = stats.bombCount,
            skinIdx = stats.shipColorIndex
        )
        _isNewHighScore.value = false
        _currentScreen.value = ScreenState.PLAYING
    }

    fun pauseGame() {
        if (_currentScreen.value == ScreenState.PLAYING) {
            _currentScreen.value = ScreenState.PAUSED
        }
    }

    fun resumeGame() {
        if (_currentScreen.value == ScreenState.PAUSED) {
            _currentScreen.value = ScreenState.PLAYING
        }
    }

    fun triggerBomb() {
        gameEngine.triggerNovaBomb(soundManager)
    }

    fun onGameOver() {
        _currentScreen.value = ScreenState.GAME_OVER
        viewModelScope.launch {
            val isNew = repository.saveGameResult(
                score = gameEngine.score,
                wave = gameEngine.wave,
                enemiesKilled = gameEngine.enemiesKilled,
                energyCoresCollected = gameEngine.energyCoresCollected
            )
            _isNewHighScore.value = isNew
        }
    }

    fun upgradeBlaster() {
        val stats = pilotStats.value ?: return
        val cost = stats.blasterLevel * 30
        viewModelScope.launch {
            if (repository.upgradeBlaster(cost)) {
                soundManager.playPowerup()
            }
        }
    }

    fun upgradeShield() {
        val stats = pilotStats.value ?: return
        val cost = stats.shieldLevel * 25
        viewModelScope.launch {
            if (repository.upgradeShield(cost)) {
                soundManager.playPowerup()
            }
        }
    }

    fun upgradeMagnet() {
        val stats = pilotStats.value ?: return
        val cost = stats.magnetLevel * 20
        viewModelScope.launch {
            if (repository.upgradeMagnet(cost)) {
                soundManager.playPowerup()
            }
        }
    }

    fun buyNovaBomb() {
        val stats = pilotStats.value ?: return
        val cost = 40
        viewModelScope.launch {
            if (repository.buyNovaBomb(cost)) {
                soundManager.playPowerup()
            }
        }
    }

    fun selectSkin(index: Int) {
        viewModelScope.launch {
            repository.selectShipColor(index)
            soundManager.playPowerup()
        }
    }

    fun toggleSound() {
        val newVal = !_soundEnabled.value
        _soundEnabled.value = newVal
        soundManager.soundEnabled = newVal
    }

    fun toggleHaptics() {
        val newVal = !_hapticsEnabled.value
        _hapticsEnabled.value = newVal
        soundManager.hapticsEnabled = newVal
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
