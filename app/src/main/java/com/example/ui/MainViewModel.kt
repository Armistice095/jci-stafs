package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiException
import com.example.data.api.JciApiRepository
import com.example.data.model.AgentProfile
import com.example.data.model.CheckinResultStatus
import com.example.data.model.Counters
import com.example.data.model.Event
import com.example.data.model.ScanHistoryItem
import com.example.data.model.ScanResponse
import com.example.util.DateFormats
import com.example.util.FeedbackManager
import com.example.util.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class Screen {
  WELCOME,
  LOGIN,
  EVENT_SELECT,
  SCANNER,
  HISTORY
}

data class ScanResultUiState(
  val status: CheckinResultStatus,
  val title: String,
  val subtitle: String? = null,
  val attendeeName: String? = null,
  val category: String? = null,
  val ticketNumber: String? = null,
  val firstCheckedInAt: String? = null,
  val firstCheckedInBy: String? = null,
  val reason: String? = null,
  val otherEventTitle: String? = null,
  val otherEventDate: String? = null,
  val elapsedLabel: String? = null
)

data class MainUiState(
  val currentScreen: Screen = Screen.WELCOME,
  val agent: AgentProfile? = null,
  val events: List<Event> = emptyList(),
  val selectedEvent: Event? = null,
  val counters: Counters? = null,
  val isOnline: Boolean = true,
  val isTorchOn: Boolean = false,
  val soundEnabled: Boolean = true,
  val vibrationEnabled: Boolean = true,
  val recentScans: List<ScanHistoryItem> = emptyList(),
  val activeResult: ScanResultUiState? = null,
  val isProcessingScan: Boolean = false,
  val showManualInputDialog: Boolean = false,
  val showMenuDialog: Boolean = false,
  val loginError: String? = null,
  val isLoggingIn: Boolean = false,
  val isLoadingEvents: Boolean = false,
  val eventsError: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

  val repository = JciApiRepository(application)
  private val feedbackManager = FeedbackManager(application)
  private val networkMonitor = NetworkMonitor(application)

  private val _uiState = MutableStateFlow(
    MainUiState(
      soundEnabled = repository.soundEnabled,
      vibrationEnabled = repository.vibrationEnabled
    )
  )
  val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

  // Anti-doublon throttle: 3 seconds for identical code
  private var lastScannedText: String? = null
  private var lastScannedTimestamp: Long = 0L

  // Idempotency key preserved for retries on NETWORK_ERROR
  private var currentIdempotencyKey: String = UUID.randomUUID().toString()
  private var pendingToken: String? = null
  private var pendingTicketNumber: String? = null

  private var autoDismissJob: Job? = null
  private var statsRefreshJob: Job? = null

  init {
    viewModelScope.launch {
      networkMonitor.isOnline.collect { online ->
        _uiState.update { it.copy(isOnline = online) }
      }
    }

    if (repository.authToken != null) {
      checkExistingSession()
    }
  }

  private fun checkExistingSession() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoggingIn = true) }
      val result = repository.getMe()
      val agent = result.getOrNull()
      if (agent != null && agent.canScan) {
        _uiState.update { it.copy(agent = agent, isLoggingIn = false) }
        loadEvents(autoSelectLast = true)
        return@launch
      }
      val code = (result.exceptionOrNull() as? ApiException)?.code ?: 0
      if (agent != null || code == 401 || code == 403) {
        // Session invalide ou droit retiré : on se déconnecte proprement.
        repository.logout()
        _uiState.update {
          it.copy(
            isLoggingIn = false,
            loginError = if (agent != null || code == 403) {
              "Votre compte n'est pas autorisé à contrôler les entrées."
            } else {
              "Votre session a expiré. Reconnectez-vous."
            }
          )
        }
      } else {
        // Pas de réseau au démarrage : on garde la session, l'agent peut réessayer.
        _uiState.update { it.copy(isLoggingIn = false, loginError = "Pas de connexion. Réessayez.") }
      }
    }
  }

  fun login(email: String, password: String) {
    if (email.isBlank() || password.isBlank()) {
      _uiState.update { it.copy(loginError = "Veuillez renseigner vos identifiants") }
      return
    }

    if (!_uiState.value.isOnline) {
      _uiState.update { it.copy(loginError = "Pas de connexion internet. Réessayez.") }
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isLoggingIn = true, loginError = null) }
      val result = repository.login(email.trim(), password)
      if (result.isSuccess) {
        val agent = result.getOrNull()
        if (agent != null && agent.canScan) {
          _uiState.update {
            it.copy(
              agent = agent,
              isLoggingIn = false,
              loginError = null
            )
          }
          loadEvents(autoSelectLast = true)
        } else {
          _uiState.update {
            it.copy(
              isLoggingIn = false,
              loginError = "Votre compte n'est pas autorisé à contrôler les entrées."
            )
          }
          repository.logout()
        }
      } else {
        _uiState.update {
          it.copy(
            isLoggingIn = false,
            loginError = result.exceptionOrNull()?.message ?: "E-mail ou mot de passe incorrect."
          )
        }
      }
    }
  }

  fun loadEvents(autoSelectLast: Boolean = false) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoadingEvents = true, eventsError = null) }
      val result = repository.getEvents()
      if (result.isSuccess) {
        val list = result.getOrDefault(emptyList())
        val lastId = repository.lastSelectedEventId
        val lastEvent = if (autoSelectLast && lastId != null) {
          list.firstOrNull { it.id == lastId }
        } else null

        if (lastEvent != null) {
          _uiState.update { it.copy(events = list) }
          selectEvent(lastEvent)
        } else {
          _uiState.update {
            it.copy(
              events = list,
              isLoadingEvents = false,
              currentScreen = Screen.EVENT_SELECT
            )
          }
        }
      } else {
        val error = result.exceptionOrNull() as? ApiException
        if (error?.code == 401) {
          logout("Votre session a expiré. Reconnectez-vous.")
        } else {
          _uiState.update {
            it.copy(
              isLoadingEvents = false,
              eventsError = if (error == null || error.code == 0) {
                "Impossible de charger les événements. Vérifiez la connexion."
              } else {
                error.message ?: "Impossible de charger les événements."
              },
              currentScreen = Screen.EVENT_SELECT
            )
          }
        }
      }
    }
  }

  fun selectEvent(event: Event) {
    repository.lastSelectedEventId = event.id
    _uiState.update {
      it.copy(
        selectedEvent = event,
        counters = null,
        currentScreen = Screen.SCANNER,
        isLoadingEvents = false
      )
    }
    fetchStats(event.id)
    startPeriodicStats(event.id)
  }

  private fun fetchStats(eventId: String) {
    viewModelScope.launch {
      val res = repository.getEventStats(eventId)
      if (res.isSuccess) {
        val counters = res.getOrNull()
        _uiState.update { it.copy(counters = counters) }
      }
    }
  }

  private fun startPeriodicStats(eventId: String) {
    statsRefreshJob?.cancel()
    statsRefreshJob = viewModelScope.launch {
      while (true) {
        delay(30_000)
        if (_uiState.value.currentScreen == Screen.SCANNER && _uiState.value.isOnline) {
          fetchStats(eventId)
        }
      }
    }
  }

  fun onQrCodeDetected(rawQr: String) {
    if (!_uiState.value.isOnline) {
      return
    }

    val now = System.currentTimeMillis()
    if (rawQr == lastScannedText && (now - lastScannedTimestamp) < 3000) {
      return
    }
    lastScannedText = rawQr
    lastScannedTimestamp = now

    val token = repository.extractQrToken(rawQr)
    if (token == null) {
      showInvalidQrResult("Ce code n'est pas un billet JCI valide")
      return
    }

    currentIdempotencyKey = UUID.randomUUID().toString()
    pendingToken = token
    pendingTicketNumber = null
    processScan(token = token, ticketNumber = null, key = currentIdempotencyKey)
  }

  fun onManualTicketSubmitted(ticketNumber: String) {
    val cleanNumber = ticketNumber.trim().uppercase()
    if (cleanNumber.isBlank()) return

    _uiState.update { it.copy(showManualInputDialog = false) }

    if (!_uiState.value.isOnline) {
      showNetworkErrorResult()
      return
    }

    currentIdempotencyKey = UUID.randomUUID().toString()
    pendingToken = null
    pendingTicketNumber = cleanNumber
    processScan(token = null, ticketNumber = cleanNumber, key = currentIdempotencyKey)
  }

  fun retryPendingScan() {
    if (_uiState.value.selectedEvent == null) return
    if (!_uiState.value.isOnline) {
      showNetworkErrorResult()
      return
    }
    processScan(token = pendingToken, ticketNumber = pendingTicketNumber, key = currentIdempotencyKey)
  }

  private fun processScan(token: String?, ticketNumber: String?, key: String) {
    val event = _uiState.value.selectedEvent ?: return

    viewModelScope.launch {
      _uiState.update { it.copy(isProcessingScan = true) }

      val result = repository.scanTicket(
        eventId = event.id,
        qrToken = token,
        ticketNumber = ticketNumber,
        idempotencyKey = key
      )

      _uiState.update { it.copy(isProcessingScan = false) }

      val response = result.getOrNull()
      if (response != null) {
        handleScanResponse(response)
      } else {
        val error = result.exceptionOrNull() as? ApiException
        when (error?.code) {
          401 -> logout("Votre session a expiré. Reconnectez-vous.")
          403 -> showNetworkErrorResult("Vous n'avez pas le droit de contrôler cet événement.")
          404 -> showNetworkErrorResult("Événement introuvable. Changez d'événement.")
          null, 0 -> showNetworkErrorResult()
          else -> showNetworkErrorResult("Le serveur ne répond pas. Rescannez dans un instant.")
        }
      }
    }
  }

  private fun handleScanResponse(response: ScanResponse) {
    val status = response.status
    if (status == CheckinResultStatus.NETWORK_ERROR) {
      // Résultat inconnu : on ne devine jamais.
      showNetworkErrorResult("Réponse inattendue du serveur. Rescannez.")
      return
    }

    val ticket = response.ticket
    val firstHour = DateFormats.hour(response.firstCheckedInAt).takeIf { it.isNotBlank() }

    val resultUi = when (status) {
      CheckinResultStatus.ADMITTED -> ScanResultUiState(
        status = status,
        title = "BILLET\nVALIDE",
        attendeeName = ticket?.holderName,
        category = ticket?.category,
        ticketNumber = ticket?.number
      )
      CheckinResultStatus.ALREADY_USED -> ScanResultUiState(
        status = status,
        title = "DÉJÀ\nSCANNÉ",
        attendeeName = ticket?.holderName,
        category = ticket?.category,
        ticketNumber = ticket?.number,
        firstCheckedInAt = firstHour,
        firstCheckedInBy = response.firstCheckedInBy,
        elapsedLabel = DateFormats.elapsedLabel(response.firstCheckedInAt)
      )
      CheckinResultStatus.WRONG_EVENT -> ScanResultUiState(
        status = status,
        title = "AUTRE\nÉVÉNEMENT",
        otherEventTitle = ticket?.eventTitle,
        ticketNumber = ticket?.number
      )
      CheckinResultStatus.CANCELLED -> ScanResultUiState(
        status = status,
        title = "BILLET\nANNULÉ",
        attendeeName = ticket?.holderName,
        category = ticket?.category,
        ticketNumber = ticket?.number,
        reason = response.reason
      )
      CheckinResultStatus.EVENT_NOT_ACTIVE -> ScanResultUiState(
        status = status,
        title = "ÉVÉNEMENT\nNON ACTIF",
        subtitle = response.reason ?: "Cet événement est clôturé ou annulé."
      )
      else -> ScanResultUiState(
        status = CheckinResultStatus.INVALID,
        title = "BILLET\nINCONNU",
        subtitle = "Ce code n'est pas un billet JCI valide"
      )
    }

    displayResult(resultUi)

    response.counters?.let { updatedCounters ->
      _uiState.update { it.copy(counters = updatedCounters) }
    }

    val historyItem = ScanHistoryItem(
      id = UUID.randomUUID().toString(),
      timestamp = System.currentTimeMillis(),
      ticketNumber = ticket?.number ?: "",
      holderName = ticket?.holderName ?: when (status) {
        CheckinResultStatus.WRONG_EVENT -> "Autre événement"
        CheckinResultStatus.EVENT_NOT_ACTIVE -> "Événement non actif"
        else -> "Billet inconnu"
      },
      category = ticket?.category ?: "",
      status = resultUi.status,
      note = when (status) {
        CheckinResultStatus.ALREADY_USED -> if (firstHour != null) "Déjà scanné à $firstHour" else "Déjà scanné"
        CheckinResultStatus.CANCELLED -> "Billet annulé"
        CheckinResultStatus.INVALID -> "Code non reconnu"
        CheckinResultStatus.WRONG_EVENT -> ticket?.eventTitle ?: "Autre événement"
        CheckinResultStatus.EVENT_NOT_ACTIVE -> "Événement non actif"
        else -> null
      }
    )

    _uiState.update { state ->
      val newHistory = (listOf(historyItem) + state.recentScans).take(50)
      state.copy(recentScans = newHistory)
    }
  }

  private fun showInvalidQrResult(reason: String) {
    val resultUi = ScanResultUiState(
      status = CheckinResultStatus.INVALID,
      title = "BILLET\nINCONNU",
      subtitle = reason
    )
    displayResult(resultUi)

    val historyItem = ScanHistoryItem(
      id = UUID.randomUUID().toString(),
      timestamp = System.currentTimeMillis(),
      ticketNumber = "",
      holderName = "Billet inconnu",
      category = "",
      status = CheckinResultStatus.INVALID,
      note = "Code non reconnu"
    )
    _uiState.update { state ->
      val newHistory = (listOf(historyItem) + state.recentScans).take(50)
      state.copy(recentScans = newHistory)
    }
  }

  private fun showNetworkErrorResult(message: String = "Vérifiez la connexion et rescannez") {
    val resultUi = ScanResultUiState(
      status = CheckinResultStatus.NETWORK_ERROR,
      title = "VÉRIFICATION\nIMPOSSIBLE",
      subtitle = message
    )
    displayResult(resultUi)
  }

  private fun displayResult(resultUi: ScanResultUiState) {
    _uiState.update { it.copy(activeResult = resultUi) }

    feedbackManager.notifyResult(
      status = resultUi.status,
      soundEnabled = _uiState.value.soundEnabled,
      vibrationEnabled = _uiState.value.vibrationEnabled,
      scope = viewModelScope
    )

    autoDismissJob?.cancel()
    // Auto-dismiss in 1.5s for normal statuses; for NETWORK_ERROR, stay until user action
    if (resultUi.status != CheckinResultStatus.NETWORK_ERROR) {
      autoDismissJob = viewModelScope.launch {
        delay(1500)
        _uiState.update {
          if (it.activeResult == resultUi) it.copy(activeResult = null) else it
        }
      }
    }
  }

  fun dismissActiveResult() {
    autoDismissJob?.cancel()
    _uiState.update { it.copy(activeResult = null) }
  }

  fun toggleTorch() {
    _uiState.update { it.copy(isTorchOn = !it.isTorchOn) }
  }

  fun toggleSound() {
    val newValue = !_uiState.value.soundEnabled
    repository.soundEnabled = newValue
    _uiState.update { it.copy(soundEnabled = newValue) }
  }

  fun toggleVibration() {
    val newValue = !_uiState.value.vibrationEnabled
    repository.vibrationEnabled = newValue
    _uiState.update { it.copy(vibrationEnabled = newValue) }
  }

  fun setManualInputDialogVisible(visible: Boolean) {
    _uiState.update { it.copy(showManualInputDialog = visible) }
  }

  fun setMenuDialogVisible(visible: Boolean) {
    _uiState.update { it.copy(showMenuDialog = visible) }
  }

  fun navigateTo(screen: Screen) {
    _uiState.update { it.copy(currentScreen = screen) }
  }

  fun logout(message: String? = null) {
    repository.logout()
    statsRefreshJob?.cancel()
    autoDismissJob?.cancel()
    _uiState.update {
      it.copy(
        currentScreen = if (message != null) Screen.LOGIN else Screen.WELCOME,
        agent = null,
        events = emptyList(),
        eventsError = null,
        selectedEvent = null,
        counters = null,
        recentScans = emptyList(),
        activeResult = null,
        showMenuDialog = false,
        showManualInputDialog = false,
        isLoadingEvents = false,
        isLoggingIn = false,
        loginError = message
      )
    }
  }

  override fun onCleared() {
    super.onCleared()
    feedbackManager.release()
    networkMonitor.unregister()
    statsRefreshJob?.cancel()
    autoDismissJob?.cancel()
  }
}
