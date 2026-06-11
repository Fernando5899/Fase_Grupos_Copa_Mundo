package com.example.mundial_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mundial_2.ui.theme.Mundial_2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Mundial_2Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { Header() }
                ) { innerPadding ->
                    MundialScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Immutable
data class Team(val id: String, val name: String, val flag: String)

data class Match(
    val id: Int,
    val local: Team,
    val visitor: Team,
    val localScore: String = "",
    val visitorScore: String = ""
)

data class TeamStats(
    val team: Team,
    var jj: Int = 0,
    var jg: Int = 0,
    var je: Int = 0,
    var jp: Int = 0,
    var gf: Int = 0,
    var gc: Int = 0,
    var dg: Int = 0,
    var pts: Int = 0
)

@Composable
fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1B3E))
            .padding(top = 36.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🏆", fontSize = 22.sp, modifier = Modifier.padding(end = 8.dp))
        Column {
            Text(
                text = "MUNDIAL 2026",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "GRUPO A",
                color = Color(0xFFFF9800),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MundialScreen(modifier: Modifier = Modifier) {
    val teams = remember {
        listOf(
            Team("MX", "México", "🇲🇽"),
            Team("KR", "Corea del Sur", "🇰🇷"),
            Team("CZ", "República Checa", "🇨🇿"),
            Team("ZA", "Sudáfrica", "🇿🇦")
        )
    }

    var matches by remember {
        mutableStateOf(
            listOf(
                Match(1, teams[0], teams[3]), // MX vs ZA
                Match(2, teams[1], teams[2]), // KR vs CZ
                Match(3, teams[2], teams[3]), // CZ vs ZA
                Match(4, teams[0], teams[1]), // MX vs KR
                Match(5, teams[2], teams[0]), // CZ vs MX
                Match(6, teams[3], teams[1])  // ZA vs KR
            )
        )
    }

    val standings = remember(matches) {
        calculateStandings(teams, matches)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PARTIDOS DEL GRUPO A",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
        )

        matches.forEach { match ->
            MatchCard(
                match = match,
                onScoreChange = { localScore, visitorScore ->
                    matches = matches.map {
                        if (it.id == match.id) it.copy(localScore = localScore, visitorScore = visitorScore)
                        else it
                    }
                }
            )
            Spacer(modifier = Modifier.height(5.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        StandingsTable(standings)

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(6.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "ⓘ", color = Color(0xFF0288D1), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Los dos primeros lugares de cada grupo clasifican a dieciseisavos de final.",
                    fontSize = 10.sp,
                    color = Color(0xFF0288D1),
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
fun MatchCard(match: Match, onScoreChange: (String, String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 3.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Local
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.1f)) {
                    Text(text = match.local.flag, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = match.local.name, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1)
                }

                Text(text = "vs", color = Color.Gray, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp))

                // Visitor
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.1f), horizontalArrangement = Arrangement.End) {
                    Text(text = match.visitor.name, fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.End, maxLines = 1)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = match.visitor.flag, fontSize = 18.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Inputs Section
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(text = "Local", fontSize = 7.sp, color = Color.Gray)
                    ScoreInput(value = match.localScore, onValueChange = { onScoreChange(it, match.visitorScore) })
                }

                Text(text = "•", color = Color.LightGray, modifier = Modifier.padding(horizontal = 10.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(text = "Visitante", fontSize = 7.sp, color = Color.Gray)
                    ScoreInput(value = match.visitorScore, onValueChange = { onScoreChange(match.localScore, it) })
                }
            }
        }
    }
}

@Composable
fun ScoreInput(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) onValueChange(it) },
        modifier = Modifier
            .width(55.dp)
            .height(26.dp)
            .border(0.5.dp, Color.LightGray, RoundedCornerShape(5.dp)),
        textStyle = androidx.compose.ui.text.TextStyle(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.Black
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                innerTextField()
            }
        }
    )
}

