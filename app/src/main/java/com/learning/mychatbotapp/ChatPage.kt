package com.learning.mychatbotapp

import android.os.SystemClock
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learning.mychatbotapp.ui.theme.AccentBlueLight
import com.learning.mychatbotapp.ui.theme.ColorModelMessage
import com.learning.mychatbotapp.ui.theme.ColorUserMessage
import com.learning.mychatbotapp.ui.theme.TextOnDark
import kotlinx.coroutines.delay

@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    onHomeClick: () -> Unit,
    ttsMuted: Boolean = false,
    onToggleTts: (() -> Unit)? = null
) {

    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) {
        AppHeader(onHomeClick = onHomeClick, ttsMuted = ttsMuted, onToggleTts = onToggleTts)
        ModelStatusBanner(state = viewModel.modelState)
        MessageList(
            modifier = modifier.weight(1f),
            messageList = viewModel.messageList
        )
        MessageInput(
            enabled = viewModel.modelState is ModelState.Ready,
            onMessageSend = {
                viewModel.sendMessage(it)
            })
    }
}

@Composable
fun ModelStatusBanner(state: ModelState) {
    when (state) {
        is ModelState.Checking -> Text(
            modifier = Modifier.padding(12.dp),
            text = stringResource(R.string.checking_model),
            color = MaterialTheme.colorScheme.onBackground
        )
        is ModelState.Downloading -> Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.downloading_model, (state.progress * 100).toInt()),
                color = MaterialTheme.colorScheme.onBackground
            )
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                color = AccentBlueLight
            )
        }
        is ModelState.Error -> Text(
            modifier = Modifier.padding(12.dp),
            text = state.message,
            color = MaterialTheme.colorScheme.error
        )
        is ModelState.Ready -> Unit
    }
}

@Composable
fun LiveSecondsCounter(startTime: Long, modifier: Modifier = Modifier) {
    val elapsedMs by produceState(initialValue = 0L) {
        while (true) {
            value = SystemClock.elapsedRealtime() - startTime
            delay(100)
        }
    }
    Text(
        text = stringResource(R.string.timer_format, elapsedMs / 1000.0),
        modifier = modifier,
        fontSize = 11.sp,
        color = TextOnDark.copy(alpha = 0.7f)
    )
}


@Composable
fun MessageList(modifier: Modifier = Modifier, messageList: List<MessageModel>) {
    if (messageList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.robot_face_logo),
                contentDescription = stringResource(R.string.assistant),
                modifier = Modifier.size(72.dp)
            )
            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = stringResource(R.string.ask_anything),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    } else {
        LazyColumn(
            modifier = modifier, reverseLayout = true
        ) {
            items(messageList.reversed()) {
                MessageRow(it)
            }
        }
    }

}


@Composable
fun MessageRow(messageModel: MessageModel) {

    val isModel = messageModel.role == "model"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isModel) 8.dp else 64.dp,
                end = if (isModel) 64.dp else 8.dp,
                top = 12.dp,
                bottom = 12.dp
            ),
        horizontalArrangement = if (isModel) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        if (isModel) {
            Image(
                painter = painterResource(id = R.drawable.robot_face_logo),
                contentDescription = stringResource(R.string.assistant),
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isModel) ColorModelMessage else ColorUserMessage)
                .border(
                    width = 1.dp,
                    color = if (isModel) TextOnDark.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                SelectionContainer {
                    Text(
                        text = messageModel.message,
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        color = TextOnDark
                    )
                }
                if (isModel && messageModel.message == "Escribiendo..." && messageModel.startedAtMs > 0) {
                    LiveSecondsCounter(
                        startTime = messageModel.startedAtMs,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                    )
                } else if (isModel && messageModel.elapsedMs > 0) {
                    Text(
                        text = stringResource(R.string.timer_format, messageModel.elapsedMs / 1000.0),
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                        fontSize = 11.sp,
                        color = TextOnDark.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}


@Composable
fun MessageInput(enabled: Boolean = true, onMessageSend: (String) -> Unit) {

    var message by remember {
        mutableStateOf("")
    }
    Row(
        modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = message,
            enabled = enabled,
            onValueChange = { message = it },
            placeholder = { Text(stringResource(R.string.direct_question), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = AccentBlueLight,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        IconButton(
            enabled = enabled,
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(CircleShape)
                .background(if (enabled) AccentBlueLight else MaterialTheme.colorScheme.surfaceVariant),
            onClick = {
                if (message.isNotEmpty()) {
                    onMessageSend(message)
                    message = ""
                }
            }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.send), tint = Color.White)
        }

    }
}


@Composable
fun AppHeader(
    onHomeClick: (() -> Unit)? = null,
    ttsMuted: Boolean = false,
    onToggleTts: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onHomeClick != null) {
            IconButton(onClick = onHomeClick) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = stringResource(R.string.go_back_home),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Text(
            modifier = Modifier.weight(1f).padding(16.dp),
            text = stringResource(R.string.app_header_title),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        if (onToggleTts != null) {
            IconButton(onClick = onToggleTts) {
                Icon(
                    imageVector = if (ttsMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = if (ttsMuted) {
                        stringResource(R.string.enable_voice)
                    } else {
                        stringResource(R.string.mute_voice)
                    },
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
