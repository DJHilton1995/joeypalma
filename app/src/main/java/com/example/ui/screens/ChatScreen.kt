package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.JoeyViewModel
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.JoeyAvatar
import com.example.ui.components.QuickPromptRow
import com.example.ui.components.RetroGridBackground
import com.example.ui.components.SecurityBadge
import com.example.ui.components.TypingIndicator
import com.example.ui.theme.JoeyCyanGlow
import com.example.ui.theme.JoeyIndigoLight
import com.example.ui.theme.JoeyIndigoPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: JoeyViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val currentPersona by viewModel.currentPersona.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val showSecurityDialog by viewModel.showSecurityDialog.collectAsStateWithLifecycle()
    val showPersonaDialog by viewModel.showPersonaDialog.collectAsStateWithLifecycle()
    val showNluDialog by viewModel.showNluDialog.collectAsStateWithLifecycle()
    val showMusicStudioDialog by viewModel.showMusicStudioDialog.collectAsStateWithLifecycle()
    val isGeneratingMusic by viewModel.isGeneratingMusic.collectAsStateWithLifecycle()
    val generatedTracks by viewModel.generatedTracks.collectAsStateWithLifecycle()
    val isPlayingMusic by viewModel.musicPlayer.isPlaying.collectAsStateWithLifecycle()
    val currentMusicTrack by viewModel.musicPlayer.currentTrack.collectAsStateWithLifecycle()
    val musicPlaybackProgress by viewModel.musicPlayer.playbackProgress.collectAsStateWithLifecycle()
    val musicCurrentPositionSec by viewModel.musicPlayer.currentPositionSec.collectAsStateWithLifecycle()
    val visualizerAmplitudes by viewModel.musicPlayer.visualizerAmplitudes.collectAsStateWithLifecycle()
    val securityReport by viewModel.securityReport.collectAsStateWithLifecycle()

    val isSpeaking by viewModel.speaker.isSpeaking.collectAsStateWithLifecycle()
    val currentlySpeakingId by viewModel.speaker.currentlySpeakingMessageId.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            val targetIndex = if (isGenerating) messages.size else messages.size - 1
            listState.animateScrollToItem(targetIndex)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerSessionsView(
                    sessions = sessions,
                    currentSessionId = currentSessionId,
                    searchQuery = searchQuery,
                    onSearchChanged = viewModel::onSearchQueryChanged,
                    onSelectSession = viewModel::selectSession,
                    onNewChat = viewModel::createNewChat,
                    onTogglePin = viewModel::togglePinSession,
                    onRenameSession = viewModel::renameSession,
                    onDeleteSession = viewModel::deleteSession,
                    onClearAll = viewModel::clearAllHistory,
                    onCloseDrawer = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("top_persona_selector")
                        ) {
                            JoeyAvatar(
                                size = 26.dp,
                                emoji = currentPersona.emoji,
                                isSpeaking = isSpeaking
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = currentPersona.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { viewModel.openPersonaDialog(true) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch Persona",
                                    tint = JoeyCyanGlow
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Joey\'s Memories",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        // Stop TTS Audio Button (if speaking)
                        if (isSpeaking) {
                            IconButton(
                                onClick = { viewModel.stopSpeaking() },
                                modifier = Modifier.testTag("stop_audio_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.StopCircle,
                                    contentDescription = "Stop Audio",
                                    tint = JoeyCyanGlow
                                )
                            }
                        }

                        // AI Music Studio Button
                        IconButton(
                            onClick = { viewModel.openMusicStudio(true) },
                            modifier = Modifier.testTag("music_studio_button")
                        ) {
                            Icon(
                                imageVector = if (isPlayingMusic) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                contentDescription = "AI Music Studio",
                                tint = if (isPlayingMusic) JoeyCyanGlow else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // NLU Engine Inspector Dialog Button
                        IconButton(
                            onClick = { viewModel.openNluDialog(true) },
                            modifier = Modifier.testTag("nlu_inspector_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "NLU Intelligence Engine",
                                tint = JoeyCyanGlow
                            )
                        }

                        // Export / Share transcript
                        IconButton(
                            onClick = {
                                val transcript = viewModel.exportChatTranscript()
                                viewModel.shareTranscriptAsFile(transcript)
                            },
                            modifier = Modifier.testTag("export_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export Transcript",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Security Shield Settings Dialog
                        IconButton(
                            onClick = { viewModel.openSecurityDialog(true) },
                            modifier = Modifier.testTag("security_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Security Settings",
                                tint = JoeyCyanGlow
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    // Security quick badge & prompt chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SecurityBadge(onClick = { viewModel.openSecurityDialog(true) })
                        Spacer(modifier = Modifier.width(6.dp))
                        QuickPromptRow(
                            persona = currentPersona,
                            onSelectPrompt = { prompt -> viewModel.sendMessage(prompt) }
                        )
                    }

                    // Input bar
                    ChatInputBar(
                        inputText = inputText,
                        onTextChanged = viewModel::onInputTextChanged,
                        onSendMessage = { viewModel.sendMessage() },
                        isGenerating = isGenerating,
                        persona = currentPersona,
                        onOpenPersonaSelector = { viewModel.openPersonaDialog(true) }
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Animated 80s Neon Grid Background with Purple & Cyan Glow
                RetroGridBackground(modifier = Modifier.fillMaxSize())

                if (messages.isEmpty()) {
                    EmptyChatHero(
                        persona = currentPersona,
                        onQuickStart = { prompt -> viewModel.sendMessage(prompt) }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messages, key = { it.id }) { message ->
                            ChatMessageItem(
                                message = message,
                                isSpeaking = isSpeaking && currentlySpeakingId == message.id,
                                onSpeak = { viewModel.speakMessage(message.id, message.content) },
                                onCopy = { content -> viewModel.copyToClipboard(content) },
                                onShare = { content -> viewModel.shareMessage(content) },
                                onLikeToggle = { viewModel.toggleMessageLiked(message.id, message.isLiked) }
                            )
                        }

                        if (isGenerating) {
                            item(key = "typing_indicator") {
                                TypingIndicator(persona = currentPersona)
                            }
                        }
                    }
                }
            }
        }
    }

    // Security Dialog
    if (showSecurityDialog) {
        SecurityDialog(
            report = securityReport,
            currentCustomKey = customApiKey,
            onSaveCustomKey = viewModel::setCustomApiKey,
            onDismiss = { viewModel.openSecurityDialog(false) }
        )
    }

    // Persona Selection Dialog
    if (showPersonaDialog) {
        PersonaSelectionDialog(
            currentPersona = currentPersona,
            onSelectPersona = viewModel::selectPersona,
            onDismiss = { viewModel.openPersonaDialog(false) }
        )
    }

    // NLU Intelligence Inspector Dialog
    if (showNluDialog) {
        NluInspectorDialog(
            onDismiss = { viewModel.openNluDialog(false) },
            onSendPrompt = { prompt -> viewModel.sendMessage(prompt) }
        )
    }

    // AI Music Studio Dialog
    if (showMusicStudioDialog) {
        MusicStudioDialog(
            isPlaying = isPlayingMusic,
            currentTrack = currentMusicTrack,
            playbackProgress = musicPlaybackProgress,
            currentPositionSec = musicCurrentPositionSec,
            visualizerAmplitudes = visualizerAmplitudes,
            isGeneratingMusic = isGeneratingMusic,
            generatedTracks = generatedTracks,
            onGenerateTrack = { prompt, genre -> viewModel.generateMusic(prompt, genre) },
            onTogglePlayPause = { viewModel.toggleMusicPlayPause() },
            onPlayTrack = { track -> viewModel.playMusicTrack(track) },
            onStopMusic = { viewModel.stopMusic() },
            onExportWav = { track -> viewModel.exportMusicWav(track) },
            onShareTrack = { track -> viewModel.shareMusicTrack(track) },
            onDismiss = { viewModel.openMusicStudio(false) }
        )
    }
}

@Composable
fun EmptyChatHero(
    persona: com.example.ai.JoeyPersona,
    onQuickStart: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        JoeyAvatar(size = 80.dp, emoji = persona.emoji)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to Joey AI",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = persona.welcomeMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🌟 Try asking:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = JoeyIndigoLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "🦀 How does Rust ensure memory safety without GC?",
                    "⚡ Explain Kotlin coroutines Structured Concurrency",
                    "😎 How you doin'?",
                    "🍕 What is Joey's golden rule about pizza?"
                ).forEach { suggestion ->
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .padding(bottom = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
