package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiJarvisService
import com.example.ai.StarkDocumentGenerator
import com.example.apps.AppAccessManager
import com.example.data.model.CallRecord
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.model.DocumentType
import com.example.data.model.GeneratedDocument
import com.example.data.model.JarvisIntentTag
import com.example.data.model.MessageCategory
import com.example.data.model.MessageRecord
import com.example.data.model.ScreeningState
import com.example.data.repository.CalendarRepository
import com.example.data.repository.CallsMessagesRepository
import com.example.data.repository.ContactsRepository
import com.example.data.repository.PaymentRepository
import com.example.voice.JarvisVoiceManager
import com.example.voice.VoiceBiometricsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

sealed class JarvisScreen(val route: String, val title: String) {
    data object Console : JarvisScreen("console", "J.A.R.V.I.S.")
    data object Apps : JarvisScreen("apps", "App Nexus")
    data object VoicePay : JarvisScreen("voice_pay", "Stark Pay")
    data object DataVault : JarvisScreen("data_vault", "Data Vault")
    data object Calls : JarvisScreen("calls", "Comms & Screener")
    data object Messages : JarvisScreen("messages", "Message Center")
    data object Documents : JarvisScreen("documents", "Stark Docs (PPT/PDF)")
    data object Schedule : JarvisScreen("schedule", "Stark Schedule")
    data object Contacts : JarvisScreen("contacts", "Contacts & VIPs")
}

