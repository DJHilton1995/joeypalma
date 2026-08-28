package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ai.JoeyPersona

@Composable
fun QuickPromptRow(
    persona: JoeyPersona,
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val prompts = when (persona) {
        JoeyPersona.TECH_ARCHITECT -> listOf(
            "🎵 Compose Cyberpunk Synthwave track",
            "🦀 Explain RustyMCP Hardened Protocol",
            "🛡️ How X25519 & ChaCha20Poly1305 defend MCP",
            "⚡ Kotlin Coroutines vs Threads",
            "📱 Jetpack Compose best practices"
        )
        JoeyPersona.DEEP_THINKER -> listOf(
            "🌌 Generate Deep Space Ambient soundscape",
            "🧠 Break down first-principles thinking",
            "⚖️ Ethical implications of Artificial General Intelligence",
            "🔍 Analyze zero-trust network defenses"
        )
        JoeyPersona.CREATIVE_SPARK -> listOf(
            "⚡ Compose 8-Bit Retro Chiptune arcade music",
            "✨ Pitch a high-tech sci-fi mystery",
            "🍕 Tell me a witty Joey Tribbiani joke",
            "🚀 Viral startup slogan generator"
        )
        JoeyPersona.STUDY_PROD -> listOf(
            "☕ Generate Lo-Fi Chillhop study beats",
            "🎯 Create a 5-step daily focus plan",
            "📚 Summarize active recall methods",
            "⚡ 2-minute productivity hacks"
        )
        JoeyPersona.CLASSIC -> listOf(
            "🎵 Compose a Cyberpunk Synthwave beat!",
            "😎 How you doin'?",
            "🦀 How does the RustyMCP server keep our chats safe?",
            "🍕 What's Joey's favorite meal?",
            "🚀 What can you help me build today?"
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        prompts.forEachIndexed { index, prompt ->
            AssistChip(
                onClick = { onSelectPrompt(prompt) },
                label = {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ),
                border = AssistChipDefaults.assistChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    borderWidth = 1.dp,
                    enabled = true
                ),
                modifier = Modifier.testTag("quick_prompt_chip_$index")
            )
        }
    }
}
