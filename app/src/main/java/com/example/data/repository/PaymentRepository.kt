package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.PaymentDraft
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PaymentRepository(private val context: Context) {

    private val initialTransactions = listOf(
        PaymentTransaction(
            id = "tx-1",
            recipientName = "Pepper Potts",
            recipientAccountOrUpi = "pepper@starkindustries.com",
            amount = 50.0,
            currency = "USD",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
            status = PaymentStatus.COMPLETED,
            referenceCode = "STARK-PAY-9812A",
            note = "Executive luncheon catering disbursement",
            isVoiceAuthorized = true
        ),
        PaymentTransaction(
            id = "tx-2",
            recipientName = "Happy Hogan",
            recipientAccountOrUpi = "happy.assetprotection@stark.com",
            amount = 120.0,
            currency = "USD",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 180,
            status = PaymentStatus.COMPLETED,
            referenceCode = "STARK-PAY-4402B",
            note = "Audi R8 workshop maintenance fuel",
            isVoiceAuthorized = true
        ),
        PaymentTransaction(
            id = "tx-3",
            recipientName = "Stark Clean Energy Grid",
            recipientAccountOrUpi = "grid-ny@cleanenergy.stark.org",
            amount = 350.0,
            currency = "USD",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 14,
            status = PaymentStatus.COMPLETED,
            referenceCode = "STARK-PAY-1109C",
            note = "Arc Reactor sub-station turbine recharge",
            isVoiceAuthorized = true
        )
    )

    private val _transactions = MutableStateFlow<List<PaymentTransaction>>(initialTransactions)
    val transactions: StateFlow<List<PaymentTransaction>> = _transactions.asStateFlow()

    // Active Stark Pay Account balance
    private val _balanceUsd = MutableStateFlow(24850.0)
    val balanceUsd: StateFlow<Double> = _balanceUsd.asStateFlow()

    // Current pending draft awaiting user voice confirmation
    private val _activeDraft = MutableStateFlow<PaymentDraft?>(null)
    val activeDraft: StateFlow<PaymentDraft?> = _activeDraft.asStateFlow()

    fun createPaymentDraft(
        recipient: String,
        amount: Double,
        currency: String = "USD",
        note: String = "Stark ArcPay Voice Transfer",
        recipientAccount: String = ""
    ): PaymentDraft {
        val targetAccount = recipientAccount.ifBlank {
            when {
                recipient.lowercase().contains("pepper") -> "pepper@starkindustries.com"
                recipient.lowercase().contains("happy") -> "happy.security@stark.com"
                recipient.lowercase().contains("rhodey") || recipient.lowercase().contains("rhodes") -> "rhodey@usaf.mil"
                recipient.lowercase().contains("peter") -> "peter.parker@mit.edu"
                recipient.lowercase().contains("fury") -> "director@shield.gov"
                else -> "${recipient.lowercase().replace(" ", "")}@arcpay.network"
            }
        }
        val draft = PaymentDraft(
            recipientName = recipient,
            recipientAccountOrUpi = targetAccount,
            amount = amount,
            currency = currency,
            note = note,
            isAwaitingConfirmation = true
        )
        _activeDraft.value = draft
        return draft
    }

    fun confirmPayment(draft: PaymentDraft): PaymentTransaction {
        val refCode = "STARK-PAY-${UUID.randomUUID().toString().take(8).uppercase()}"
        val transaction = PaymentTransaction(
            id = "tx-${UUID.randomUUID()}",
            recipientName = draft.recipientName,
            recipientAccountOrUpi = draft.recipientAccountOrUpi,
            amount = draft.amount,
            currency = draft.currency,
            timestamp = System.currentTimeMillis(),
            status = PaymentStatus.COMPLETED,
            referenceCode = refCode,
            note = draft.note,
            isVoiceAuthorized = true
        )

        // Deduct from balance
        _balanceUsd.value = (_balanceUsd.value - draft.amount).coerceAtLeast(0.0)
        _transactions.value = listOf(transaction) + _transactions.value
        _activeDraft.value = null

        return transaction
    }

    fun cancelPaymentDraft() {
        _activeDraft.value = null
    }

    fun launchUpiPaymentIntent(transaction: PaymentTransaction): Boolean {
        try {
            val amountFormatted = String.format(Locale.US, "%.2f", transaction.amount)
            val upiUri = Uri.parse(
                "upi://pay?pa=${Uri.encode(transaction.recipientAccountOrUpi)}" +
                        "&pn=${Uri.encode(transaction.recipientName)}" +
                        "&mc=0000" +
                        "&tr=${transaction.referenceCode}" +
                        "&tn=${Uri.encode(transaction.note)}" +
                        "&am=$amountFormatted" +
                        "&cu=${if (transaction.currency == "INR" || transaction.currency == "₹") "INR" else "INR"}"
            )
            val intent = Intent(Intent.ACTION_VIEW, upiUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun deleteTransaction(id: String): Boolean {
        val exists = _transactions.value.any { it.id == id }
        if (exists) {
            _transactions.value = _transactions.value.filterNot { it.id == id }
            return true
        }
        return false
    }

    fun clearPaymentHistory() {
        _transactions.value = emptyList()
    }
}
