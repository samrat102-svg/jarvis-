package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentStatus
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDim
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.JarvisGreenSuccess
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VoicePayScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val balance by viewModel.paymentRepository.balanceUsd.collectAsState()
    val transactions by viewModel.paymentRepository.transactions.collectAsState()
    val activeDraft by viewModel.paymentRepository.activeDraft.collectAsState()
    val voiceProfile by viewModel.voiceBiometricsManager.profile.collectAsState()
    val lastRecognition by viewModel.voiceBiometricsManager.lastRecognition.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "STARK ARCPAY • VOICE DISPATCH TERMINAL",
                    color = ArcCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Zero-Trust Biometric Voice Payment Protocol",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Balance Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StarkGold.copy(alpha = 0.7f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = StarkGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STARK ENTERPRISES RESERVE",
                                color = StarkGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(JarvisGreenSuccess.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CRYPTOGRAPHICALLY SECURE",
                                color = JarvisGreenSuccess,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", balance)}",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Speaker Verified: ${lastRecognition.speakerName} (${lastRecognition.confidencePercent}% match)",
                            color = ArcCyanDim,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Active Payment Confirmation Card (when user gave voice pay command)
        activeDraft?.let { draft ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, ArcCyanGlow, RoundedCornerShape(14.dp))
                        .testTag("active_payment_confirmation_card"),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(ArcCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VOICE AUTHORIZATION REQUIRED",
                                    color = ArcCyanGlow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            IconButton(
                                onClick = { viewModel.paymentRepository.cancelPaymentDraft() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(
                            text = "Transfer $${String.format(Locale.US, "%.2f", draft.amount)} to ${draft.recipientName}",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Account: ${draft.recipientAccountOrUpi}\nNote: ${draft.note}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "Say \"Confirm\" or \"Authorize\", or tap below to disburse funds.",
                            color = ArcCyanDim,
                            fontSize = 11.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.confirmVoicePayment()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("authorize_payment_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Authorize Transfer", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.paymentRepository.cancelPaymentDraft() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(0.7f)
                            ) {
                                Text("Dismiss", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Voice Command Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderCyan, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VOICE PAYMENT SYNTAX",
                            color = ArcCyanGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• \"Pay $50 to Pepper Potts\"\n• \"Send 100 to Happy Hogan\"\n• \"Transfer $250 to Rhodey\"\n• \"पेप्पर को 500 रुपये भेजो\"",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Quick Pay Dispatch Buttons
        item {
            Text(
                text = "INSTANT VOICE DISPATCH PRESETS",
                color = ArcCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val presets = listOf(
                Pair("Pepper Potts", 50.0),
                Pair("Happy Hogan", 120.0),
                Pair("Col. Rhodes", 250.0),
                Pair("Clean Energy", 80.0)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { (name, amt) ->
                    Button(
                        onClick = {
                            viewModel.initiateVoicePayment(name, amt)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(name.substringBefore(" "), color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$$amt", color = ArcCyanGlow, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Transaction History Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISBURSED TRANSACTIONS (${transactions.size})",
                    color = ArcCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                if (transactions.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.paymentRepository.clearPaymentHistory() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear History", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Transactions List
        items(transactions, key = { it.id }) { tx ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderCyan, RoundedCornerShape(10.dp))
                    .testTag("tx_item_${tx.id}"),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(JarvisGreenSuccess.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = JarvisGreenSuccess, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tx.recipientName,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${tx.referenceCode} • ${SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(tx.timestamp))}",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = tx.note,
                                color = ArcCyanDim,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "-$${String.format(Locale.US, "%.2f", tx.amount)}",
                            color = ArcCyanGlow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(
                            onClick = { viewModel.paymentRepository.deleteTransaction(tx.id) },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Tx", tint = TextSecondary.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
