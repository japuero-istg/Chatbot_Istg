package com.learning.mychatbotapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.learning.mychatbotapp.ui.theme.MyChatBotAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var ttsManager: TtsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = ChatViewModel.Factory(applicationContext)
        val viewmodel = ViewModelProvider(this, factory)[ChatViewModel::class.java]
        ttsManager = TtsManager(this)
        observeSpeech(viewmodel)
        setContent {
            MyChatBotAppTheme {
                var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        is AppScreen.Splash -> SplashScreen(
                            modifier = Modifier.padding(innerPadding),
                            onContinue = { currentScreen = AppScreen.Menu }
                        )

                        is AppScreen.Menu -> MenuScreen(
                            modifier = Modifier.padding(innerPadding),
                            categories = viewmodel.categories,
                            onCategoryClick = { category ->
                                viewmodel.sendMessage(category.sampleQuestion)
                                currentScreen = AppScreen.Chat
                            },
                            onDirectQuestion = { question ->
                                viewmodel.sendMessage(question)
                                currentScreen = AppScreen.Chat
                            }
                        )

                        is AppScreen.Chat -> ChatPage(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewmodel,
                            onHomeClick = { currentScreen = AppScreen.Menu },
                            ttsMuted = ttsManager.muted,
                            onToggleTts = { ttsManager.toggleMuted() }
                        )
                    }
                }
            }
        }
    }

    private fun observeSpeech(viewModel: ChatViewModel) {
        lifecycleScope.launch {
            viewModel.ttsEvents.collect { event ->
                when (event) {
                    is TtsEvent.Speak -> ttsManager.speak(event.text)
                    TtsEvent.Stop -> ttsManager.stop()
                }
            }
        }
    }

    override fun onDestroy() {
        ttsManager.shutdown()
        super.onDestroy()
    }
}
