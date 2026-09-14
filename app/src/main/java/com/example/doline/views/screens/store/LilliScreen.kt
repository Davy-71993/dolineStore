package com.example.doline.views.screens.store

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doline.R
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText
import com.example.doline.views.components.Screen
import com.example.doline.views.components.TextType
import com.example.doline.views.screens.store.home.ChatInput
import com.example.doline.views.screens.store.home.Message
import com.example.doline.views.screens.store.home.MessageBubble
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LilliScreen(viewModel: LilliScreenViewModel = hiltViewModel()) {
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    Screen(
        topAppBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ai),
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(IconSize.NORMAL)
                        )
                        Row(modifier = Modifier.padding(start = Spacing.SM)) {
                            AppText("Lilli", variant = TextType.Heading, maxLines = 1)
                        }
                    }
                },
                modifier = Modifier
                    .padding(vertical = 0.dp)
                    .shadow(10.dp),
                colors = TopAppBarColors(
                    containerColor = colorScheme.background,
                    scrolledContainerColor = colorScheme.background,
                    navigationIconContentColor = colorScheme.onBackground,
                    titleContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground,
                    subtitleContentColor = colorScheme.onBackground,
                ),
                actions = {
                    IconButton(onClick = { viewModel.clearConversation() }) {
                        Icon(
                            painter = painterResource(R.drawable.trash),
                            contentDescription = "Clear conversation",
                            modifier = Modifier.size(IconSize.NORMAL),
                            tint = colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(Spacing.MD),
                verticalArrangement = Arrangement.spacedBy(Spacing.SM)
            ) {
                items(messages) { message ->
                    MessageBubble(message = message)
                }
                if (isTyping) {
                    item {
                        AppText(
                            "Lilli is typing…",
                            variant = TextType.Small,
                            color = colorScheme.onBackground.copy(.6f)
                        )
                    }
                }
            }
            ChatInput(
                value = draft,
                onValueChange = { draft = it },
                onSend = {
                    if (draft.isNotBlank()) {
                        viewModel.sendMessage(draft)
                        draft = ""
                    }
                }
            )
            Spacer(Modifier.height(80.dp))
        }
    }

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
}

private val LILLI_REPLIES = listOf(
    "Let me look into that for you.",
    "I'll get back to you on that shortly.",
    "Got it — checking your store's data now.",
    "That's a great question! Let me pull up the details.",
    "I'm still learning, but I'll do my best to help with that."
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class LilliScreenViewModel @Inject constructor() : ViewModel() {
    private val _messages = MutableStateFlow(
        listOf(
            Message(
                text = "Hi, I'm Lilli — ask me anything about your store's sales, inventory, or orders.",
                sentAt = nowLabel(),
                sender = false
            )
        )
    )
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        _messages.value = _messages.value + Message(text = trimmed, sentAt = nowLabel(), sender = true)

        viewModelScope.launch {
            _isTyping.value = true
            delay(900)
            _messages.value = _messages.value + Message(
                text = LILLI_REPLIES.random(),
                sentAt = nowLabel(),
                sender = false
            )
            _isTyping.value = false
        }
    }

    fun clearConversation() {
        _messages.value = listOf(
            Message(
                text = "Hi, I'm Lilli — ask me anything about your store's sales, inventory, or orders.",
                sentAt = nowLabel(),
                sender = false
            )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun nowLabel(): String {
    return LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))
}
