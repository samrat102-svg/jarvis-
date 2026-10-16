package com.example.data.model

enum class CallType {
    INCOMING, OUTGOING, MISSED, SCREENED
}

enum class CallStatus {
    COMPLETED,
    SCREENING_IN_PROGRESS,
    SCREENED_ACCEPTED,
    SCREENED_DISMISSED,
    BLOCKED
}

enum class JarvisIntentTag {
    VIP, URGENT, BUSINESS, CASUAL, SPAM
}

data class CallRecord(
    val id: String,
    val contactName: String,
    val phoneNumber: String,
    val timestamp: Long,
    val durationSeconds: Int,
    val callType: CallType,
    val status: CallStatus,
    val transcript: String = "",
    val jarvisAssessment: String = "",
    val intentTag: JarvisIntentTag = JarvisIntentTag.CASUAL,
    val isSpam: Boolean = false
)

enum class MessageCategory {
    ALPHA,       // High priority directives / VIP
    DIRECTIVE,   // Work & tasks
    RECON,       // Intel / notification
    SOCIAL,      // Casual chat
    SPAM         // Unwanted
}

data class MessageRecord(
    val id: String,
    val senderName: String,
    val senderPhone: String,
    val timestamp: Long,
    val body: String,
    val isIncoming: Boolean,
    val isRead: Boolean = true,
    val category: MessageCategory = MessageCategory.DIRECTIVE,
    val jarvisSummary: String = "",
    val suggestedReplies: List<String> = emptyList(),
    val replySent: String? = null
)

data class ContactRecord(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String = "",
    val isVip: Boolean = false,
    val affiliation: String = "",
    val notes: String = ""
)

data class CalendarEventRecord(
    val id: String,
    val title: String,
    val description: String = "",
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val location: String = "",
    val isJarvisScheduled: Boolean = false
)

data class ScreeningState(
    val isScreeningActive: Boolean = false,
    val callerName: String = "",
    val callerPhone: String = "",
    val callerSpeech: String = "",
    val jarvisScreeningResponse: String = "",
    val recommendation: String = "",
    val isVip: Boolean = false,
    val detectedIntent: JarvisIntentTag = JarvisIntentTag.CASUAL
)

// App Access & Launcher Model
data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val launchCount: Int = 0,
    val category: String = "Application"
)

// Voice Recognition & Biometrics Models
data class VoiceProfile(
    val commanderName: String = "Master Tony Stark",
    val enrolledPhrase: String = "I am Iron Man",
    val isEnrolled: Boolean = true,
    val pitchBaselineHz: Float = 118.5f,
    val confidenceThreshold: Int = 90,
    val strictSecurityEnabled: Boolean = true,
    val clearanceLevel: String = "OMEGA-1",
    val lastRecognizedAt: Long = System.currentTimeMillis()
)

data class VoiceRecognitionResult(
    val speakerName: String,
    val confidencePercent: Int,
    val isAuthorized: Boolean,
    val clearanceLevel: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Voice Payment Models
enum class PaymentStatus {
    PENDING_AUTHORIZATION,
    AUTHORIZED,
    COMPLETED,
    REJECTED,
    CANCELLED
}

data class PaymentTransaction(
    val id: String,
    val recipientName: String,
    val recipientAccountOrUpi: String,
    val amount: Double,
    val currency: String = "USD", // USD, INR (₹), EUR (€)
    val timestamp: Long,
    val status: PaymentStatus,
    val referenceCode: String,
    val note: String = "Stark ArcPay Voice Dispatch",
    val isVoiceAuthorized: Boolean = true
)

data class PaymentDraft(
    val recipientName: String,
    val recipientAccountOrUpi: String,
    val amount: Double,
    val currency: String = "USD",
    val note: String = "Stark ArcPay Voice Transfer",
    val isAwaitingConfirmation: Boolean = false
)
