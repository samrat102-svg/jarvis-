package com.example.data.repository

import android.content.Context
import com.example.data.model.CallRecord
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.model.JarvisIntentTag
import com.example.data.model.MessageCategory
import com.example.data.model.MessageRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CallsMessagesRepository(private val context: Context) {

    private val initialCalls = listOf(
        CallRecord(
            id = "call-1",
            contactName = "Unknown Caller",
            phoneNumber = "+1 (800) 555-0199",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 18,
            durationSeconds = 42,
            callType = CallType.SCREENED,
            status = CallStatus.SCREENED_DISMISSED,
            transcript = "Caller: 'Hello! I'm calling from Global Horizons regarding an exclusive timeshare warranty offer.' -> JARVIS: 'I am afraid Mr. Stark is indisposed. Directing this correspondence to the recycle bin.'",
            jarvisAssessment = "Automated sales pitch detected. High probability of telemarketing fraud. Call politely terminated and number placed on spam quarantine.",
            intentTag = JarvisIntentTag.SPAM,
            isSpam = true
        ),
        CallRecord(
            id = "call-2",
            contactName = "Pepper Potts",
            phoneNumber = "+1 (555) 782-7501",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 85,
            durationSeconds = 184,
            callType = CallType.INCOMING,
            status = CallStatus.SCREENED_ACCEPTED,
            transcript = "Caller: 'Tony, make sure you look over the clean energy bill before the 2 PM press conference.' -> JARVIS: 'Transferred directly to user with high priority.'",
            jarvisAssessment = "VIP Priority contact. Immediate transfer authorized with schedule reminder tagged.",
            intentTag = JarvisIntentTag.VIP,
            isSpam = false
        ),
        CallRecord(
            id = "call-3",
            contactName = "Col. James Rhodes",
            phoneNumber = "+1 (555) 927-4402",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 240,
            durationSeconds = 95,
            callType = CallType.OUTGOING,
            status = CallStatus.COMPLETED,
            transcript = "Tony: 'Rhodey, repulsor sync is at 98%. War Machine upgrades ready.' -> Col. Rhodes: 'Copy that Tony, landing on the helipad in 20.'",
            jarvisAssessment = "Tactical coordination call completed. Telemetry sync verified.",
            intentTag = JarvisIntentTag.BUSINESS,
            isSpam = false
        ),
        CallRecord(
            id = "call-4",
            contactName = "Express Courier",
            phoneNumber = "+1 (555) 301-4492",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 420,
            durationSeconds = 35,
            callType = CallType.SCREENED,
            status = CallStatus.SCREENED_ACCEPTED,
            transcript = "Caller: 'Delivery for Stark Labs at Gate 3. Need gate pass.' -> JARVIS: 'Gate code 4096 issued. Forwarding notice to Happy Hogan.'",
            jarvisAssessment = "Legitimate logistics courier. Gate clearance authenticated.",
            intentTag = JarvisIntentTag.CASUAL,
            isSpam = false
        )
    )

    private val initialMessages = listOf(
        MessageRecord(
            id = "msg-1",
            senderName = "Pepper Potts",
            senderPhone = "+1 (555) 782-7501",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 8,
            body = "Tony, are you coming to the symposium on sustainable energy tonight? The delegates are arriving at 7 PM.",
            isIncoming = true,
            isRead = false,
            category = MessageCategory.ALPHA,
            jarvisSummary = "Symposium tonight at 7 PM. Attendance requested.",
            suggestedReplies = listOf(
                "I'll be there right at 7:00, with the prototype.",
                "Slight delay in the lab, JARVIS will prep my arrival.",
                "Already en route, save me a front row seat."
            )
        ),
        MessageRecord(
            id = "msg-2",
            senderName = "Col. James Rhodes",
            senderPhone = "+1 (555) 927-4402",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 35,
            body = "Tony, DoD wants the armor flight telemetry before tomorrow morning's debrief. Can you transmit the logs?",
            isIncoming = true,
            isRead = true,
            category = MessageCategory.DIRECTIVE,
            jarvisSummary = "Request for Mark VII flight telemetry logs for DoD debrief.",
            suggestedReplies = listOf(
                "Transmitting telemetry logs now via secure link.",
                "JARVIS is compiling the encrypted packet as we speak.",
                "Let's review the flight data over coffee tomorrow morning."
            )
        ),
        MessageRecord(
            id = "msg-3",
            senderName = "Peter Parker",
            senderPhone = "+1 (555) 392-8810",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 110,
            body = "Mr. Stark! The new web shooter pressure valves are insane! Can I drop by the workshop after school to calibrate them?",
            isIncoming = true,
            isRead = true,
            category = MessageCategory.SOCIAL,
            jarvisSummary = "Peter asks to visit the workshop after school for valve calibration.",
            suggestedReplies = listOf(
                "Lab pass unlocked for 4 PM. Don't blow up my workbench, kid.",
                "JARVIS set up a calibration bay for you. See you later.",
                "Sure thing, finish your homework first."
            )
        ),
        MessageRecord(
            id = "msg-4",
            senderName = "John",
            senderPhone = "+1 (555) 678-9012",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 220,
            body = "Hey, are you still on for the engineering review this afternoon?",
            isIncoming = true,
            isRead = true,
            category = MessageCategory.DIRECTIVE,
            jarvisSummary = "Inquiring if engineering review meeting is still on for this afternoon.",
            suggestedReplies = listOf(
                "Yes, confirmed for 3 PM.",
                "Running about 15 minutes behind, I'll be late.",
                "Can we push it to tomorrow morning?"
            )
        ),
        MessageRecord(
            id = "msg-5",
            senderName = "Mom",
            senderPhone = "+1 (555) 234-5678",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 480,
            body = "Don't forget to eat something healthy today and call me when you take a break!",
            isIncoming = true,
            isRead = true,
            category = MessageCategory.ALPHA,
            jarvisSummary = "Mom reminder to eat and call back during break.",
            suggestedReplies = listOf(
                "Calling you in 10 minutes!",
                "Will do, love you!",
                "Just had lunch, doing great!"
            )
        )
    )

    private val _calls = MutableStateFlow<List<CallRecord>>(initialCalls)
    val calls: StateFlow<List<CallRecord>> = _calls.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageRecord>>(initialMessages)
    val messages: StateFlow<List<MessageRecord>> = _messages.asStateFlow()

    fun addCall(call: CallRecord) {
        _calls.value = listOf(call) + _calls.value
    }

    fun updateCall(call: CallRecord) {
        _calls.value = _calls.value.map { if (it.id == call.id) call else it }
    }

    fun addMessage(message: MessageRecord) {
        _messages.value = listOf(message) + _messages.value
    }

    fun markMessageRead(messageId: String) {
        _messages.value = _messages.value.map {
            if (it.id == messageId) it.copy(isRead = true) else it
        }
    }

    fun sendReply(messageId: String, replyText: String) {
        _messages.value = _messages.value.map {
            if (it.id == messageId) it.copy(replySent = replyText, isRead = true) else it
        }
        // Also add outgoing message record
        val original = _messages.value.firstOrNull { it.id == messageId }
        val outgoing = MessageRecord(
            id = "out-${UUID.randomUUID()}",
            senderName = "Tony Stark (You)",
            senderPhone = original?.senderPhone ?: "",
            timestamp = System.currentTimeMillis(),
            body = replyText,
            isIncoming = false,
            isRead = true,
            category = MessageCategory.DIRECTIVE,
            jarvisSummary = "Dispatched via JARVIS smart reply"
        )
        _messages.value = listOf(outgoing) + _messages.value
    }

    fun composeNewMessage(recipientName: String, recipientPhone: String, body: String) {
        val outgoing = MessageRecord(
            id = "out-${UUID.randomUUID()}",
            senderName = "To: $recipientName",
            senderPhone = recipientPhone,
            timestamp = System.currentTimeMillis(),
            body = body,
            isIncoming = false,
            isRead = true,
            category = MessageCategory.DIRECTIVE,
            jarvisSummary = "Sent via JARVIS voice/command dispatch"
        )
        _messages.value = listOf(outgoing) + _messages.value
    }

    fun deleteCall(callId: String): Boolean {
        val exists = _calls.value.any { it.id == callId }
        if (exists) {
            _calls.value = _calls.value.filterNot { it.id == callId }
            return true
        }
        return false
    }

    fun deleteCallsByPhone(phone: String) {
        val clean = phone.replace("[^0-9]".toRegex(), "")
        _calls.value = _calls.value.filterNot {
            it.phoneNumber.replace("[^0-9]".toRegex(), "").contains(clean)
        }
    }

    fun clearAllCalls() {
        _calls.value = emptyList()
    }

    fun deleteMessage(messageId: String): Boolean {
        val exists = _messages.value.any { it.id == messageId }
        if (exists) {
            _messages.value = _messages.value.filterNot { it.id == messageId }
            return true
        }
        return false
    }

    fun clearAllMessages() {
        _messages.value = emptyList()
    }

    fun restoreDefaults() {
        _calls.value = initialCalls
        _messages.value = initialMessages
    }
}
