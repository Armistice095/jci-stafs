package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.screens.EventSelectScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.DesignScale
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        val uiState by viewModel.uiState.collectAsState()

        Surface(modifier = Modifier.fillMaxSize()) {
          AppNavigation(uiState = uiState, viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun AppNavigation(
  uiState: MainUiState,
  viewModel: MainViewModel
) {
  AnimatedContent(
    targetState = uiState.currentScreen,
    transitionSpec = {
      fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
    },
    label = "screen_transition"
  ) { targetScreen ->
    when (targetScreen) {
      Screen.WELCOME -> {
        WelcomeScreen(
          onNavigateToLogin = { viewModel.navigateTo(Screen.LOGIN) }
        )
      }

      Screen.LOGIN -> {
        BackHandler {
          viewModel.navigateTo(Screen.WELCOME)
        }
        LoginScreen(
          uiState = uiState,
          viewModel = viewModel,
          onNavigateBack = { viewModel.navigateTo(Screen.WELCOME) }
        )
      }

      Screen.EVENT_SELECT -> {
        DesignScale {
          EventSelectScreen(uiState = uiState, viewModel = viewModel)
        }
      }

      Screen.SCANNER -> {
        BackHandler {
          viewModel.navigateTo(Screen.EVENT_SELECT)
        }
        DesignScale {
          ScannerScreen(uiState = uiState, viewModel = viewModel)
        }
      }

      Screen.HISTORY -> {
        BackHandler {
          viewModel.navigateTo(Screen.SCANNER)
        }
        DesignScale {
          HistoryScreen(uiState = uiState, viewModel = viewModel)
        }
      }
    }
  }
}
