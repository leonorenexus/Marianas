package com.leonoretech.marianas.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leonoretech.marianas.data.db.ConfigEntity
import com.leonoretech.marianas.data.db.MarianasDatabase
import com.leonoretech.marianas.data.db.MessageEntity
import com.leonoretech.marianas.data.db.SessionEntity
import com.leonoretech.marianas.data.repository.ApiException
import com.leonoretech.marianas.data.repository.ChatMessage
import com.leonoretech.marianas.data.repository.ChatRepository
import com.leonoretech.marianas.data.repository.FormatStyle
import com.leonoretech.marianas.data.repository.ImagePathsJson
import com.leonoretech.marianas.data.repository.ImageStore
import com.leonoretech.marianas.data.repository.Provider
import com.leonoretech.marianas.data.repository.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Local-only ViewModel. All chat history, sessions, and provider config live
 * in Room on-device — there is no cloud backend, sync, or login of any kind.
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MarianasDatabase.getInstance(application)
    private val chatRepository = ChatRepository()
    private val imageStore = ImageStore(application)

    // ---------------------------------------------------------------
    // App-level loading screen state
    // ---------------------------------------------------------------
    private val _isAppReady = MutableStateFlow(false)
    val isAppReady: StateFlow<Boolean> = _isAppReady.asStateFlow()

    // ---------------------------------------------------------------
    // Local login gate (NOT a real auth system — just a simple access
    // gate with a fixed username/password, checked fresh on every app open)
    // ---------------------------------------------------------------
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    fun attemptLogin(username: String, password: String) {
        if (username == "Dragonic" && password == "Leonore") {
            _loginError.value = null
            _isLoggedIn.value = true
        } else {
            _loginError.value = "Username atau password salah."
        }
    }

    // ---------------------------------------------------------------
    // Chat screen state
    // ---------------------------------------------------------------
    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    private val _statusText = MutableStateFlow("siap")
    private val _pendingImagePaths = MutableStateFlow<List<String>>(emptyList())
    private val _streamingContent = MutableStateFlow<String?>(null)
    private val _messagesForCurrentSession = MutableStateFlow<List<MessageEntity>>(emptyList())
    private val _activeProviderLabel = MutableStateFlow("no model set")

    val chatState: StateFlow<ChatScreenState> = combine(
        _messagesForCurrentSession, _isSending, _statusText, _pendingImagePaths, _streamingContent, _activeProviderLabel
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val messages = values[0] as List<MessageEntity>
        val sending = values[1] as Boolean
        val status = values[2] as String
        val pendingImages = values[3] as List<String>
        val streaming = values[4] as String?
        val providerLabel = values[5] as String

        val uiMessages = messages.map { it.toChatUiMessage() }.toMutableList()
        if (sending && streaming != null) {
            uiMessages.add(
                ChatUiMessage(id = -1L, role = MessageRole.ASSISTANT, content = streaming, isStreaming = true)
            )
        }
        ChatScreenState(
            messages = uiMessages,
            isSending = sending,
            statusText = status,
            activeProviderLabel = providerLabel,
            pendingImagePaths = pendingImages
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ChatScreenState())

    // ---------------------------------------------------------------
    // Provider config dashboard state
    // ---------------------------------------------------------------
    private val _fallbackEnabled = MutableStateFlow(true)
    private val _connectionTestResult = MutableStateFlow<ConnectionTestResult>(ConnectionTestResult.Idle)
    private val _configEntity = MutableStateFlow(ConfigEntity())

    val configState: StateFlow<ConfigScreenState> = combine(
        _configEntity, _fallbackEnabled, _connectionTestResult
    ) { cfg, fallback, testResult ->
        cfg.toConfigScreenState(fallbackEnabled = fallback).copy(connectionTestResult = testResult)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ConfigScreenState())

    // ---------------------------------------------------------------
    // Data & Sesi dashboard state (local session history only)
    // ---------------------------------------------------------------
    private val _allSessions = MutableStateFlow<List<SessionEntity>>(emptyList())

    val dataState: StateFlow<DataScreenState> = combine(_allSessions, _currentSessionId) { sessions, activeId ->
        DataScreenState(sessions = sessions.map { it.toSessionUiItem(activeId) })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DataScreenState())

    init {
        viewModelScope.launch { observeConfig() }
        observeSessions()
        viewModelScope.launch {
            ensureInitialSession()
            _isAppReady.value = true
        }
    }

    private suspend fun observeConfig() {
        db.configDao().observe().collectLatest { cfg ->
            val resolved = cfg ?: ConfigEntity().also { db.configDao().upsert(it) }
            _configEntity.value = resolved
            _activeProviderLabel.value = activeModelLabel(resolved)
        }
    }

    private fun observeSessions() {
        viewModelScope.launch {
            db.sessionDao().observeAll().collectLatest { sessions ->
                _allSessions.value = sessions
            }
        }
    }

    private suspend fun ensureInitialSession() {
        val sessions = db.sessionDao().getAll()
        if (sessions.isEmpty()) {
            createNewSession()
        } else {
            switchSession(sessions.first().id)
        }
    }

    private fun activeModelLabel(cfg: ConfigEntity): String = when (Provider.valueOf(cfg.activeProvider.uppercase())) {
        Provider.OPENROUTER -> cfg.openrouterModel.ifBlank { "no model set" }
        Provider.GROQ -> cfg.groqModel.ifBlank { "no model set" }
        Provider.GOOGLE -> cfg.googleModel.ifBlank { "no model set" }
        Provider.CUSTOM1 -> cfg.custom1Model.ifBlank { "no model set" }
        Provider.CUSTOM2 -> cfg.custom2Model.ifBlank { "no model set" }
        Provider.CUSTOM3 -> cfg.custom3Model.ifBlank { "no model set" }
    }

    // =========================================================
    // Session management
    // =========================================================

    fun createNewSession() {
        viewModelScope.launch {
            val session = SessionEntity(
                id = "sess_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
                title = "Obrolan Baru",
                createdAt = System.currentTimeMillis()
            )
            db.sessionDao().upsert(session)
            switchSession(session.id)
        }
    }

    fun switchSession(sessionId: String) {
        _currentSessionId.value = sessionId
        viewModelScope.launch {
            db.messageDao().observeForSession(sessionId).collectLatest { messages ->
                if (_currentSessionId.value == sessionId) {
                    _messagesForCurrentSession.value = messages
                }
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            db.sessionDao().deleteById(sessionId) // CASCADE also removes its messages
            val remaining = db.sessionDao().getAll()
            if (_currentSessionId.value == sessionId) {
                if (remaining.isNotEmpty()) {
                    switchSession(remaining.first().id)
                } else {
                    createNewSession()
                }
            }
        }
    }

    private suspend fun updateSessionTitleIfNeeded(sessionId: String, firstUserMessage: String) {
        val session = db.sessionDao().getAll().find { it.id == sessionId } ?: return
        if (session.title == "Obrolan Baru") {
            db.sessionDao().update(session.copy(title = firstUserMessage.take(40).ifBlank { "Obrolan" }))
        }
    }

    // =========================================================
    // Sending messages
    // =========================================================

    fun addPendingImage(filePath: String) {
        if (_pendingImagePaths.value.size >= MAX_IMAGES_PER_MESSAGE) {
            _statusText.value = "Maksimal $MAX_IMAGES_PER_MESSAGE foto per pesan"
            return
        }
        _pendingImagePaths.value = _pendingImagePaths.value + filePath
    }

    fun removePendingImage(filePath: String) {
        imageStore.delete(filePath)
        _pendingImagePaths.value = _pendingImagePaths.value.filter { it != filePath }
    }

    fun persistAndAttachImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val path = imageStore.persistPickedImage(uri)
                addPendingImage(path)
            } catch (e: Exception) {
                _statusText.value = "Gagal membaca gambar: ${e.message}"
            }
        }
    }

    fun sendMessage(text: String) {
        val sessionId = _currentSessionId.value ?: return
        val images = _pendingImagePaths.value
        if (text.isBlank() && images.isEmpty()) return

        val config = _configEntity.value
        val providerConfig = config.toProviderConfig()

        if (!providerConfig.hasValidAuth() || providerConfig.activeModel().isBlank()) {
            viewModelScope.launch {
                persistAssistantError(
                    sessionId,
                    "Konfigurasi belum lengkap. Buka dashboard Provider dan isi API key + Model ID dulu, lalu simpan."
                )
            }
            return
        }

        viewModelScope.launch {
            val displayText = text.ifBlank { "(foto)" }
            val userMessage = MessageEntity(
                sessionId = sessionId,
                role = "user",
                content = displayText,
                imagePathsJson = ImagePathsJson.encode(images),
                timestamp = System.currentTimeMillis()
            )
            db.messageDao().insert(userMessage)
            updateSessionTitleIfNeeded(sessionId, displayText)

            _pendingImagePaths.value = emptyList()
            _isSending.value = true
            _statusText.value = "menghubungi model..."
            _streamingContent.value = ""

            try {
                val history = db.messageDao().getForSession(sessionId)
                val messages = mutableListOf<ChatMessage>()
                if (config.systemPrompt.isNotBlank()) {
                    messages.add(ChatMessage(role = Role.SYSTEM, content = config.systemPrompt))
                }
                history.forEach { entity ->
                    messages.add(entity.toChatMessage { path -> imageStore.toAttachedImage(path) })
                }

                val finalText = if (_fallbackEnabled.value) {
                    val (usedProvider, flow) = chatRepository.sendMessageWithFallback(providerConfig, messages)
                    var last = ""
                    flow.collectLatest { chunk ->
                        last = chunk
                        _streamingContent.value = chunk
                    }
                    if (usedProvider != providerConfig.activeProvider) {
                        _statusText.value = "dibalas via ${usedProvider.name.lowercase()} (fallback)"
                    }
                    last
                } else {
                    var last = ""
                    chatRepository.sendMessage(providerConfig, messages).collectLatest { chunk ->
                        last = chunk
                        _streamingContent.value = chunk
                    }
                    last
                }

                db.messageDao().insert(
                    MessageEntity(sessionId = sessionId, role = "assistant", content = finalText, timestamp = System.currentTimeMillis())
                )
                if (_statusText.value.startsWith("menghubungi")) {
                    _statusText.value = "siap"
                }
            } catch (e: Exception) {
                persistAssistantError(sessionId, "Gagal mendapat respons: ${e.message ?: "unknown error"}")
                _statusText.value = "error"
            } finally {
                _isSending.value = false
                _streamingContent.value = null
            }
        }
    }

    private suspend fun persistAssistantError(sessionId: String, message: String) {
        db.messageDao().insert(
            MessageEntity(sessionId = sessionId, role = "assistant", content = message, isError = true, timestamp = System.currentTimeMillis())
        )
    }

    // =========================================================
    // Provider config dashboard actions
    // =========================================================

    fun setActiveProvider(provider: Provider) {
        viewModelScope.launch {
            db.configDao().upsert(_configEntity.value.copy(activeProvider = provider.name.lowercase()))
        }
    }

    fun setFallbackEnabled(enabled: Boolean) {
        _fallbackEnabled.value = enabled
    }

    fun saveProviderConfig(
        openrouterKey: String, openrouterModel: String,
        groqKey: String, groqModel: String,
        googleKey: String, googleModel: String,
        custom1: CustomSlotUiState, custom2: CustomSlotUiState, custom3: CustomSlotUiState,
        systemPrompt: String, streamEnabled: Boolean, timeoutSeconds: Int
    ) {
        viewModelScope.launch {
            val cfg = _configEntity.value
            db.configDao().upsert(
                cfg.copy(
                    openrouterKey = openrouterKey,
                    openrouterModel = openrouterModel,
                    groqKey = groqKey,
                    groqModel = groqModel,
                    googleKey = googleKey,
                    googleModel = googleModel.ifBlank { "gemini-3.5-flash" },
                    custom1Name = custom1.name, custom1Url = custom1.url, custom1Key = custom1.key,
                    custom1Model = custom1.model, custom1FormatStyle = if (custom1.formatStyle == FormatStyle.GEMINI) "gemini" else "openai",
                    custom2Name = custom2.name, custom2Url = custom2.url, custom2Key = custom2.key,
                    custom2Model = custom2.model, custom2FormatStyle = if (custom2.formatStyle == FormatStyle.GEMINI) "gemini" else "openai",
                    custom3Name = custom3.name, custom3Url = custom3.url, custom3Key = custom3.key,
                    custom3Model = custom3.model, custom3FormatStyle = if (custom3.formatStyle == FormatStyle.GEMINI) "gemini" else "openai",
                    systemPrompt = systemPrompt,
                    streamEnabled = streamEnabled,
                    timeoutSeconds = timeoutSeconds
                )
            )
        }
    }

    fun testConnection() {
        val config = _configEntity.value.toProviderConfig()
        viewModelScope.launch {
            _connectionTestResult.value = ConnectionTestResult.Testing
            try {
                val pingMessage = listOf(ChatMessage(role = Role.USER, content = "ping"))
                val nonStreamingConfig = config.copy(streamEnabled = false)
                var result = ""
                chatRepository.sendMessage(nonStreamingConfig, pingMessage).collectLatest { result = it }
                _connectionTestResult.value = ConnectionTestResult.Success("Terhubung! Respons: ${result.take(60)}")
            } catch (e: ApiException) {
                _connectionTestResult.value = ConnectionTestResult.Failure(e.message ?: "Gagal terhubung")
            } catch (e: Exception) {
                _connectionTestResult.value = ConnectionTestResult.Failure(e.message ?: "Gagal terhubung")
            }
        }
    }

    // =========================================================
    // Data management
    // =========================================================

    fun wipeAllData() {
        viewModelScope.launch {
            db.messageDao().clearAll()
            db.sessionDao().clearAll()
            db.configDao().clear()
            db.configDao().upsert(ConfigEntity())
            createNewSession()
        }
    }

    companion object {
        private const val MAX_IMAGES_PER_MESSAGE = 4
    }
}
