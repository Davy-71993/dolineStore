package com.example.doline.views.screens.store.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.doline.R
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Rounding
import com.example.doline.ui.theme.Spacing
import com.example.doline.ui.theme.onSuccessLight
import com.example.doline.ui.theme.successLight
import com.example.doline.views.components.AppText
import com.example.doline.views.components.TextType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(navController: NavController, viewModel: InboxScreenViewModel){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val storeId = viewModel.storeId
    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppText("Inbox", variant = TextType.Heading, maxLines = 1) },
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
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
            innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            // List implementation
            items(chatHeads.size) { index ->
                val chatHead = chatHeads[index]
                ChatListItem(
                    {navController.navigate("store/$storeId/inbox/${chatHead.client.id}")},
                    chatHead = chatHead
                )
            }
        }
    }
}

@Composable
fun ChatListItem(action: ()-> Unit, chatHead: ChatHead) {
    Row(
        modifier = Modifier
            .padding(horizontal = Spacing.SM, vertical = Spacing.XXS)
            .clickable { action() },
        horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(Rounding.FULL)
                )
        ){
            // Placeholder for avatar image
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(Spacing.MD)
                .padding(start = Spacing.SM)
        ) {
            Row {
                AppText(
                    text = chatHead.client.fullNames,
                    variant = TextType.Label,
                    modifier = Modifier.weight(1f)
                )
                AppText(
                    text = chatHead.lastMessage.sentAt,
                    variant = TextType.Label
                )
            }
            Row {
                AppText(
                    text = chatHead.lastMessage.text,
                    color = MaterialTheme.colorScheme.onBackground.copy(.6f),
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                if (chatHead.unreadMessages > 0){
                    AppText(
                        text = chatHead.unreadMessages.toString(),
                        color = onSuccessLight,
                        variant = TextType.Small,
                        modifier = Modifier
                            .padding(start = Spacing.SM)
                            .background(
                                successLight,
                                shape = RoundedCornerShape(Rounding.FULL)
                            )
                            .padding(horizontal = Spacing.SM, vertical = Spacing.XXS)
                    )
                }
            }
        }
    }
}

@HiltViewModel
class InboxScreenViewModel @Inject constructor(
    savedState: SavedStateHandle
): ViewModel(){
    val storeId = savedState.get<Long>("storeId")
}

data class ChatHead(
    val client: Client,
    val lastMessage: Message,
    val unreadMessages: Int
)

data class Client(
    val avatarUrl: String,
    val fullNames: String,
    val id: Int
)

data class Message(
    val text: String,
    val sentAt: String,
    val sender: Boolean,
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    SENT, DELIVERED, READ
}

val chatHeads = listOf<ChatHead>(
    ChatHead(
        client = Client("url", "John Doe", 1),
        lastMessage = Message("Hello, is this available?", "10:00 AM", false),
        unreadMessages = 2
    )
)
