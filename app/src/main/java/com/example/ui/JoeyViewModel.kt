package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.JoeyPersona
import java.io.File
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.JoeyDatabase
import com.example.data.repository.JoeyRepository
import com.example.music.AudioSynthEngine
import com.example.music.GeneratedMusicTrack
import com.example.music.MusicGenre
import com.example.music.MusicIntelligenceEngine
import com.example.music.MusicPlayer
import com.example.security.SecureVault
import com.example.security.SecurityReport
import com.example.speech.JoeySpeaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JoeyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JoeyDatabase.getInstance(application)
    private val repository = JoeyRepository(db.chatDao())
    val speaker = JoeySpeaker(application)
    val musicPlayer = MusicPlayer(viewModelScope)

    // Current Session ID
    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    // Current Persona
    private val _currentPersona = MutableStateFlow(JoeyPersona.CLASSIC)
    val currentPersona: StateFlow<JoeyPersona> = _currentPersona.asStateFlow()

    // Input text
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Loading / Thinking state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Search query for history
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Custom API key override (stored in runtime memory securely)
    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Dialog visibilities
    private val _showSecurityDialog = MutableStateFlow(false)
    val showSecurityDialog: StateFlow<Boolean> = _showSecurityDialog.asStateFlow()

    private val _showPersonaDialog = MutableStateFlow(false)
    val showPersonaDialog: StateFlow<Boolean> = _showPersonaDialog.asStateFlow()

    private val _showNluDialog = MutableStateFlow(false)
    val showNluDialog: StateFlow<Boolean> = _showNluDialog.asStateFlow()

    // Music Studio Dialog Visibility
    private val _showMusicStudioDialog = MutableStateFlow(false)
    val showMusicStudioDialog: StateFlow<Boolean> = _showMusicStudioDialog.asStateFlow()

    // Music Generation Status
    private val _isGeneratingMusic = MutableStateFlow(false)
    val isGeneratingMusic: StateFlow<Boolean> = _isGeneratingMusic.asStateFlow()

    // Generated Music Library
    private val _generatedTracks = MutableStateFlow<List<GeneratedMusicTrack>>(emptyList())
    val generatedTracks: StateFlow<List<GeneratedMusicTrack>> = _generatedTracks.asStateFlow()

    // Security report
    val securityReport: StateFlow<SecurityReport> = MutableStateFlow(SecureVault.getSecurityReport()).asStateFlow()

    // All sessions
    val sessions: StateFlow<List<ChatSessionEntity>> = repository.getSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active session messages
    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    init {
        viewModelScope.launch {
            // Auto-load or create first session
            repository.getSessions().collect { list ->
                if (list.isEmpty()) {
                    val newId = repository.createNewSession("How you doin'?", _currentPersona.value)
                    _currentSessionId.value = newId
                } else if (_currentSessionId.value == null) {
                    _currentSessionId.value = list.first().id
                    _currentPersona.value = JoeyPersona.fromId(list.first().personaId)
                }
            }
        }

        viewModelScope.launch {
            _currentSessionId.collect { sessionId ->
                if (sessionId != null) {
                    repository.getMessages(sessionId).collect { msgList ->
                        _messages.value = msgList
                    }
                } else {
                    _messages.value = emptyList()
                }
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
    }

    fun openSecurityDialog(show: Boolean) {
        _showSecurityDialog.value = show
    }

    fun openPersonaDialog(show: Boolean) {
        _showPersonaDialog.value = show
    }

    fun openNluDialog(show: Boolean) {
        _showNluDialog.value = show
    }

    fun openMusicStudio(show: Boolean) {
        _showMusicStudioDialog.value = show
        if (show && _generatedTracks.value.isEmpty()) {
            // Generate a default Cyberpunk Synthwave track if library is empty
            generateMusic("Cyberpunk Synthwave with driving 16th bass and neon lead", MusicGenre.SYNTHWAVE)
        }
    }

    fun generateMusic(prompt: String, genre: MusicGenre? = null) {
        if (_isGeneratingMusic.value) return
        _isGeneratingMusic.value = true
        speaker.stop()

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val track = MusicIntelligenceEngine.composeFromPrompt(prompt, genre)
                withContext(Dispatchers.Main) {
                    _generatedTracks.value = listOf(track) + _generatedTracks.value
                    _isGeneratingMusic.value = false
                    musicPlayer.playTrack(track)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _isGeneratingMusic.value = false
                    Toast.makeText(getApplication(), "Music composition failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun playMusicTrack(track: GeneratedMusicTrack) {
        speaker.stop()
        musicPlayer.playTrack(track)
    }

    fun toggleMusicPlayPause() {
        musicPlayer.togglePlayPause()
    }

    fun stopMusic() {
        musicPlayer.stop()
    }

    fun exportMusicWav(track: GeneratedMusicTrack) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val exportsDir = File(context.cacheDir, "exports")
                if (!exportsDir.exists()) exportsDir.mkdirs()

                val safeTitle = track.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(25)
                val wavFile = File(exportsDir, "JoeyMusic_${safeTitle}_${track.genre.name}.wav")
                AudioSynthEngine.saveToWavFile(wavFile, track.audioDataPcm16, track.sampleRate)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Saved WAV: ${wavFile.name} (${wavFile.length() / 1024} KB)", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Failed to export WAV", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun shareMusicTrack(track: GeneratedMusicTrack) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val exportsDir = File(context.cacheDir, "exports")
                if (!exportsDir.exists()) exportsDir.mkdirs()

                val safeTitle = track.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(25)
                val wavFile = File(exportsDir, "JoeyMusic_${safeTitle}_${track.genre.name}.wav")
                AudioSynthEngine.saveToWavFile(wavFile, track.audioDataPcm16, track.sampleRate)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    wavFile
                )

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Joey AI Music: ${track.title}")
                    type = "audio/wav"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }

                val shareIntent = Intent.createChooser(sendIntent, "Share Generated Music (WAV)").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(shareIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Failed to share WAV audio", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun selectPersona(persona: JoeyPersona) {
        _currentPersona.value = persona
        _showPersonaDialog.value = false
    }

    fun selectSession(sessionId: Long, personaId: String) {
        speaker.stop()
        _currentSessionId.value = sessionId
        _currentPersona.value = JoeyPersona.fromId(personaId)
    }

    fun createNewChat() {
        viewModelScope.launch {
            speaker.stop()
            val id = repository.createNewSession("New Chat", _currentPersona.value)
            _currentSessionId.value = id
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            if (_currentSessionId.value == sessionId) {
                speaker.stop()
                _currentSessionId.value = null
            }
            repository.deleteSession(sessionId)
        }
    }

    fun togglePinSession(sessionId: Long, isPinned: Boolean) {
        viewModelScope.launch {
            repository.setPinned(sessionId, !isPinned)
        }
    }

    fun renameSession(sessionId: Long, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.updateSessionTitle(sessionId, newTitle)
        }
    }

    fun sendMessage(promptText: String? = null) {
        val textToSend = (promptText ?: _inputText.value).trim()
        if (textToSend.isBlank() || _isGenerating.value) return

        val sessionId = _currentSessionId.value ?: return

        _inputText.value = ""
        _isGenerating.value = true

        viewModelScope.launch {
            val history = _messages.value.map { it.sender to it.content }
            repository.sendMessage(
                sessionId = sessionId,
                userPrompt = textToSend,
                persona = _currentPersona.value,
                history = history,
                customApiKey = _customApiKey.value
            )
            _isGenerating.value = false
        }
    }

    fun toggleMessageLiked(messageId: Long, currentLiked: Boolean) {
        viewModelScope.launch {
            repository.setMessageLiked(messageId, !currentLiked)
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun speakMessage(messageId: Long, content: String) {
        speaker.speak(messageId, content)
    }

    fun stopSpeaking() {
        speaker.stop()
    }

    fun copyToClipboard(content: String, label: String = "Joey AI Message") {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun shareMessage(content: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share via Joey AI").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(shareIntent)
    }

    fun exportChatTranscript(): String {
        val session = sessions.value.find { it.id == _currentSessionId.value }
        val title = session?.title ?: "Joey AI Chat Transcript"
        val sb = StringBuilder()
        sb.append("# $title\n\n")
        sb.append("*Generated with Joey AI Chatbot • Persona: ${_currentPersona.value.displayName}*\n\n")
        sb.append("---\n\n")

        for (msg in _messages.value) {
            val senderLabel = if (msg.sender == "user") "👤 **You**" else "😎 **Joey AI**"
            sb.append("$senderLabel:\n")
            sb.append("${msg.content}\n\n")
        }
        return sb.toString()
    }

    fun shareTranscriptAsFile(content: String) {
        try {
            val context = getApplication<Application>()
            val session = sessions.value.find { it.id == _currentSessionId.value }
            val rawTitle = session?.title ?: "Transcript"
            val safeTitle = rawTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
            
            val exportsDir = File(context.cacheDir, "exports")
            if (!exportsDir.exists()) {
                exportsDir.mkdirs()
            }
            
            val file = File(exportsDir, "Joey_AI_$safeTitle.txt")
            file.writeText(content)
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Joey AI Transcript")
                type = "text/plain"
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val shareIntent = Intent.createChooser(sendIntent, "Export Transcript as File").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(getApplication(), "Failed to export file", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            speaker.stop()
            repository.clearAllData()
            val newId = repository.createNewSession("How you doin'?", JoeyPersona.CLASSIC)
            _currentSessionId.value = newId
        }
    }

    override fun onCleared() {
        super.onCleared()
        speaker.shutdown()
        musicPlayer.release()
    }
}
