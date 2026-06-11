package com.example.mundial_2

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mundial_2.ui.theme.Mundial_2Theme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "mundial_scores")

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Play startup sound
        playStartupSound()

        setContent {
            Mundial_2Theme {
                MundialApp()
            }
        }
    }

    private fun playStartupSound() {
        try {
            // We use the ID R.raw.inicio_mundial. 
            // The user must place the file in res/raw/inicio_mundial.mp3
            mediaPlayer = MediaPlayer.create(this, R.raw.inicio_mundial)
            mediaPlayer?.start()
            
            // Auto stop after 6 seconds if it's longer
            mediaPlayer?.setOnCompletionListener {
                releaseMediaPlayer()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }
}

@Immutable
data class Team(val id: String, val name: String, val flag: String)

@Immutable
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

@Immutable
data class Group(val name: String, val teams: List<Team>, val matches: List<Match>)

object WorldCupData {
    val groups = listOf(
        createGroup("A", "México", "🇲🇽", "Sudáfrica", "🇿🇦", "Corea del Sur", "🇰🇷", "Chequia", "🇨🇿"),
        createGroup("B", "Canadá", "🇨🇦", "Bosnia", "🇧🇦", "Qatar", "🇶🇦", "Suiza", "🇨🇭"),
        createGroup("C", "Brasil", "🇧🇷", "Marruecos", "🇲🇦", "Haití", "🇭🇹", "Escocia", "🏴󠁧󠁢󠁳󠁣󠁴󠁿"),
        createGroup("D", "USA", "🇺🇸", "Paraguay", "🇵🇾", "Australia", "🇦🇺", "Turquía", "🇹🇷"),
        createGroup("E", "Alemania", "🇩🇪", "Curazao", "🇨🇼", "Costa de Marfil", "🇨🇮", "Ecuador", "🇪🇨"),
        createGroup("F", "Países Bajos", "🇳🇱", "Japón", "🇯🇵", "Suecia", "🇸🇪", "Túnez", "🇹🇳"),
        createGroup("G", "Bélgica", "🇧🇪", "Egipto", "🇪🇬", "Irán", "🇮🇷", "Nueva Zelanda", "🇳🇿"),
        createGroup("H", "España", "🇪🇸", "Cabo Verde", "🇨🇻", "Arabia Saudita", "🇸🇦", "Uruguay", "🇺🇾"),
        createGroup("I", "Francia", "🇫🇷", "Senegal", "🇸🇳", "Irak", "🇮🇶", "Noruega", "🇳🇴"),
        createGroup("J", "Argentina", "🇦🇷", "Argelia", "🇩🇿", "Austria", "🇦🇹", "Jordania", "🇯🇴"),
        createGroup("K", "Portugal", "🇵🇹", "RD Congo", "🇨🇩", "Uzbekistán", "🇺🇿", "Colombia", "🇨🇴"),
        createGroup("L", "Inglaterra", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Croacia", "🇭🇷", "Ghana", "🇬🇭", "Panamá", "🇵🇦")
    )

    private fun createGroup(name: String, t1n: String, t1f: String, t2n: String, t2f: String, t3n: String, t3f: String, t4n: String, t4f: String): Group {
        val teams = listOf(
            Team("${name}_1", t1n, t1f),
            Team("${name}_2", t2n, t2f),
            Team("${name}_3", t3n, t3f),
            Team("${name}_4", t4n, t4f)
        )
        val matches = listOf(
            Match(1, teams[0], teams[1]),
            Match(2, teams[2], teams[3]),
            Match(3, teams[0], teams[2]),
            Match(4, teams[1], teams[3]),
            Match(5, teams[3], teams[0]),
            Match(6, teams[2], teams[1])
        )
        return Group(name, teams, matches)
    }
}

@Composable
fun MundialApp() {
    var selectedGroupIndex by remember { mutableIntStateOf(0) }
    val selectedGroup = remember(selectedGroupIndex) { WorldCupData.groups[selectedGroupIndex] }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { Header(selectedGroup.name) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            GroupSelector(
                selectedIndex = selectedGroupIndex,
                onGroupSelected = { selectedGroupIndex = it }
            )
            MundialScreen(group = selectedGroup)
        }
    }
}

@Composable
fun GroupSelector(selectedIndex: Int, onGroupSelected: (Int) -> Unit) {
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 16.dp,
        containerColor = Color(0xFF0D1B3E),
        contentColor = Color.White,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = Color(0xFFFF9800)
            )
        }
    ) {
        WorldCupData.groups.forEachIndexed { index, group ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onGroupSelected(index) },
                text = { Text("Grupo ${group.name}", fontSize = 12.sp) }
            )
        }
    }
}

@Composable
fun Header(groupName: String) {
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
                text = "GRUPO $groupName",
                color = Color(0xFFFF9800),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MundialScreen(group: Group) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Optimized DataStore observation
    val savedScoresFlow = remember(group.name) {
        context.dataStore.data.map { prefs ->
            group.matches.associate { match ->
                val localKey = "match_${group.name}_${match.id}_local"
                val visitorKey = "match_${group.name}_${match.id}_visitor"
                match.id to (Pair(prefs[stringPreferencesKey(localKey)] ?: "", prefs[stringPreferencesKey(visitorKey)] ?: ""))
            }
        }
    }
    val savedScores by savedScoresFlow.collectAsState(initial = emptyMap())

    // Derived state for matches to minimize recomposition
    val matches = remember(group, savedScores) {
        group.matches.map { match ->
            val scores = savedScores[match.id] ?: Pair("", "")
            match.copy(localScore = scores.first, visitorScore = scores.second)
        }
    }

    // Derived state for standings calculation
    val standings = remember(matches) {
        calculateStandings(group.teams, matches)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 10.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PARTIDOS DEL GRUPO ${group.name}",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
        )

        matches.forEach { match ->
            key(match.id) {
                MatchCard(
                    match = match,
                    onScoreChange = { localScore, visitorScore ->
                        scope.launch {
                            context.dataStore.edit { prefs ->
                                prefs[stringPreferencesKey("match_${group.name}_${match.id}_local")] = localScore
                                prefs[stringPreferencesKey("match_${group.name}_${match.id}_visitor")] = visitorScore
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
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
            key(stats.team.id) {
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
            val localStats = statsMap[match.local.id] ?: return@forEach
            val visitorStats = statsMap[match.visitor.id] ?: return@forEach

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
        MundialApp()
    }
}
