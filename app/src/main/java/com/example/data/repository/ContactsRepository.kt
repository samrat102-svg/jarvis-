package com.example.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.data.model.ContactRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class ContactsRepository(private val context: Context) {

    private val defaultStarkContacts = listOf(
        ContactRecord(
            id = "c-1",
            name = "Pepper Potts",
            phoneNumber = "+1 (555) 782-7501",
            email = "pepper@starkindustries.com",
            isVip = true,
            affiliation = "CEO, Stark Industries",
            notes = "Direct bypass protocol active"
        ),
        ContactRecord(
            id = "c-2",
            name = "Col. James Rhodes",
            phoneNumber = "+1 (555) 927-4402",
            email = "rhodey@usaf.mil",
            isVip = true,
            affiliation = "US Air Force / War Machine",
            notes = "Priority military clearance"
        ),
        ContactRecord(
            id = "c-3",
            name = "Happy Hogan",
            phoneNumber = "+1 (555) 438-6621",
            email = "happy@starkenterprises.com",
            isVip = true,
            affiliation = "Head of Security",
            notes = "Personal transport & asset protection"
        ),
        ContactRecord(
            id = "c-4",
            name = "Peter Parker",
            phoneNumber = "+1 (555) 392-8810",
            email = "peter.parker@midtownhigh.edu",
            isVip = true,
            affiliation = "Intern / Spider-Man",
            notes = "Suit telemetry link enabled"
        ),
        ContactRecord(
            id = "c-5",
            name = "Nick Fury",
            phoneNumber = "+1 (555) 000-0099",
            email = "fury@shield.gov",
            isVip = true,
            affiliation = "S.H.I.E.L.D. Director",
            notes = "Encrypted frequency"
        ),
        ContactRecord(
            id = "c-6",
            name = "Dr. Bruce Banner",
            phoneNumber = "+1 (555) 832-1194",
            email = "banner@starklabs.org",
            isVip = true,
            affiliation = "Gamma Research / Avengers",
            notes = "Bio-monitor telemetry"
        ),
        ContactRecord(
            id = "c-7",
            name = "Mom",
            phoneNumber = "+1 (555) 234-5678",
            email = "mom@family.net",
            isVip = true,
            affiliation = "Family",
            notes = "Highest priority"
        ),
        ContactRecord(
            id = "c-8",
            name = "John",
            phoneNumber = "+1 (555) 678-9012",
            email = "john.colleague@tech.com",
            isVip = false,
            affiliation = "Project Partner",
            notes = "Colleague on engineering specs"
        )
    )

    private val _contacts = MutableStateFlow<List<ContactRecord>>(defaultStarkContacts)
    val contacts: StateFlow<List<ContactRecord>> = _contacts.asStateFlow()

    suspend fun refreshContacts() = withContext(Dispatchers.IO) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val deviceContacts = mutableListOf<ContactRecord>()

        if (hasPermission) {
            try {
                val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone._ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )

                context.contentResolver.query(uri, projection, null, null, "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC")?.use { cursor ->
                    val idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
                    val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    while (cursor.moveToNext()) {
                        val id = if (idIndex != -1) cursor.getString(idIndex) else UUID.randomUUID().toString()
                        val name = if (nameIndex != -1) cursor.getString(nameIndex) ?: "Unknown" else "Unknown"
                        val number = if (numberIndex != -1) cursor.getString(numberIndex) ?: "" else ""

                        if (number.isNotBlank()) {
                            deviceContacts.add(
                                ContactRecord(
                                    id = "device-$id",
                                    name = name,
                                    phoneNumber = number,
                                    isVip = false,
                                    affiliation = "Device Contact",
                                    notes = "Synced from Android Contacts"
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Graceful fallback to default contacts
            }
        }

        // Merge device contacts with Stark contacts (avoid duplicate names)
        val combined = (defaultStarkContacts + deviceContacts)
            .distinctBy { it.phoneNumber.replace("[^0-9]".toRegex(), "") }
            .sortedByDescending { it.isVip }

        _contacts.value = combined
    }

    fun findContactByName(query: String): ContactRecord? {
        val cleanQuery = query.trim().lowercase()
        return _contacts.value.firstOrNull { contact ->
            val contactName = contact.name.lowercase()
            contactName == cleanQuery ||
                    contactName.contains(cleanQuery) ||
                    cleanQuery.contains(contactName)
        }
    }

    fun findContactByPhone(number: String): ContactRecord? {
        val cleanInput = number.replace("[^0-9]".toRegex(), "")
        if (cleanInput.isEmpty()) return null
        return _contacts.value.firstOrNull { contact ->
            val cleanContact = contact.phoneNumber.replace("[^0-9]".toRegex(), "")
            cleanContact.endsWith(cleanInput) || cleanInput.endsWith(cleanContact)
        }
    }

    fun addContact(name: String, phone: String, isVip: Boolean = false, affiliation: String = "") {
        val newContact = ContactRecord(
            id = "c-${UUID.randomUUID()}",
            name = name,
            phoneNumber = phone,
            isVip = isVip,
            affiliation = affiliation.ifBlank { "Personal Contact" },
            notes = "Added via Jarvis Protocol"
        )
        _contacts.value = listOf(newContact) + _contacts.value
    }

    fun deleteContact(id: String): Boolean {
        val exists = _contacts.value.any { it.id == id }
        if (exists) {
            _contacts.value = _contacts.value.filterNot { it.id == id }
            return true
        }
        return false
    }

    fun deleteContactByQuery(query: String): ContactRecord? {
        val cleanQuery = query.trim().lowercase()
        val match = _contacts.value.firstOrNull { contact ->
            contact.name.lowercase().contains(cleanQuery) ||
                    contact.phoneNumber.replace("[^0-9]".toRegex(), "").contains(cleanQuery.replace("[^0-9]".toRegex(), "")) ||
                    cleanQuery.contains(contact.name.lowercase())
        }
        if (match != null) {
            _contacts.value = _contacts.value.filterNot { it.id == match.id }
        }
        return match
    }

    fun clearAllContacts() {
        _contacts.value = emptyList()
    }

    fun restoreDefaults() {
        _contacts.value = defaultStarkContacts
    }
}
