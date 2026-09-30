package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.game.ScreenState
import com.example.ui.GamePlayScreen
import com.example.ui.GameViewModel
import com.example.ui.HangarScreen
import com.example.ui.LeaderboardScreen
import com.example.ui.MenuScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaceBlack

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SpaceBlack)
                ) { _ ->
                    AstroStrikeApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseGame()
    }
}

@Composable
fun AstroStrikeApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        when (currentScreen) {
            ScreenState.MENU -> MenuScreen(viewModel = viewModel)
            ScreenState.PLAYING,
            ScreenState.PAUSED,
            ScreenState.GAME_OVER -> GamePlayScreen(viewModel = viewModel)
            ScreenState.HANGAR -> HangarScreen(viewModel = viewModel)
            ScreenState.LEADERBOARD -> LeaderboardScreen(viewModel = viewModel)
        }
    }
}
