package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.JoeyPersona
import com.example.data.local.ChatMessageEntity
import com.example.ui.theme.CodeBlockDarkBg
import com.example.ui.theme.CodeBlockHeaderBg
import com.example.ui.theme.JoeyAmberAccent
import com.example.ui.theme.JoeyCyanGlow
import com.example.ui.theme.JoeyIndigoLight
import com.example.ui.theme.JoeyIndigoPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onLikeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "user"
    val persona = JoeyPersona.fromId(message.personaId)
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("chat_message_${message.id}"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            JoeyAvatar(
                size = 36.dp,
                emoji = persona.emoji,
                isSpeaking = isSpeaking,
                modifier = Modifier.padding(top = 2.dp, end = 8.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Sender name & time label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isUser) "You" else persona.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUser) JoeyIndigoLight else JoeyCyanGlow
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                if (message.isEncrypted) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted with AES-256",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            // Message Bubble
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isUser) 18.dp else 4.dp,
                    topEnd = if (isUser) 4.dp else 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                ),
                color = if (isUser) {
                    JoeyIndigoPrimary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                tonalElevation = if (isUser) 4.dp else 1.dp,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = if (isUser) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(
                            topStart = if (isUser) 18.dp else 4.dp,
                            topEnd = if (isUser) 4.dp else 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    RenderMessageContent(
                        content = message.content,
                        isUser = isUser,
                        onCopyCode = onCopy
                    )
                }
            }

            // NLU Insights breakdown
            if (message.nluIntent.isNotBlank() || !isUser) {
                Spacer(modifier = Modifier.height(4.dp))
                NluInsightsCard(
                    intentName = if (message.nluIntent.isNotBlank()) message.nluIntent else "UNCERTAIN_GENERAL",
                    entitiesSummary = message.nluEntities,
                    sentimentOrComplexity = message.nluSentiment,
                    rawContent = message.content,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }

            // Action toolbar for Bot messages
            if (!isUser) {
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // TTS Audio Speaker Button
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("speak_button_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isSpeaking) "Stop Speaking" else "Speak Message",
                            tint = if (isSpeaking) JoeyCyanGlow else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Copy Message
                    IconButton(
                        onClick = { onCopy(message.content) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("copy_button_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = { onShare(message.content) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("share_button_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Like / Thumbs Up
                    IconButton(
                        onClick = onLikeToggle,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("like_button_${message.id}")
                    ) {
                        Icon(
                            imageVector = if (message.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Thumbs up",
                            tint = if (message.isLiked) JoeyAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    if (message.checksum.isNotBlank()) {
                        Text(
                            text = "SHA: ${message.checksum.take(6)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RenderMessageContent(
    content: String,
    isUser: Boolean,
    onCopyCode: (String) -> Unit
) {
    val codeBlockRegex = Regex("```([a-zA-Z0-9_-]*)\\n([\\s\\S]*?)```")
    val matches = codeBlockRegex.findAll(content).toList()

    if (matches.isEmpty()) {
        // Plain or basic markdown text
        FormattedText(
            text = content,
            textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
        )
    } else {
        var lastIndex = 0
        matches.forEach { match ->
            val range = match.range
            if (range.first > lastIndex) {
                val preText = content.substring(lastIndex, range.first).trim()
                if (preText.isNotEmpty()) {
                    FormattedText(
                        text = preText,
                        textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            val language = match.groupValues.getOrNull(1)?.ifBlank { "code" } ?: "code"
            val code = match.groupValues.getOrNull(2) ?: ""

            CodeBlockCard(
                language = language,
                code = code,
                onCopy = { onCopyCode(code) }
            )
            Spacer(modifier = Modifier.height(8.dp))

            lastIndex = range.last + 1
        }

        if (lastIndex < content.length) {
            val postText = content.substring(lastIndex).trim()
            if (postText.isNotEmpty()) {
                FormattedText(
                    text = postText,
                    textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun FormattedText(
    text: String,
    textColor: Color
) {
    // Parse lightweight markdown headers & bolding
    val lines = text.lines()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            when {
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = JoeyCyanGlow
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## "),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = JoeyCyanGlow
                    )
                }
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# "),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = JoeyCyanGlow
                    )
                }
                line.startsWith("• ") || line.startsWith("- ") -> {
                    Row(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = "•",
                            fontWeight = FontWeight.Bold,
                            color = JoeyIndigoLight,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        val itemContent = if (line.startsWith("• ")) line.removePrefix("• ") else line.removePrefix("- ")
                        Text(
                            text = buildAnnotatedInlineMarkdown(itemContent, textColor),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
                else -> {
                    Text(
                        text = buildAnnotatedInlineMarkdown(line, textColor),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}

fun buildAnnotatedInlineMarkdown(raw: String, defaultColor: Color) = buildAnnotatedString {
    var cursor = 0
    val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
    val italicRegex = Regex("\\*(.*?)\\*")
    val inlineCodeRegex = Regex("`([^`]+)`")

    val allTokens = mutableListOf<Triple<IntRange, String, String>>() // Range, Type, Value

    boldRegex.findAll(raw).forEach { allTokens.add(Triple(it.range, "bold", it.groupValues[1])) }
    inlineCodeRegex.findAll(raw).forEach { allTokens.add(Triple(it.range, "code", it.groupValues[1])) }

    allTokens.sortBy { it.first.first }

    var lastPos = 0
    allTokens.forEach { token ->
        if (token.first.first >= lastPos) {
            if (token.first.first > lastPos) {
                withStyle(SpanStyle(color = defaultColor)) {
                    append(raw.substring(lastPos, token.first.first))
                }
            }
            when (token.second) {
                "bold" -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                        append(token.third)
                    }
                }
                "code" -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = JoeyCyanGlow,
                            background = Color(0x33000000)
                        )
                    ) {
                        append(" ${token.third} ")
                    }
                }
            }
            lastPos = token.first.last + 1
        }
    }

    if (lastPos < raw.length) {
        withStyle(SpanStyle(color = defaultColor)) {
            append(raw.substring(lastPos))
        }
    }
}

@Composable
fun CodeBlockCard(
    language: String,
    code: String,
    onCopy: () -> Unit
) {
    var isCopied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CodeBlockDarkBg)
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CodeBlockHeaderBg)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(JoeyCyanGlow)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = language.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = JoeyCyanGlow
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        onCopy()
                        isCopied = true
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = if (isCopied) JoeyCyanGlow else Color(0xFF94A3B8),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isCopied) "Copied" else "Copy",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = if (isCopied) JoeyCyanGlow else Color(0xFF94A3B8)
                )
            }
        }

        // Code Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = code.trim(),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = Color(0xFFE2E8F0)
            )
        }
    }
}