@Composable
fun StandingsTable(standings: List<TeamStats>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(0.5.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "📊", fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "TABLA DE POSICIONES",
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp
            )
        }

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D1B3E))
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableCell(text = "Pos", weight = 0.6f, color = Color.White)
            TableCell(text = "Equipo", weight = 2.6f, color = Color.White)
            TableCell(text = "JJ", weight = 0.6f, color = Color.White)
            TableCell(text = "JG", weight = 0.6f, color = Color.White)
            TableCell(text = "JE", weight = 0.6f, color = Color.White)
            TableCell(text = "JP", weight = 0.6f, color = Color.White)
            TableCell(text = "GF", weight = 0.6f, color = Color.White)
            TableCell(text = "GC", weight = 0.6f, color = Color.White)
            TableCell(text = "DG", weight = 0.6f, color = Color.White)
            TableCell(text = "PTS", weight = 0.8f, color = Color.White)
        }

        standings.forEachIndexed { index, stats ->
            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableCell(text = "${index + 1}", weight = 0.6f, isPos = true, pos = index + 1)
                Row(modifier = Modifier.weight(2.6f), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stats.team.flag, modifier = Modifier.padding(horizontal = 3.dp), fontSize = 11.sp)
                    Text(text = stats.team.name, fontSize = 8.5.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                }
                TableCell(text = "${stats.jj}", weight = 0.6f)
                TableCell(text = "${stats.jg}", weight = 0.6f)
                TableCell(text = "${stats.je}", weight = 0.6f)
                TableCell(text = "${stats.jp}", weight = 0.6f)
                TableCell(text = "${stats.gf}", weight = 0.6f)
                TableCell(text = "${stats.gc}", weight = 0.6f)
                TableCell(text = "${if (stats.dg > 0) "+" else ""}${stats.dg}", weight = 0.6f)
                TableCell(text = "${stats.pts}", weight = 0.8f, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B3E))
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    color: Color = Color.Black,
    fontWeight: FontWeight = FontWeight.Normal,
    isPos: Boolean = false,
    pos: Int = 0
) {
    Box(
        modifier = Modifier.weight(weight),
        contentAlignment = Alignment.Center
    ) {
        if (isPos) {
            val bgColor = when (pos) {
                1 -> Color(0xFF1B5E20) // Green (Qualified)
                2 -> Color(0xFF2E7D32) // Green (Qualified)
                3 -> Color(0xFFB71C1C) // Red (Not Qualified)
                4 -> Color(0xFFD32F2F) // Red (Not Qualified)
                else -> Color.Transparent
            }
            if (bgColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .size(17.dp)
                        .background(bgColor, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = androidx.compose.ui.text.TextStyle(
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
            } else {
                Text(
                    text = text,
                    color = color,
                    fontSize = 9.sp,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(
                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
            }
        } else {
            Text(
                text = text,
                color = color,
                fontSize = 9.sp,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
        }
    }
}

fun calculateStandings(teams: List<Team>, matches: List<Match>): List<TeamStats> {
    val statsMap = teams.associateBy({ it.id }, { TeamStats(it) }).toMutableMap()

    matches.forEach { match ->
        val localScore = match.localScore.toIntOrNull()
        val visitorScore = match.visitorScore.toIntOrNull()

        if (localScore != null && visitorScore != null) {
            val localStats = statsMap[match.local.id]!!
            val visitorStats = statsMap[match.visitor.id]!!

            localStats.jj++
            visitorStats.jj++

            localStats.gf += localScore
            localStats.gc += visitorScore
            visitorStats.gf += visitorScore
            visitorStats.gc += localScore

            when {
                localScore > visitorScore -> {
                    localStats.jg++
                    localStats.pts += 3
                    visitorStats.jp++
                }
                localScore < visitorScore -> {
                    visitorStats.jg++
                    visitorStats.pts += 3
                    localStats.jp++
                }
                else -> {
                    localStats.je++
                    visitorStats.je++
                    localStats.pts += 1
                    visitorStats.pts += 1
                }
            }
        }
    }

    return statsMap.values.map {
        it.dg = it.gf - it.gc
        it
    }.sortedWith(
        compareByDescending<TeamStats> { it.pts }
            .thenByDescending { it.dg }
            .thenByDescending { it.gf }
    )
}

@Preview(showBackground = true)
@Composable
fun MundialPreview() {
    Mundial_2Theme {
        Scaffold(topBar = { Header() }) { innerPadding ->
            MundialScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}
