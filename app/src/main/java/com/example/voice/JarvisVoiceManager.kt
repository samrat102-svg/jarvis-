package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

const val JARVIS_SIGNATURE_GREETING = "Greetings. I've detected your presence and initialized all primary systems. My protocols are fully synchronized with your personal preferences. I'm actively monitoring all channels and ready to provide immediate assistance with any task you require."

enum class JarvisLanguage(
    val code: String,
    val displayName: String,
    val locale: Locale,
    val flag: String,
    val greeting: String
) {
    ENGLISH_UK(
        "en-GB",
        "English (UK Butler)",
        Locale.UK,
        "🇬🇧",
        JARVIS_SIGNATURE_GREETING
    ),
    HINDI(
        "hi-IN",
        "Hindi (हिन्दी)",
        Locale.forLanguageTag("hi-IN"),
        "🇮🇳",
        "नमस्ते सर। मैंने आपकी उपस्थिति पहचान ली है और सभी प्राथमिक प्रणालियां सक्रिय कर दी हैं। जारविस आपकी सेवा में हाज़िर है।"
    ),
    HINGLISH(
        "en-IN",
        "English (India / Hinglish)",
        Locale.forLanguageTag("en-IN"),
        "🇮🇳",
        "Greetings sir. All primary defense, communications, and telemetry systems online and ready for your directive."
    ),
    ENGLISH_US(
        "en-US",
        "English (US Tech)",
        Locale.US,
        "🇺🇸",
        "All systems nominal. Communications array, call screening, and document intelligence ready, sir."
    ),
    SPANISH(
        "es-ES",
        "Spanish (Español)",
        Locale.forLanguageTag("es-ES"),
        "🇪🇸",
        "Saludos, señor. Sistemas de Stark Industries en línea y a su completa disposición."
    ),
    FRENCH(
        "fr-FR",
        "French (Français)",
        Locale.FRANCE,
        "🇫🇷",
        "Bonjour monsieur. Tous les protocoles J.A.R.V.I.S. sont prêts pour vos directives."
    ),
    GERMAN(
        "de-DE",
        "German (Deutsch)",
        Locale.GERMANY,
        "🇩🇪",
        "Guten Tag, Sir. Primäre Verteidigungssysteme und Telemetrie sind voll einsatzbereit."
    ),
    JAPANESE(
        "ja-JP",
        "Japanese (日本語)",
        Locale.JAPAN,
        "🇯🇵",
        "こんにちは、ボス。すべてのシステムが正常に稼働しています。ご指示をどうぞ。"
    )
}

enum class JarvisVoicePersona(
    val id: String,
    val title: String,
    val subtitle: String,
    val defaultPitch: Float,
    val defaultSpeed: Float,
    val language: JarvisLanguage,
    val sampleText: String
) {
    JARVIS_CLASSIC(
        "classic",
        "J.A.R.V.I.S. (UK Butler)",
        "Paul Bettany iconic British butler cadence",
        0.80f,
        0.92f,
        JarvisLanguage.ENGLISH_UK,
        JARVIS_SIGNATURE_GREETING
    ),
    STARK_HINDI(
        "hindi",
        "जारविस (हिन्दी AI)",
        "Refined Indian AI voice with polite Hindi protocol",
        0.86f,
        0.96f,
        JarvisLanguage.HINDI,
        "नमस्ते सर, जारविस हमेशा आपकी सेवा में हाज़िर है।"
    ),
    FRIDAY(
        "friday",
        "F.R.I.D.A.Y. (Tactical)",
        "Crisp female Stark defense assistant",
        1.18f,
        1.05f,
        JarvisLanguage.ENGLISH_US,
        "Boss, Mark 85 armor diagnostics are locked and loaded."
    ),
    SUB_BASS_CORE(
        "cyber",
        "Sub-Bass Cyber Core",
        "Deep resonant metallic cybernetic frequency",
        0.70f,
        0.88f,
        JarvisLanguage.ENGLISH_UK,
        "Defense firewall active. Threat matrix clear."
    ),
    TECH_US(
        "tech_us",
        "Stark Tech Prime",
        "High-cadence modern tactical ops voice",
        0.96f,
        1.02f,
        JarvisLanguage.ENGLISH_US,
        "Satellite telemetry synced. Document intelligence ready."
    )
}

