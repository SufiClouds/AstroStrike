package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HighScoreEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LeaderboardScreen(viewModel: GameViewModel) {
    val scores by viewModel.topScores.collectAsState()
    val stats by viewModel.pilotStats.collectAsState()

    BackHandler {
        viewModel.navigateTo(ScreenState.MENU)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(ScreenState.MENU) },
                modifier = Modifier.testTag("leaderboard_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Menu",
                    tint = TextWhite
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "HALL OF FAME",
                color = SolarGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Career Overview Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SpaceCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SolarGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PILOT CAREER LOG",
                    color = SolarGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CareerStatItem("HIGH SCORE", String.format("%,d", stats?.highScore ?: 0L), NeonCyan)
                    CareerStatItem("MISSIONS", "${stats?.totalGamesPlayed ?: 0}", ShieldBlue)
                    CareerStatItem("ENEMIES", "${stats?.totalEnemiesDefeated ?: 0}", NeonMagenta)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "TOP FLIGHT RECORDS",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        if (scores.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Flight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No flight records logged yet.",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Launch a mission to claim your rank!",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(scores) { index, item ->
                    HighScoreRow(index = index + 1, item = item)
                }
            }
        }
    }
}

@Composable
private fun CareerStatItem(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun HighScoreRow(index: Int, item: HighScoreEntity) {
    val rankColor = when (index) {
        1 -> SolarGold
        2 -> Color(0xFFE2E8F0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> TextMuted
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateStr = dateFormat.format(Date(item.dateTimestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = SpaceCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (index <= 3) 1.5.dp else 1.dp,
            color = if (index <= 3) rankColor.copy(alpha = 0.6f) else Color.DarkGray
        ),
        modifier = Modifier.fillMaxWidth().testTag("score_row_$index")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(rankColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (index <= 3) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = rankColor,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = "#$index",
                            color = rankColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = String.format("%,d PTS", item.score),
                        color = TextWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Wave ${item.wave} • ${item.enemiesKilled} Enemies • $dateStr",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                color = SpaceBlack,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "W${item.wave}",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