data class JarvisHudTelemetry(
    val powerPercentage: Int = 100,
    val coreTempCelsius: Int = 34,
    val securityLevel: String = "OMEGA-1",
    val activeProtocol: String = "MARK VII COMMS",
    val isDndActive: Boolean = false,
    val autoScreeningEnabled: Boolean = true
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()

    val contactsRepository = ContactsRepository(context)
    val calendarRepository = CalendarRepository(context)
    val callsMessagesRepository = CallsMessagesRepository(context)
    val paymentRepository = PaymentRepository(context)
    val appAccessManager = AppAccessManager(context)
    val voiceBiometricsManager = VoiceBiometricsManager(context)
    val aiService = GeminiJarvisService()

    private val _currentScreen = MutableStateFlow<JarvisScreen>(JarvisScreen.Console)
    val currentScreen: StateFlow<JarvisScreen> = _currentScreen.asStateFlow()

    private val _telemetry = MutableStateFlow(JarvisHudTelemetry())
    val telemetry: StateFlow<JarvisHudTelemetry> = _telemetry.asStateFlow()

    private val _screeningState = MutableStateFlow(ScreeningState())
    val screeningState: StateFlow<ScreeningState> = _screeningState.asStateFlow()

    private val _jarvisNotice = MutableStateFlow("J.A.R.V.I.S. online. At your service, sir.")
    val jarvisNotice: StateFlow<String> = _jarvisNotice.asStateFlow()

    private val _isProcessingAi = MutableStateFlow(false)
    val isProcessingAi: StateFlow<Boolean> = _isProcessingAi.asStateFlow()

    private val _voiceCommandHistory = MutableStateFlow<List<String>>(
        listOf(
            "System initialized: Stark Comms Protocol Active",
            "Screening filter enabled: Auto-intercept unknown numbers"
        )
    )
    val voiceCommandHistory: StateFlow<List<String>> = _voiceCommandHistory.asStateFlow()

    val voiceManager: JarvisVoiceManager = JarvisVoiceManager(context) { command ->
        executeVoiceCommand(command)
    }

    val documentGenerator = StarkDocumentGenerator(context)

    private val _generatedDocuments = MutableStateFlow<List<GeneratedDocument>>(emptyList())
    val generatedDocuments: StateFlow<List<GeneratedDocument>> = _generatedDocuments.asStateFlow()

    private val _isGeneratingDocument = MutableStateFlow(false)
    val isGeneratingDocument: StateFlow<Boolean> = _isGeneratingDocument.asStateFlow()

    init {
        viewModelScope.launch {
            contactsRepository.refreshContacts()
            calendarRepository.refreshCalendar()

            // Seed initial Stark documents (PDF, PPT, Excel, Word)
            try {
                val seedPdf = documentGenerator.generateDocument(
                    DocumentType.PDF,
                    "Comms & Security Telemetry Audit",
                    "Comprehensive review of incoming calls, spam interceptions, and Mark VII armor diagnostic status."
                )
                val seedPpt = documentGenerator.generateDocument(
                    DocumentType.PPT,
                    "Arc Reactor Clean Energy Initiative",
                    "Executive slide deck on miniature fusion power architecture and municipal grid rollout."
                )
                val seedExcel = documentGenerator.generateDocument(
                    DocumentType.EXCEL,
                    "Q4 Stark Defense & R&D Budget Matrix",
                    "Financial model tracking division burn rates, equipment surplus, and research allocations."
                )
                val seedWord = documentGenerator.generateDocument(
                    DocumentType.WORD,
                    "Board Memorandum: Global Threat Defense",
                    "Executive directive on automated call triage, satellite relays, and rapid response units."
                )
                _generatedDocuments.value = listOf(seedPdf, seedPpt, seedExcel, seedWord)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun createDocument(type: DocumentType, topic: String, details: String = "") {
        _isGeneratingDocument.value = true
        triggerVibration()
        viewModelScope.launch {
            val doc = documentGenerator.generateDocument(type, topic, details)
            _generatedDocuments.value = listOf(doc) + _generatedDocuments.value
            _isGeneratingDocument.value = false
            val lang = voiceManager.currentLanguage.value
            if (lang == com.example.voice.JarvisLanguage.HINDI) {
                announce("${doc.type.displayName} फाइल '${doc.title}' सफलतापूर्वक तैयार कर दी गई है, सर।")
            } else {
                announce("I have compiled your ${doc.type.displayName} file for '${doc.title}', sir.")
            }
        }
    }

    fun playSignatureGreeting() {
        triggerVibration()
        voiceManager.playSignatureGreeting()
        _jarvisNotice.value = JarvisVoiceManager.SIGNATURE_GREETING
        _voiceCommandHistory.value = listOf("JARVIS [Signature Protocol Active]: \"${JarvisVoiceManager.SIGNATURE_GREETING}\"") + _voiceCommandHistory.value.take(15)
    }

    fun refreshContactsFromDevice() {
        viewModelScope.launch {
            contactsRepository.refreshContacts()
        }
    }

    fun refreshCalendarFromDevice() {
        viewModelScope.launch {
            calendarRepository.refreshCalendar()
        }
    }

    fun navigateTo(screen: JarvisScreen) {
        _currentScreen.value = screen
    }

    fun triggerVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    // Voice Command Parsing & Execution
    fun executeVoiceCommand(rawCommand: String) {
        if (rawCommand.isBlank()) return
        val command = rawCommand.trim()
        val lower = command.lowercase()

        _voiceCommandHistory.value = listOf("Command: \"$command\"") + _voiceCommandHistory.value.take(15)
        triggerVibration()

        viewModelScope.launch {
            when {
                // "Read my last message" / "Read unread message" / Hindi: "मैसेज पढ़ो", "message padho"
                (lower.contains("read") && (lower.contains("message") || lower.contains("text"))) ||
                        lower.contains("मैसेज पढ़ो") || lower.contains("संदेश पढ़ो") || lower.contains("message padho") -> {
                    readLastMessage()
                }

                // Hindi: "[Name] को मैसेज भेजो कि [Message]" or "[Name] ko message bhejo ki [Message]"
                command.contains("को मैसेज") || lower.contains("ko message") || lower.contains("ko text") -> {
                    handleHindiSendTextCommand(command)
                }

                // Document Generation: PPT / Slides / Presentation
                lower.contains("presentation") || lower.contains("ppt") || lower.contains("slides") || command.contains("प्रेजेंटेशन") || command.contains("पीपीटी") -> {
                    val topic = extractTopicFromCommand(command, "presentation", "ppt", "slides", "प्रेजेंटेशन")
                    createDocument(DocumentType.PPT, topic.ifBlank { "Stark Arc Reactor Initiative" })
                }

                // Document Generation: PDF
                lower.contains("pdf") || (lower.contains("generate") && lower.contains("report")) || command.contains("पीडीएफ") || command.contains("रिपोर्ट") -> {
                    val topic = extractTopicFromCommand(command, "pdf", "report", "पीडीएफ", "रिपोर्ट")
                    createDocument(DocumentType.PDF, topic.ifBlank { "Executive Security & Telemetry Report" })
                }

                // Document Generation: Excel / Spreadsheet
                lower.contains("excel") || lower.contains("spreadsheet") || lower.contains("sheet") || command.contains("एक्सेल") || command.contains("शीट") -> {
                    val topic = extractTopicFromCommand(command, "excel", "spreadsheet", "sheet", "एक्सेल")
                    createDocument(DocumentType.EXCEL, topic.ifBlank { "Stark Division Financial Matrix" })
                }

                // Document Generation: Word / Memo
                lower.contains("word") || lower.contains("memo") || lower.contains("document") || command.contains("वर्ड") || command.contains("दस्तावेज") -> {
                    val topic = extractTopicFromCommand(command, "word", "memo", "document", "वर्ड")
                    createDocument(DocumentType.WORD, topic.ifBlank { "Executive Operational Memorandum" })
                }

                // "Send a text to [Name] saying [Message]"
                lower.contains("send a text") || lower.contains("send text") || lower.contains("text to") -> {
                    handleSendTextCommand(command)
                }

                // Hindi: "[Name] को कॉल करो" or "[Name] ko call karo"
                command.contains("को कॉल") || command.contains("को फोन") || lower.contains("ko call") || lower.contains("ko phone") -> {
                    val rawTarget = when {
                        command.contains("को कॉल") -> command.substringBefore("को कॉल").trim()
                        command.contains("को फोन") -> command.substringBefore("को फोन").trim()
                        lower.contains("ko call") -> {
                            val idx = lower.indexOf("ko call")
                            if (idx >= 0) command.substring(0, idx).trim() else command
                        }
                        else -> {
                            val idx = lower.indexOf("ko phone")
                            if (idx >= 0) command.substring(0, idx).trim() else command
                        }
                    }
                    val targetName = rawTarget.substringAfterLast(" ").trim()
                    handleCallCommand(targetName)
                }

                // "Call [Name]"
                lower.startsWith("call ") || lower.contains("place a call to ") -> {
                    val targetName = when {
                        lower.startsWith("call ") -> command.substring(5).trim()
                        lower.contains("place a call to ") -> command.substringAfter("place a call to ").trim()
                        else -> command.substringAfter("call ").trim()
                    }
                    handleCallCommand(targetName)
                }

                // "Schedule [event]" / "Remind me to [event]" / Hindi "शेड्यूल"
                lower.contains("schedule") || lower.contains("remind me to") || command.contains("शेड्यूल") || lower.contains("meeting") -> {
                    handleScheduleCommand(command)
                }

                // "Screen calls" / "Enable screening"
                lower.contains("screen call") || lower.contains("enable screening") || command.contains("कॉल स्क्रीन") -> {
                    _telemetry.value = _telemetry.value.copy(autoScreeningEnabled = true)
                    val lang = voiceManager.currentLanguage.value
                    if (lang == com.example.voice.JarvisLanguage.HINDI) {
                        announce("कॉल स्क्रीनिंग प्रोटोकॉल सक्रिय कर दिया गया है, सर।")
                    } else {
                        announce("Incoming call screening protocol has been engaged, sir.")
                    }
                }

                // "Diagnostics" / "Status" / Hindi "हालचाल बताओ"
                lower.contains("status") || lower.contains("diagnostic") || command.contains("हालचाल") || command.contains("हाल बताओ") -> {
                    val lang = voiceManager.currentLanguage.value
                    if (lang == com.example.voice.JarvisLanguage.HINDI) {
                        announce("कोर रिएक्टर 100% पर है। सभी प्रणालियां और सुरक्षा प्रोटोकॉल सामान्य हैं, सर।")
                    } else {
                        announce("Core reactor at 100%. Diagnostics nominal. Comms array ready for your command, sir.")
                    }
                }

                // Universal App Access: "Open [App]" / "Launch [App]" / Hindi "[App] खोलो" / "[App] चालू करो"
                lower.startsWith("open ") || lower.startsWith("launch ") || lower.contains("खोलो") || lower.contains("चालू करो") || (lower.contains("access") && lower.contains("app")) -> {
                    handleAppLaunchCommand(command)
                }

                // Voice Recognition & Biometrics: "Who is speaking" / "Recognize voice" / "Voice biometric" / Hindi "मेरी आवाज़ पहचानो"
                lower.contains("who is speaking") || lower.contains("recognize voice") || lower.contains("voice recognition") || lower.contains("voice print") || command.contains("आवाज़ पहचानो") || command.contains("मेरी आवाज़") -> {
                    handleVoiceRecognitionQuery()
                }

                // Confirm Payment by Voice: "Confirm" / "Authorize" / "Confirm payment"
                (lower.contains("confirm payment") || lower.contains("authorize payment") || lower.contains("confirm transfer") || lower == "confirm" || lower == "authorize" || command.contains("भुगतान पुष्टि") || command.contains("ट्रांसफर करो")) && paymentRepository.activeDraft.value != null -> {
                    confirmVoicePayment()
                }

                // Voice Payment: "Pay $50 to Pepper Potts" / "Send 100 to Happy" / "Transfer $250 to Rhodey" / Hindi "पैसे भेजो" / "पे करो"
                (lower.startsWith("pay ") || lower.startsWith("send money") || lower.startsWith("transfer ") || lower.contains("pay $") || lower.contains("transfer $") || command.contains("पैसे भेजो") || command.contains("रुपये भेजो") || command.contains("पे करो")) -> {
                    handleVoicePaymentCommand(command)
                }

                // Delete Number / Contact: "Delete number [Name]" / "Remove contact [Name]"
                (lower.contains("delete") || lower.contains("remove") || lower.contains("हटाओ") || lower.contains("डिलीट")) && (lower.contains("number") || lower.contains("contact") || command.contains("नंबर") || command.contains("संपर्क")) -> {
                    handleDeleteNumberCommand(command)
                }

                // Delete Call Data / Call Logs: "Delete call data" / "Clear call logs" / "Wipe call history"
                (lower.contains("delete") || lower.contains("clear") || lower.contains("wipe") || lower.contains("purge") || lower.contains("हटाओ") || lower.contains("डिलीट") || lower.contains("मिटाओ")) && (lower.contains("call") || lower.contains("कॉल")) -> {
                    handleDeleteCallDataCommand()
                }

                // Delete Calendar Data: "Delete calendar data" / "Clear schedule" / "Delete meeting"
                (lower.contains("delete") || lower.contains("clear") || lower.contains("wipe") || lower.contains("हटाओ") || lower.contains("डिलीट") || lower.contains("मिटाओ")) && (lower.contains("cal") || lower.contains("schedule") || lower.contains("meeting") || lower.contains("event") || command.contains("कैलेंडर") || command.contains("शेड्यूल")) -> {
                    handleDeleteCalendarDataCommand(command)
                }

                // Emergency Master Wipe: "Wipe all data" / "Clear all data" / "Erase mainframe data"
                lower.contains("wipe all data") || lower.contains("clear all data") || lower.contains("erase all data") || command.contains("सारा डेटा मिटाओ") || command.contains("सारा डेटा डिलीट") -> {
                    wipeAllData()
                }

                // General AI query to Jarvis
                else -> {
                    _isProcessingAi.value = true
                    val response = aiService.askJarvis(command)
                    _isProcessingAi.value = false
                    announce(response)
                }
            }
        }
    }

    private fun extractTopicFromCommand(command: String, vararg keywords: String): String {
        // Handle Hindi pre-topic pattern: "[Topic] पर पीपीटी बनाओ"
        if (command.contains(" पर ") && (command.contains("पीपीटी") || command.contains("प्रेजेंटेशन") || command.contains("एक्सेल") || command.contains("पीडीएफ") || command.contains("शीट") || command.contains("वर्ड"))) {
            val before = command.substringBefore(" पर ").trim().removePrefix("हे जारविस").removePrefix("जारविस").removePrefix(",").trim()
            if (before.isNotBlank()) return before
        }

        for (kw in keywords) {
            val lower = command.lowercase()
            val kwLower = kw.lowercase()
            if (lower.contains(kwLower)) {
                val idx = lower.indexOf(kwLower)
                var after = command.substring(idx + kw.length).trim().removePrefix(":").trim()
                if (after.startsWith("on ", ignoreCase = true)) after = after.substring(3).trim()
                if (after.startsWith("for ", ignoreCase = true)) after = after.substring(4).trim()
                if (after.startsWith("of ", ignoreCase = true)) after = after.substring(3).trim()
                if (after.startsWith("about ", ignoreCase = true)) after = after.substring(6).trim()
                if (after.startsWith("का ", ignoreCase = true)) after = after.substring(3).trim()
                if (after.startsWith("की ", ignoreCase = true)) after = after.substring(3).trim()
                if (after.startsWith("के लिए ", ignoreCase = true)) after = after.substring(7).trim()
                if (after.startsWith("पर ", ignoreCase = true)) after = after.substring(3).trim()
                if (after.endsWith("बनाओ", ignoreCase = true)) after = after.removeSuffix("बनाओ").trim()
                if (after.endsWith("तैयार करो", ignoreCase = true)) after = after.removeSuffix("तैयार करो").trim()
                if (after.isNotBlank()) return after.replaceFirstChar { it.uppercase() }
            }
        }
        return ""
    }

    private fun announce(text: String) {
        _jarvisNotice.value = text
        _voiceCommandHistory.value = listOf("JARVIS: $text") + _voiceCommandHistory.value.take(15)
        voiceManager.speak(text)
    }

    private fun handleHindiSendTextCommand(command: String) {
        val lower = command.lowercase()
        val rawRecipient = when {
            command.contains(" को मैसेज") -> command.substringBefore(" को मैसेज").trim()
            command.contains(" को टेक्स्ट") -> command.substringBefore(" को टेक्स्ट").trim()
            lower.contains(" ko message") -> {
                val idx = lower.indexOf(" ko message")
                if (idx >= 0) command.substring(0, idx).trim() else command
            }
            lower.contains(" ko text") -> {
                val idx = lower.indexOf(" ko text")
                if (idx >= 0) command.substring(0, idx).trim() else command
            }
            else -> "Someone"
        }.substringAfterLast(" ").trim()

        val rawBody = when {
            command.contains(" कि ") -> command.substringAfter(" कि ").trim()
            lower.contains(" ki ") -> {
                val idx = lower.indexOf(" ki ")
                if (idx >= 0) command.substring(idx + 4).trim() else command
            }
            lower.contains(" saying ") -> {
                val idx = lower.indexOf(" saying ")
                if (idx >= 0) command.substring(idx + 8).trim() else command
            }
            else -> "Awaiting instructions."
        }

        val mappedRecipient = when {
            rawRecipient.contains("जॉन") || rawRecipient.equals("john", ignoreCase = true) -> "John"
            rawRecipient.contains("मम्मी") || rawRecipient.contains("माँ") || rawRecipient.equals("mom", ignoreCase = true) -> "Mom"
            rawRecipient.contains("पेपर") || rawRecipient.equals("pepper", ignoreCase = true) -> "Pepper Potts"
            rawRecipient.contains("हैप्पी") || rawRecipient.equals("happy", ignoreCase = true) -> "Happy Hogan"
            else -> rawRecipient
        }

        val contact = contactsRepository.findContactByName(mappedRecipient)
        val phone = contact?.phoneNumber ?: "+1 (555) 678-9012"
        val displayName = contact?.name ?: mappedRecipient

        callsMessagesRepository.composeNewMessage(displayName, phone, rawBody)
        val isHindi = voiceManager.currentLanguage.value == com.example.voice.JarvisLanguage.HINDI
        val announcement = if (isHindi) {
            "$displayName को संदेश भेज दिया गया है: \"$rawBody\", सर।"
        } else {
            "Dispatched message to $displayName: \"$rawBody\", sir."
        }
        announce(announcement)
    }

    private fun handleCallCommand(contactNameQuery: String) {
        val cleanQuery = contactNameQuery.removeSuffix(".").trim()
        val mappedQuery = when {
            cleanQuery.contains("मम्मी") || cleanQuery.contains("माँ") || cleanQuery.contains("माताजी") || cleanQuery.equals("mom", ignoreCase = true) -> "Mom"
            cleanQuery.contains("पेपर") || cleanQuery.equals("pepper", ignoreCase = true) -> "Pepper Potts"
            cleanQuery.contains("रोडी") || cleanQuery.equals("rhodey", ignoreCase = true) -> "Col. James Rhodes"
            cleanQuery.contains("हैप्पी") || cleanQuery.equals("happy", ignoreCase = true) -> "Happy Hogan"
            cleanQuery.contains("जॉन") || cleanQuery.equals("john", ignoreCase = true) -> "John"
            else -> cleanQuery
        }

        val contact = contactsRepository.findContactByName(mappedQuery)
            ?: contactsRepository.contacts.value.firstOrNull {
                it.name.contains(mappedQuery, ignoreCase = true) || mappedQuery.contains(it.name, ignoreCase = true)
            }

        val isHindi = voiceManager.currentLanguage.value == com.example.voice.JarvisLanguage.HINDI

        if (contact != null) {
            val announcement = if (isHindi) {
                "${contact.name} को कॉल लगाया जा रहा है, सर।"
            } else {
                "Placing direct call to ${contact.name} at ${contact.phoneNumber}, sir."
            }
            announce(announcement)

            // Log outgoing call
            val callRecord = CallRecord(
                id = "call-${UUID.randomUUID()}",
                contactName = contact.name,
                phoneNumber = contact.phoneNumber,
                timestamp = System.currentTimeMillis(),
                durationSeconds = 0,
                callType = CallType.OUTGOING,
                status = CallStatus.COMPLETED,
                transcript = "Outgoing call placed by user directive via JARVIS comms.",
                jarvisAssessment = "User voice-initiated call to ${contact.name}.",
                intentTag = if (contact.isVip) JarvisIntentTag.VIP else JarvisIntentTag.CASUAL
            )
            callsMessagesRepository.addCall(callRecord)

            // Dispatch intent to phone dialer
            try {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${Uri.encode(contact.phoneNumber)}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Ignore dialer start error in emulator
            }
        } else {
            val notFound = if (isHindi) {
                "मुझे आपकी संपर्क सूची में '$cleanQuery' नहीं मिला, सर।"
            } else {
                "I could not locate '$cleanQuery' in your contacts archive, sir."
            }
            announce(notFound)
        }
    }

    private fun handleSendTextCommand(command: String) {
        // e.g. "Send a text to John saying I'll be late"
        // or "Send a text to Mom saying I will be home soon"
        var recipient = ""
        var messageBody = ""

        val regex = Regex("""(?i)send (?:a )?text to (.*?) saying (.*)""")
        val match = regex.find(command)

        if (match != null) {
            recipient = match.groupValues[1].trim()
            messageBody = match.groupValues[2].trim()
        } else {
            // Fallback split
            val afterTo = command.substringAfter("to ", "")
            if (afterTo.contains(" saying ")) {
                recipient = afterTo.substringBefore(" saying ").trim()
                messageBody = afterTo.substringAfter(" saying ").trim()
            } else {
                recipient = afterTo.trim()
                messageBody = "Awaiting follow-up."
            }
        }

        val contact = contactsRepository.findContactByName(recipient)
        val phone = contact?.phoneNumber ?: "+1 (555) 000-1122"
        val displayName = contact?.name ?: recipient

        callsMessagesRepository.composeNewMessage(displayName, phone, messageBody)
        announce("Dispatched message to $displayName: \"$messageBody\", sir.")

        // Also launch SMS intent if phone available
        try {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(phone)}")
                putExtra("sms_body", messageBody)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            // Handled within app inbox
        }
    }

    private fun readLastMessage() {
        val messages = callsMessagesRepository.messages.value.filter { it.isIncoming }
        val lastUnread = messages.firstOrNull { !it.isRead } ?: messages.firstOrNull()

        if (lastUnread != null) {
            callsMessagesRepository.markMessageRead(lastUnread.id)
            val speech = "You have a message from ${lastUnread.senderName}. It reads: \"${lastUnread.body}\". ${lastUnread.jarvisSummary}"
            announce(speech)
        } else {
            announce("There are no incoming messages currently in your inbox, sir.")
        }
    }

    private fun handleScheduleCommand(command: String) {
        // e.g. "Schedule flight test tomorrow at 3pm" or "Remind me to test armor"
        val lower = command.lowercase()
        val eventTitle = when {
            lower.contains("schedule ") -> command.substringAfter("schedule ").trim()
            lower.contains("remind me to ") -> command.substringAfter("remind me to ").trim()
            else -> command
        }

        val cal = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 0)
        }

        viewModelScope.launch {
            val event = calendarRepository.scheduleEvent(
                title = eventTitle.replaceFirstChar { it.uppercase() },
                description = "Voice-scheduled directive: $command",
                startTimeMillis = cal.timeInMillis,
                endTimeMillis = cal.timeInMillis + 3600000L,
                location = "Stark Facility"
            )
            announce("Added \"${event.title}\" to your schedule for 2 hours from now, sir.")
        }
    }

    // Smart Reply Selection
    fun selectSmartReply(message: MessageRecord, replyText: String) {
        triggerVibration()
        callsMessagesRepository.sendReply(message.id, replyText)
        announce("Smart reply dispatched to ${message.senderName}: \"$replyText\", sir.")

        // Optionally fire SMS intent
        try {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(message.senderPhone)}")
                putExtra("sms_body", replyText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            // Handled
        }
    }

    // Call Screening Simulator
    fun simulateIncomingCall(
        callerName: String = "Unknown Telemarketer",
        callerPhone: String = "+1 (800) 998-2104",
        sampleSpeech: String = "Hello Mr. Stark! I'm calling from Premier Gold Investments regarding our new portfolio risk protection.",
        isVip: Boolean = false
    ) {
        triggerVibration()
        viewModelScope.launch {
            _screeningState.value = ScreeningState(
                isScreeningActive = true,
                callerName = callerName,
                callerPhone = callerPhone,
                callerSpeech = sampleSpeech,
                jarvisScreeningResponse = "J.A.R.V.I.S. protocol engaged. Screening caller voice...",
                recommendation = "Analyzing speech patterns and caller credentials...",
                isVip = isVip,
                detectedIntent = if (isVip) JarvisIntentTag.VIP else JarvisIntentTag.SPAM
            )

            // Play voice alert
            voiceManager.speak("Incoming call from $callerName. Screening caller on speaker protocol now.")

            delay(1200)

            // AI analysis
            val (jarvisSpeech, recommendation) = aiService.screenIncomingCall(callerName, sampleSpeech)

            _screeningState.value = _screeningState.value.copy(
                jarvisScreeningResponse = jarvisSpeech,
                recommendation = recommendation
            )

            voiceManager.speak(jarvisSpeech)
        }
    }

    fun acceptScreenedCall() {
        triggerVibration()
        val state = _screeningState.value
        val callRecord = CallRecord(
            id = "screened-${UUID.randomUUID()}",
            contactName = state.callerName,
            phoneNumber = state.callerPhone,
            timestamp = System.currentTimeMillis(),
            durationSeconds = 60,
            callType = CallType.INCOMING,
            status = CallStatus.SCREENED_ACCEPTED,
            transcript = "Caller: '${state.callerSpeech}' -> JARVIS: '${state.jarvisScreeningResponse}' -> Accepted by user.",
            jarvisAssessment = state.recommendation,
            intentTag = state.detectedIntent
        )
        callsMessagesRepository.addCall(callRecord)
        _screeningState.value = ScreeningState() // close
        announce("Call patched through to your primary headset, sir.")
    }

    fun dismissScreenedCallToVoicemail() {
        triggerVibration()
        val state = _screeningState.value
        val callRecord = CallRecord(
            id = "screened-${UUID.randomUUID()}",
            contactName = state.callerName,
            phoneNumber = state.callerPhone,
            timestamp = System.currentTimeMillis(),
            durationSeconds = 15,
            callType = CallType.SCREENED,
            status = CallStatus.SCREENED_DISMISSED,
            transcript = "Caller: '${state.callerSpeech}' -> JARVIS: '${state.jarvisScreeningResponse}' -> Dismissed to voicemail.",
            jarvisAssessment = "Diverted to audio storage. ${state.recommendation}",
            intentTag = state.detectedIntent
        )
        callsMessagesRepository.addCall(callRecord)
        _screeningState.value = ScreeningState() // close
        announce("Caller diverted to encrypted voicemail archive, sir.")
    }

    fun blockAndAnnihilateSpam() {
        triggerVibration()
        val state = _screeningState.value
        val callRecord = CallRecord(
            id = "screened-${UUID.randomUUID()}",
            contactName = state.callerName,
            phoneNumber = state.callerPhone,
            timestamp = System.currentTimeMillis(),
            durationSeconds = 5,
            callType = CallType.SCREENED,
            status = CallStatus.BLOCKED,
            transcript = "Caller: '${state.callerSpeech}' -> JARVIS: 'Connection severed.'",
            jarvisAssessment = "Confirmed spam. Number permanently added to firewall blocklist.",
            intentTag = JarvisIntentTag.SPAM,
            isSpam = true
        )
        callsMessagesRepository.addCall(callRecord)
        _screeningState.value = ScreeningState() // close
        announce("Line severed. Number added to Stark Defense firewall blocklist, sir.")
    }

    fun toggleAutoScreening() {
        val next = !_telemetry.value.autoScreeningEnabled
        _telemetry.value = _telemetry.value.copy(autoScreeningEnabled = next)
        announce(if (next) "Auto-screening protocol activated, sir." else "Auto-screening protocol suspended.")
    }

    fun toggleDnd() {
        val next = !_telemetry.value.isDndActive
        _telemetry.value = _telemetry.value.copy(isDndActive = next)
        announce(if (next) "Do Not Disturb protocol enabled. Only VIP contacts will be permitted through." else "Do Not Disturb disengaged.")
    }

    // --- Universal App Access & Launcher ---
    fun openApp(nameOrPackage: String) {
        val result = appAccessManager.launchAppByName(nameOrPackage)
        val isHindi = voiceManager.currentLanguage.value == com.example.voice.JarvisLanguage.HINDI
        when (result) {
            is AppAccessManager.LaunchResult.Success -> {
                val msg = if (isHindi) "${result.appName} खोला जा रहा है, सर।" else "Launching ${result.appName}, sir."
                announce(msg)
            }
            is AppAccessManager.LaunchResult.MarketFallback -> {
                val msg = if (isHindi) "${result.appName} आपके डिवाइस पर नहीं मिला। गूगल प्ले स्टोर पर खोजा जा रहा है।" else "${result.appName} is not installed locally. Redirecting to Google Play Store, sir."
                announce(msg)
            }
            is AppAccessManager.LaunchResult.NotFound -> {
                val msg = if (isHindi) "एप्लिकेशन '${result.appName}' नहीं मिल सका, सर।" else "Unable to dispatch application '${result.appName}', sir."
                announce(msg)
            }
        }
    }

    private fun handleAppLaunchCommand(command: String) {
        val lower = command.lowercase()
        val appQuery = when {
            lower.startsWith("open ") -> command.substring(5).trim()
            lower.startsWith("launch ") -> command.substring(7).trim()
            lower.contains("खोलो") -> command.substringBefore("खोलो").trim().removePrefix("हे जारविस").removePrefix("जारविस").removePrefix(",").trim()
            lower.contains("चालू करो") -> command.substringBefore("चालू करो").trim().removePrefix("हे जारविस").removePrefix("जारविस").removePrefix(",").trim()
            else -> command.substringAfter("app", "").trim().ifBlank { command }
        }
        openApp(appQuery.removeSuffix("app").trim())
    }

    // --- Voice Recognition & Biometrics ---
    private fun handleVoiceRecognitionQuery() {
        val isHindi = voiceManager.currentLanguage.value == com.example.voice.JarvisLanguage.HINDI
        val res = voiceBiometricsManager.verifySpeaker("voice recognition verification")
        val msg = if (isHindi) {
            "आवाज़ पहचान ली गई है: ${res.speakerName}। सटीकता ${res.confidencePercent}%। सुरक्षा स्तर: ${res.clearanceLevel}।"
        } else {
            "Voice signature authenticated: ${res.speakerName}. Match confidence ${res.confidencePercent} percent. Clearance level ${res.clearanceLevel} verified."
        }
        announce(msg)
    }

    // --- Voice Payments ---
    private fun handleVoicePaymentCommand(command: String) {
        val lower = command.lowercase()

        // Extract amount
        val numberRegex = Regex("""(?:\$|₹|rs\.?|rupees?)?\s*(\d+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
        val match = numberRegex.find(command)
        val amount = match?.groupValues?.get(1)?.toDoubleOrNull() ?: 50.0
        val currency = if (command.contains("₹") || lower.contains("rupee") || lower.contains("रुपये") || lower.contains("रुपया")) "INR" else "USD"

        // Extract recipient
        var recipient = when {
            lower.contains(" to ") -> command.substringAfter(" to ").trim()
            lower.contains("ko ") -> {
                val afterKo = command.substringBefore("ko").trim()
                afterKo.substringAfterLast(" ").trim()
            }
            command.contains("को") -> {
                val before = command.substringBefore("को").trim()
                before.substringAfterLast(" ").trim()
            }
            else -> "Pepper Potts"
        }
        recipient = recipient.removeSuffix("dollars").removeSuffix("rupees").trim()
        if (recipient.isBlank()) recipient = "Pepper Potts"

        initiateVoicePayment(recipient, amount, "Stark ArcPay Voice Transfer", currency)
    }

    fun initiateVoicePayment(recipient: String, amount: Double, note: String = "Stark ArcPay Voice Transfer", currency: String = "USD") {
        val isHindi = voiceManager.currentLanguage.value == com.example.voice.JarvisLanguage.HINDI
        val draft = paymentRepository.createPaymentDraft(
            recipient = recipient,
            amount = amount,
            currency = currency,
            note = note
        )
        val msg = if (isHindi) {
            "${draft.recipientName} को $$amount भुगतान की तैयारी हो गई है। पुष्टि के लिए 'Confirm' बोलें या स्क्रीन पर टैप करें, सर।"
        } else {
            "Initiating payment protocol: $${amount} to ${draft.recipientName}. Awaiting your voice authorization, sir."
        }
        announce(msg)
        navigateTo(JarvisScreen.VoicePay)
    }

    fun confirmVoicePayment() {
        val draft = paymentRepository.activeDraft.value ?: return
        val isHindi = voiceManager.currentLanguage.value == com.example.voice.JarvisLanguage.HINDI
        val tx = paymentRepository.confirmPayment(draft)
        triggerVibration()
        val msg = if (isHindi) {
            "भुगतान अधिकृत और पूरा हो गया है। $${tx.amount} ${tx.recipientName} को भेज दिए गए हैं। संदर्भ कोड: ${tx.referenceCode}।"
        } else {
            "Transaction authorized and dispatched. $${tx.amount} successfully transferred to ${tx.recipientName}. Cryptographic reference: ${tx.referenceCode}."
        }
        announce(msg)
    }

    // --- Data Deletion Suite ---
    private fun handleDeleteNumberCommand(command: String) {
        val isHindi = voiceManager.currentLanguage.value == com.example.voice.JarvisLanguage.HINDI
        val lower = command.lowercase()
        val target = when {
            lower.contains("number") -> command.substringAfter("number").trim()
            lower.contains("contact") -> command.substringAfter("contact").trim()
            command.contains("नंबर") -> command.substringBefore("का नंबर").ifBlank { command.substringBefore("नंबर") }.trim().substringAfterLast(" ")
            else -> command.substringAfter("delete").trim()
        }
        val cleanTarget = target.removePrefix("of").removePrefix("for").trim()
        val deleted = contactsRepository.deleteContactByQuery(cleanTarget)
        triggerVibration()

        val msg = if (deleted != null) {
            if (isHindi) "${deleted.name} का संपर्क और नंबर सफलतापूर्वक हटा दिया गया है, सर।"
            else "Contact ${deleted.name} (${deleted.phoneNumber}) has been purged from Stark mainframe, sir."
        } else {
            if (isHindi) "संपर्क '$cleanTarget' नहीं मिला, सर।"
            else "I could not locate '$cleanTarget' in the contact registry, sir."
        }
        announce(msg)
    }

    private fun handleDeleteCallDataCommand() {
        val isHindi = voiceManager.currentLanguage.value == com.example.voice.JarvisLanguage.HINDI
        callsMessagesRepository.clearAllCalls()
        triggerVibration()
        val msg = if (isHindi) {
            "सभी कॉल डेटा, रिकॉर्डिंग और लॉग मिटा दिए गए हैं, सर।"
        } else {
            "All call records, screening transcripts, and telecommunication logs have been permanently erased, sir."
        }
        announce(msg)
    }

    private fun handleDeleteCalendarDataCommand(command: String) {
        val isHindi = voiceManager.currentLanguage.value == com.example.voice.JarvisLanguage.HINDI
        val lower = command.lowercase()
        if (lower.contains("all") || lower.contains("all cal") || lower.contains("clear schedule") || command.contains("सारा")) {
            calendarRepository.clearAllEvents()
            triggerVibration()
            val msg = if (isHindi) "कैलेंडर का सारा शेड्यूल मिटा दिया गया है, सर।" else "All calendar events and schedule directives have been cleared, sir."
            announce(msg)
        } else {
            val query = command.substringAfter("meeting").substringAfter("event").substringAfter("schedule").trim()
            val deleted = if (query.isNotBlank()) calendarRepository.deleteEventsByTitle(query) else null
            triggerVibration()
            if (deleted != null) {
                val msg = if (isHindi) "मीटिंग '${deleted.title}' कैलेंडर से हटा दी गई है, सर।" else "Event '${deleted.title}' removed from calendar schedule, sir."
                announce(msg)
            } else {
                calendarRepository.clearAllEvents()
                val msg = if (isHindi) "कैलेंडर डेटा हटा दिया गया है, सर।" else "Calendar schedule data purged as requested, sir."
                announce(msg)
            }
        }
    }

    fun deleteContact(id: String) {
        triggerVibration()
        contactsRepository.deleteContact(id)
        announce("Contact removed from Stark directory.")
    }

    fun deleteCallRecord(id: String) {
        triggerVibration()
        callsMessagesRepository.deleteCall(id)
        announce("Call log entry purged.")
    }

    fun deleteCalendarEvent(id: String) {
        triggerVibration()
        calendarRepository.deleteEvent(id)
        announce("Scheduled event erased.")
    }

    fun wipeCallData() {
        triggerVibration()
        callsMessagesRepository.clearAllCalls()
        announce("All call and screening records have been completely erased.")
    }

    fun wipeCalendarData() {
        triggerVibration()
        calendarRepository.clearAllEvents()
        announce("All calendar events have been purged.")
    }

    fun wipeContactsData() {
        triggerVibration()
        contactsRepository.clearAllContacts()
        announce("All contact numbers have been cleared.")
    }

    fun wipeAllData() {
        triggerVibration()
        contactsRepository.clearAllContacts()
        callsMessagesRepository.clearAllCalls()
        calendarRepository.clearAllEvents()
        announce("Master wipe complete. All contacts, call records, and calendar telemetry purged from mainframe.")
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
