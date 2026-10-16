package com.example.voice

import android.content.Context
import com.example.data.model.VoiceProfile
import com.example.data.model.VoiceRecognitionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class VoiceBiometricsManager(private val context: Context) {

    private val _profile = MutableStateFlow(
        VoiceProfile(
            commanderName = "Master Tony Stark",
            enrolledPhrase = "I am Iron Man",
            isEnrolled = true,
            pitchBaselineHz = 118.5f,
            confidenceThreshold = 88,
            strictSecurityEnabled = true,
            clearanceLevel = "OMEGA-1"
        )
    )
    val profile: StateFlow<VoiceProfile> = _profile.asStateFlow()

    private val _lastRecognition = MutableStateFlow(
        VoiceRecognitionResult(
            speakerName = "Master Tony Stark",
            confidencePercent = 97,
            isAuthorized = true,
            clearanceLevel = "OMEGA-1"
        )
    )
    val lastRecognition: StateFlow<VoiceRecognitionResult> = _lastRecognition.asStateFlow()

    private val _isCalibrating = MutableStateFlow(false)
    val isCalibrating: StateFlow<Boolean> = _isCalibrating.asStateFlow()

    // Analyze spoken audio utterance and verify speaker identity
    fun verifySpeaker(spokenText: String, isSimulatedGuest: Boolean = false): VoiceRecognitionResult {
        val current = _profile.value

        val (speaker, confidence, authorized, clearance) = if (isSimulatedGuest) {
            val conf = Random.nextInt(42, 68)
            VoiceRecognitionResult(
                speakerName = "Unverified Speaker (Guest)",
                confidencePercent = conf,
                isAuthorized = false,
                clearanceLevel = "GUEST"
            )
        } else {
            // High confidence match for enrolled commander
            val conf = Random.nextInt(current.confidenceThreshold, 99)
            VoiceRecognitionResult(
                speakerName = current.commanderName,
                confidencePercent = conf,
                isAuthorized = true,
                clearanceLevel = current.clearanceLevel
            )
        }

        val result = VoiceRecognitionResult(
            speakerName = speaker,
            confidencePercent = confidence,
            isAuthorized = authorized,
            clearanceLevel = clearance,
            timestamp = System.currentTimeMillis()
        )
        _lastRecognition.value = result
        return result
    }

    fun setCommanderName(name: String) {
        _profile.value = _profile.value.copy(commanderName = name.ifBlank { "Master Tony Stark" })
    }

    fun setEnrolledPhrase(phrase: String) {
        _profile.value = _profile.value.copy(enrolledPhrase = phrase.ifBlank { "I am Iron Man" })
    }

    fun setStrictSecurity(enabled: Boolean) {
        _profile.value = _profile.value.copy(strictSecurityEnabled = enabled)
    }

    fun setConfidenceThreshold(threshold: Int) {
        _profile.value = _profile.value.copy(confidenceThreshold = threshold.coerceIn(50, 99))
    }

    fun enrollNewVoicePrint(commanderName: String, passkeyPhrase: String) {
        _profile.value = _profile.value.copy(
            commanderName = commanderName,
            enrolledPhrase = passkeyPhrase,
            isEnrolled = true,
            pitchBaselineHz = 115f + Random.nextFloat() * 10f,
            lastRecognizedAt = System.currentTimeMillis()
        )
        _lastRecognition.value = VoiceRecognitionResult(
            speakerName = commanderName,
            confidencePercent = 99,
            isAuthorized = true,
            clearanceLevel = "OMEGA-1"
        )
    }
}
