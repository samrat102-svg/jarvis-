package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDim
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import kotlinx.coroutines.launch

@Composable
fun ContactsScreen(
    viewModel: JarvisViewModel,
    onRequestContactsPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contacts by viewModel.contactsRepository.contacts.collectAsState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newAffiliation by remember { mutableStateOf("") }
    var newIsVip by remember { mutableStateOf(false) }

    val filtered = contacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery) ||
                it.affiliation.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STARK CONTACTS DIRECTORY",
                            color = ArcCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Caller ID & Voice Dispatch Clearance",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            onRequestContactsPermission()
                            scope.launch {
                                viewModel.contactsRepository.refreshContacts()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("sync_device_contacts_button")
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Device", color = ArcCyanGlow, fontSize = 11.sp)
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, affiliation, or number...", fontSize = 12.sp, color = TextSecondary) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contacts_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = BorderCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Contact Cards
            items(filtered, key = { it.id }) { contact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (contact.isVip) StarkGold.copy(alpha = 0.6f) else BorderCyan,
                            RoundedCornerShape(12.dp)
                        )
                        .testTag("contact_item_${contact.id}"),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (contact.isVip) StarkGold.copy(alpha = 0.2f) else JarvisSurfaceElevated
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (contact.isVip) Icons.Default.Star else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (contact.isVip) StarkGold else ArcCyanGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = contact.name,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (contact.isVip) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(StarkGold.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "VIP",
                                                color = StarkGold,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = contact.phoneNumber,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                if (contact.affiliation.isNotBlank()) {
                                    Text(
                                        text = contact.affiliation,
                                        color = ArcCyanDim,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Call & Message quick actions
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = {
                                    viewModel.executeVoiceCommand("Call ${contact.name}")
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ArcCyan)
                                    .testTag("call_contact_${contact.id}")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.Black, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = {
                                    try {
                                        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("smsto:${Uri.encode(contact.phoneNumber)}")
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(smsIntent)
                                    } catch (e: Exception) {
                                        // Handled
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(JarvisSurfaceElevated)
                                    .testTag("msg_contact_${contact.id}")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Message", tint = ArcCyanGlow, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = {
                                    viewModel.deleteContact(contact.id)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(JarvisRedAlert.copy(alpha = 0.15f))
                                    .testTag("delete_contact_${contact.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Contact", tint = JarvisRedAlert, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // FAB to add new contact
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = ArcCyan,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_contact_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Contact")
        }

        // Add Contact Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text(
                        text = "ENROLL CONTACT CLEARANCE",
                        color = ArcCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth().testTag("new_contact_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = BorderCyan,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newPhone,
                            onValueChange = { newPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth().testTag("new_contact_phone_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = BorderCyan,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newAffiliation,
                            onValueChange = { newAffiliation = it },
                            label = { Text("Affiliation / Role") },
                            modifier = Modifier.fillMaxWidth().testTag("new_contact_affiliation_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = BorderCyan,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { newIsVip = !newIsVip }
                        ) {
                            Checkbox(
                                checked = newIsVip,
                                onCheckedChange = { newIsVip = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = StarkGold,
                                    checkmarkColor = Color.Black
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Designate as Stark VIP (Auto-Bypass DND)",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newName.isNotBlank() && newPhone.isNotBlank()) {
                                viewModel.contactsRepository.addContact(
                                    name = newName,
                                    phone = newPhone,
                                    isVip = newIsVip,
                                    affiliation = newAffiliation
                                )
                                viewModel.voiceManager.speak("Contact clearance enrolled for $newName, sir.")
                                showAddDialog = false
                                newName = ""
                                newPhone = ""
                                newAffiliation = ""
                                newIsVip = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                        modifier = Modifier.testTag("submit_add_contact_button")
                    ) {
                        Text("Save Contact", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = JarvisSurfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
