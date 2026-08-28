package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.music.GeneratedMusicTrack
import com.example.music.ModelTrustTier
import com.example.music.MusicAIModel
import com.example.music.MusicGenre
import com.example.music.MusicModelRegistry
import com.example.ui.components.MusicWaveformVisualizer
import com.example.ui.theme.JoeyCyanGlow
import com.example.ui.theme.JoeyIndigoLight
import com.example.ui.theme.JoeyIndigoPrimary

@Composable
fun MusicStudioDialog(
    isPlaying: Boolean,
    currentTrack: GeneratedMusicTrack?,
    playbackProgress: Float,
    currentPositionSec: Float,
    visualizerAmplitudes: List<Float>,
    isGeneratingMusic: Boolean,
    generatedTracks: List<GeneratedMusicTrack>,
    onGenerateTrack: (prompt: String, genre: MusicGenre?) -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayTrack: (GeneratedMusicTrack) -> Unit,
    onStopMusic: () -> Unit,
    onExportWav: (GeneratedMusicTrack) -> Unit,
    onShareTrack: (GeneratedMusicTrack) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var promptInput by remember { mutableStateOf("Cyberpunk Synthwave with neon arpeggios and punchy bass") }
    var selectedGenre by remember { mutableStateOf<MusicGenre?>(MusicGenre.SYNTHWAVE) }

    Dialog(
        onDismissRequest = {
            onStopMusic()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("music_studio_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(JoeyCyanGlow, JoeyIndigoPrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Music Studio",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Joey AI Music Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Cross-Referenced Open Source Neural & Synth Engine",
                                style = MaterialTheme.typography.labelSmall,
                                color = JoeyCyanGlow
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            onStopMusic()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("close_music_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = JoeyCyanGlow,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = JoeyCyanGlow
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Studio", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Model Hub", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tracks (${generatedTracks.size})", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Contents
                when (selectedTabIndex) {
                    0 -> StudioComposerTab(
                        promptInput = promptInput,
                        onPromptChanged = { promptInput = it },
                        selectedGenre = selectedGenre,
                        onGenreSelected = { selectedGenre = it },
                        isGenerating = isGeneratingMusic,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        playbackProgress = playbackProgress,
                        currentPositionSec = currentPositionSec,
                        visualizerAmplitudes = visualizerAmplitudes,
                        onGenerate = {
                            onGenerateTrack(promptInput, selectedGenre)
                        },
                        onTogglePlayPause = onTogglePlayPause,
                        onStop = onStopMusic,
                        onExportWav = onExportWav,
                        onShareTrack = onShareTrack
                    )
                    1 -> ModelRegistryTab()
                    2 -> TrackLibraryTab(
                        tracks = generatedTracks,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        onPlayTrack = onPlayTrack,
                        onTogglePlayPause = onTogglePlayPause,
                        onExportWav = onExportWav,
                        onShareTrack = onShareTrack
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioComposerTab(
    promptInput: String,
    onPromptChanged: (String) -> Unit,
    selectedGenre: MusicGenre?,
    onGenreSelected: (MusicGenre?) -> Unit,
    isGenerating: Boolean,
    currentTrack: GeneratedMusicTrack?,
    isPlaying: Boolean,
    playbackProgress: Float,
    currentPositionSec: Float,
    visualizerAmplitudes: List<Float>,
    onGenerate: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onExportWav: (GeneratedMusicTrack) -> Unit,
    onShareTrack: (GeneratedMusicTrack) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Prompt Input
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Describe Your Music Composition",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = JoeyIndigoLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = onPromptChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("music_prompt_input"),
                        placeholder = { Text("e.g. Chill Lo-fi study beat with mellow vinyl chords in F major...") },
                        minLines = 2,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JoeyCyanGlow,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Quick Genre Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MusicGenre.values().forEach { genre ->
                            FilterChip(
                                selected = selectedGenre == genre,
                                onClick = {
                                    onGenreSelected(if (selectedGenre == genre) null else genre)
                                    onPromptChanged("${genre.displayName} track: ${genre.description}")
                                },
                                label = {
                                    Text("${genre.emoji} ${genre.displayName}")
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JoeyCyanGlow.copy(alpha = 0.25f),
                                    selectedLabelColor = JoeyCyanGlow
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onGenerate,
                        enabled = !isGenerating && promptInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_music_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JoeyCyanGlow,
                            contentColor = Color.Black
                        )
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synthesizing Neural Tracks...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.MusicNote, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate & Compose Music", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Player & Visualizer Card
        if (currentTrack != null) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F172A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_music_player_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentTrack.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${currentTrack.genre.emoji} ${currentTrack.genre.displayName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JoeyCyanGlow,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "• ${currentTrack.bpm} BPM",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "• ${currentTrack.keyScale}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { onExportWav(currentTrack) },
                                    modifier = Modifier.testTag("export_wav_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Export WAV",
                                        tint = JoeyCyanGlow
                                    )
                                }
                                IconButton(
                                    onClick = { onShareTrack(currentTrack) },
                                    modifier = Modifier.testTag("share_music_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Audio File",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Waveform Visualizer
                        MusicWaveformVisualizer(
                            amplitudes = visualizerAmplitudes,
                            isPlaying = isPlaying
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress Slider & Timers
                        Slider(
                            value = playbackProgress,
                            onValueChange = {},
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = JoeyCyanGlow,
                                activeTrackColor = JoeyCyanGlow,
                                inactiveTrackColor = Color(0xFF334155)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val currentMin = (currentPositionSec / 60).toInt()
                            val currentSec = (currentPositionSec % 60).toInt()
                            val totalMin = (currentTrack.durationSeconds / 60).toInt()
                            val totalSec = (currentTrack.durationSeconds % 60).toInt()

                            Text(
                                text = String.format("%02d:%02d", currentMin, currentSec),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = String.format("%02d:%02d", totalMin, totalSec),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Playback Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onStop,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.StopCircle,
                                    contentDescription = "Stop",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            IconButton(
                                onClick = onTogglePlayPause,
                                modifier = Modifier
                                    .size(64.dp)
                                    .testTag("toggle_music_playback_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = JoeyCyanGlow,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Provenance footer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = JoeyCyanGlow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Model Lineage: ${currentTrack.aiModelProvenance}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFCBD5E1),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelRegistryTab() {
    val models = MusicModelRegistry.VERIFIED_MODELS

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("model_registry_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = JoeyCyanGlow.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = JoeyCyanGlow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Audited & Verified Open Source Model Database",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = JoeyCyanGlow
                        )
                        Text(
                            text = "Cross-referenced with arXiv academic literature, HuggingFace SafeTensors audit registries, and public domain audio datasets with zero copyright/security violations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(models) { model ->
            ModelCard(model = model)
        }
    }
}

@Composable
private fun ModelCard(model: MusicAIModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = model.organization,
                        style = MaterialTheme.typography.labelSmall,
                        color = JoeyIndigoLight,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(model.trustTier.badgeColorHex).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Score: ${model.securityScore}/100",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(model.trustTier.badgeColorHex)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = model.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetadataBadge("Params: ${model.parameterCount}")
                MetadataBadge("License: ${model.license}")
                MetadataBadge("Rate: ${model.audioSampleRate}")
                MetadataBadge("Arch: ${model.architectureType}")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Academic Paper Citation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .padding(8.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = JoeyCyanGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Academic Publication:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = JoeyCyanGlow
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = model.academicPaper,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun TrackLibraryTab(
    tracks: List<GeneratedMusicTrack>,
    currentTrack: GeneratedMusicTrack?,
    isPlaying: Boolean,
    onPlayTrack: (GeneratedMusicTrack) -> Unit,
    onTogglePlayPause: () -> Unit,
    onExportWav: (GeneratedMusicTrack) -> Unit,
    onShareTrack: (GeneratedMusicTrack) -> Unit
) {
    if (tracks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No Tracks Composed Yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Use the AI Studio tab to compose music from prompts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("track_library_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tracks) { track ->
                val isThisTrackPlaying = isPlaying && currentTrack?.id == track.id

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentTrack?.id == track.id)
                            JoeyIndigoPrimary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayTrack(track) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                if (currentTrack?.id == track.id) {
                                    onTogglePlayPause()
                                } else {
                                    onPlayTrack(track)
                                }
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (isThisTrackPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = "Play/Pause",
                                tint = JoeyCyanGlow,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "${track.genre.emoji} ${track.genre.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JoeyCyanGlow
                                )
                                Text(
                                    text = "• ${track.bpm} BPM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• ${track.durationSeconds.toInt()}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { onExportWav(track) }) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Save WAV",
                                    tint = JoeyCyanGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = { onShareTrack(track) }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