class JarvisVoiceManager(
    private val context: Context,
    private val onCommandRecognized: (String) -> Unit
) {

    companion object {
        const val SIGNATURE_GREETING = JARVIS_SIGNATURE_GREETING
    }

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    private val _requiresSystemDialog = MutableStateFlow(false)
    val requiresSystemDialog: StateFlow<Boolean> = _requiresSystemDialog.asStateFlow()

    // Voice customization states matched to signature audio: Pitch 0.88f, Speed 0.98f
    private val _currentLanguage = MutableStateFlow(JarvisLanguage.ENGLISH_UK)
    val currentLanguage: StateFlow<JarvisLanguage> = _currentLanguage.asStateFlow()

    private val _currentPersona = MutableStateFlow(JarvisVoicePersona.JARVIS_CLASSIC)
    val currentPersona: StateFlow<JarvisVoicePersona> = _currentPersona.asStateFlow()

    private val _voicePitch = MutableStateFlow(0.80f)
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _voiceSpeed = MutableStateFlow(0.92f)
    val voiceSpeed: StateFlow<Float> = _voiceSpeed.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<String>>(emptyList())
    val availableVoices: StateFlow<List<String>> = _availableVoices.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                applyVoiceSettings()

                // Query available voices
                try {
                    val voices = tts?.voices?.map { it.name } ?: emptyList()
                    _availableVoices.value = voices.take(10)
                } catch (e: Exception) {
                    // Ignore
                }

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    private fun applyVoiceSettings() {
        if (!isTtsInitialized || tts == null) return
        val lang = _currentLanguage.value
        tts?.language = lang.locale
        tts?.setPitch(_voicePitch.value)
        tts?.setSpeechRate(_voiceSpeed.value)
    }

    fun setLanguage(language: JarvisLanguage) {
        _currentLanguage.value = language
        applyVoiceSettings()
        speak(language.greeting)
    }

    fun applyPersona(persona: JarvisVoicePersona) {
        _currentPersona.value = persona
        _currentLanguage.value = persona.language
        _voicePitch.value = persona.defaultPitch
        _voiceSpeed.value = persona.defaultSpeed
        applyVoiceSettings()
        speak(persona.sampleText)
    }

    fun playSignatureGreeting() {
        _currentPersona.value = JarvisVoicePersona.JARVIS_CLASSIC
        _currentLanguage.value = JarvisLanguage.ENGLISH_UK
        _voicePitch.value = 0.82f
        _voiceSpeed.value = 0.95f
        applyVoiceSettings()
        speak(SIGNATURE_GREETING)
    }

    fun setVoicePitch(pitch: Float) {
        _voicePitch.value = pitch
        tts?.setPitch(pitch)
    }

    fun setVoiceSpeed(speed: Float) {
        _voiceSpeed.value = speed
        tts?.setSpeechRate(speed)
    }

    fun speak(text: String, flush: Boolean = true) {
        if (!isTtsInitialized || tts == null) {
            _isSpeaking.value = true
            return
        }
        applyVoiceSettings()
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, queueMode, null, "jarvis_speech_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun createSpeechRecognizerIntent(): Intent {
        val lang = _currentLanguage.value
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang.locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang.locale.toLanguageTag())
            putExtra(
                "android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES",
                arrayOf("hi-IN", "en-IN", "en-GB", "en-US", "es-ES", "fr-FR", "de-DE", "ja-JP")
            )
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                when (lang) {
                    JarvisLanguage.HINDI -> "बोलिए: 'हे जारविस' या कोई कमांड..."
                    JarvisLanguage.SPANISH -> "Diga 'Hola Jarvis' o una orden..."
                    JarvisLanguage.FRENCH -> "Dites 'Salut Jarvis' ou donnez un ordre..."
                    JarvisLanguage.GERMAN -> "Sagen Sie 'Hey Jarvis'..."
                    JarvisLanguage.JAPANESE -> "「ジャービス」と話しかけてください..."
                    else -> "Say 'Hey Jarvis' or give a command..."
                }
            )
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
    }

    fun startListening(): Boolean {
        if (!isRecognitionAvailable()) {
            _requiresSystemDialog.value = true
            _speechError.value = "Direct speech recognizer unavailable. Launching system speech dialog..."
            return false
        }

        try {
            stopListening()

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _speechError.value = null
                        _requiresSystemDialog.value = false
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _audioRms.value = rmsdB
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_NETWORK -> "Network connection required"
                            SpeechRecognizer.ERROR_CLIENT -> {
                                _requiresSystemDialog.value = true
                                "Speech client error. Switching to system dialog..."
                            }
                            else -> "Recognition paused ($error)"
                        }
                        _speechError.value = errorMsg
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val topMatch = matches?.firstOrNull() ?: return
                        _lastRecognizedText.value = topMatch
                        processVoiceInput(topMatch)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let {
                            _lastRecognizedText.value = it
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = createSpeechRecognizerIntent().apply {
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            speechRecognizer?.startListening(intent)
            return true
        } catch (e: Exception) {
            _isListening.value = false
            _speechError.value = e.localizedMessage
            _requiresSystemDialog.value = true
            return false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // ignore
        } finally {
            speechRecognizer = null
            _isListening.value = false
        }
    }

    fun resetSystemDialogFlag() {
        _requiresSystemDialog.value = false
    }

    fun processVoiceInput(text: String) {
        val clean = text.trim()
        _lastRecognizedText.value = clean

        // Multi-language wake-word detection:
        // Hindi: "हे जारविस", "जारविस", "अरे जारविस", "सुनो जारविस"
        // English: "Hey Jarvis", "Ok Jarvis", "Jarvis"
        // Spanish: "Oye Jarvis", "Hola Jarvis"
        // French: "Salut Jarvis", "Dis Jarvis"
        val lower = clean.lowercase()
        val command = when {
            // Hindi wake-words
            clean.contains("हे जारविस") -> clean.substringAfter("हे जारविस").trim().removePrefix(",").trim()
            clean.contains("सुनो जारविस") -> clean.substringAfter("सुनो जारविस").trim().removePrefix(",").trim()
            clean.contains("अरे जारविस") -> clean.substringAfter("अरे जारविस").trim().removePrefix(",").trim()
            clean.contains("जारविस") -> clean.substringAfter("जारविस").trim().removePrefix(",").trim()

            // English wake-words
            lower.startsWith("hey jarvis,") || lower.startsWith("hey jarvis") ->
                clean.substring(clean.indexOf("jarvis", ignoreCase = true) + 6).trim().removePrefix(",").trim()
            lower.startsWith("ok jarvis,") || lower.startsWith("ok jarvis") ->
                clean.substring(clean.indexOf("jarvis", ignoreCase = true) + 6).trim().removePrefix(",").trim()
            lower.startsWith("hello jarvis,") || lower.startsWith("hello jarvis") ->
                clean.substring(clean.indexOf("jarvis", ignoreCase = true) + 6).trim().removePrefix(",").trim()
            lower.startsWith("jarvis,") || lower.startsWith("jarvis") ->
                clean.substring(clean.indexOf("jarvis", ignoreCase = true) + 6).trim().removePrefix(",").trim()

            // Spanish wake-words
            lower.contains("oye jarvis") || lower.contains("hola jarvis") -> {
                val idx = clean.indexOf("jarvis", ignoreCase = true)
                if (idx >= 0) clean.substring(idx + 6).trim().removePrefix(",").trim() else clean
            }

            // French wake-words
            lower.contains("salut jarvis") || lower.contains("dis jarvis") -> {
                val idx = clean.indexOf("jarvis", ignoreCase = true)
                if (idx >= 0) clean.substring(idx + 6).trim().removePrefix(",").trim() else clean
            }

            else -> clean
        }

        val finalCommand = if (command.isNotBlank()) command else clean
        onCommandRecognized(finalCommand)
    }

    fun destroy() {
        stopSpeaking()
        stopListening()
        tts?.shutdown()
    }
}
