package com.example.doline.views.screens.store.home


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.components.AppText
import com.example.doline.views.components.FormScreen
import com.example.doline.views.components.TextInputField
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(navController: NavController, viewModel: ChatScreenViewModel){

    val chatHead = chatHeads.find { head ->
        head.client.id == 1
    }

    if (chatHead == null){
        return
    }
    var messageText by remember { mutableStateOf("") }
    val messages = remember { 
        mutableStateListOf(
            Message("Hello, is this available?", "10:00 AM", false),
            Message("Yes, it is!", "10:05 AM", true)
        )
    }
    val listState = rememberLazyListState()

    FormScreen(
        appBar = {
            TopAppBar(
                title = { AppText(chatHead.client.fullNames, variant = TextType.Heading, maxLines = 1) },
                modifier = Modifier.padding(vertical = 0.dp),
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    subtitleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_left),
                            contentDescription = "Back",
                            modifier = Modifier.size(IconSize.NORMAL),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) {
        messages.forEach { m ->
            MessageBubble(message = m)
        }
        ChatInput(
            value = messageText,
            onValueChange = { messageText = it },
            onSend = {
                if (messageText.isNotBlank()) {
                    messages.add(Message(messageText, "Now", true))
                    messageText = ""
                }
            }
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()){
            listState.animateScrollToItem(messages.size - 1)
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val alignment = if (message.sender) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (message.sender) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.sender) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (message.sender) {
        RoundedCornerShape(Rounding.MD, Rounding.MD, 0.dp, Rounding.MD)
    } else {
        RoundedCornerShape(Rounding.MD, Rounding.MD, Rounding.MD, 0.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (message.sender) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = backgroundColor,
                shape = shape,
                modifier = Modifier.widthIn(max = (LocalWindowInfo.current.containerSize.width * 0.8).dp)
            ) {
                AppText(
                    text = message.text,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = Spacing.MD, vertical = Spacing.XS),
                    variant = TextType.Body
                )
            }
            AppText(
                text = message.sentAt,
                variant = TextType.Small,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = Spacing.XXS)
            )
        }
    }
}

@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .widthIn(100.dp)
            .fillMaxWidth()
            .padding(Spacing.SM)
            .consumeWindowInsets(WindowInsets.navigationBars)
            .imePadding(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(Spacing.XS)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            TextInputField(
                value = value,
                onValueChange = onValueChange,
                singleLine = false,
                placeHolder = "Type a message...",
            )
        }
        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Bottom)
                .background(
                    MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(Rounding.FULL)
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(IconSize.NORMAL)
            )
        }
    }
}

@HiltViewModel
class ChatScreenViewModel @Inject constructor(): ViewModel(){

}
