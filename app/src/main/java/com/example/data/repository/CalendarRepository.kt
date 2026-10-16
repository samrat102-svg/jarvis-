package com.example.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.example.data.model.CalendarEventRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID

class CalendarRepository(private val context: Context) {

    private val defaultStarkEvents: List<CalendarEventRecord>
        get() {
            val now = Calendar.getInstance()

            val cal1 = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 2)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val cal1End = (cal1.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, 1) }

            val cal2 = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 5)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
            }
            val cal2End = (cal2.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, 1) }

            val cal3 = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val cal3End = (cal3.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, 2) }

            return listOf(
                CalendarEventRecord(
                    id = "cal-1",
                    title = "Stark Board of Directors Briefing",
                    description = "Quarterly clean energy tech presentation and arc reactor infrastructure review.",
                    startTimeMillis = cal1.timeInMillis,
                    endTimeMillis = cal1End.timeInMillis,
                    location = "Stark Tower Conference Hall",
                    isJarvisScheduled = true
                ),
                CalendarEventRecord(
                    id = "cal-2",
                    title = "Flight Repulsor Calibration & Diagnostics",
                    description = "Wind tunnel test with Mark VII telemetry telemetry sync.",
                    startTimeMillis = cal2.timeInMillis,
                    endTimeMillis = cal2End.timeInMillis,
                    location = "Workshop Lab 4",
                    isJarvisScheduled = true
                ),
                CalendarEventRecord(
                    id = "cal-3",
                    title = "Dinner with Pepper",
                    description = "Reservation confirmed at Le Bernardin.",
                    startTimeMillis = cal3.timeInMillis,
                    endTimeMillis = cal3End.timeInMillis,
                    location = "Downtown Manhattan",
                    isJarvisScheduled = false
                )
            )
        }

    private val _events = MutableStateFlow<List<CalendarEventRecord>>(emptyList())
    val events: StateFlow<List<CalendarEventRecord>> = _events.asStateFlow()

    init {
        _events.value = defaultStarkEvents
    }

    suspend fun refreshCalendar() = withContext(Dispatchers.IO) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        val deviceEvents = mutableListOf<CalendarEventRecord>()

        if (hasPermission) {
            try {
                val projection = arrayOf(
                    CalendarContract.Events._ID,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DESCRIPTION,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND,
                    CalendarContract.Events.EVENT_LOCATION
                )

                val uri = CalendarContract.Events.CONTENT_URI
                val selection = "${CalendarContract.Events.DTSTART} >= ?"
                val selectionArgs = arrayOf(System.currentTimeMillis().toString())
                val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

                context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                    val idCol = cursor.getColumnIndex(CalendarContract.Events._ID)
                    val titleCol = cursor.getColumnIndex(CalendarContract.Events.TITLE)
                    val descCol = cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION)
                    val startCol = cursor.getColumnIndex(CalendarContract.Events.DTSTART)
                    val endCol = cursor.getColumnIndex(CalendarContract.Events.DTEND)
                    val locCol = cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)

                    while (cursor.moveToNext()) {
                        val id = if (idCol != -1) cursor.getString(idCol) else UUID.randomUUID().toString()
                        val title = if (titleCol != -1) cursor.getString(titleCol) ?: "Event" else "Event"
                        val desc = if (descCol != -1) cursor.getString(descCol) ?: "" else ""
                        val start = if (startCol != -1) cursor.getLong(startCol) else System.currentTimeMillis()
                        val end = if (endCol != -1) cursor.getLong(endCol) else start + 3600000L
                        val loc = if (locCol != -1) cursor.getString(locCol) ?: "" else ""

                        deviceEvents.add(
                            CalendarEventRecord(
                                id = "dev-$id",
                                title = title,
                                description = desc,
                                startTimeMillis = start,
                                endTimeMillis = end,
                                location = loc,
                                isJarvisScheduled = false
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Keep default events
            }
        }

        val currentEvents = _events.value
        val merged = (currentEvents + deviceEvents)
            .distinctBy { "${it.title}_${it.startTimeMillis}" }
            .sortedBy { it.startTimeMillis }

        _events.value = merged
    }

    suspend fun scheduleEvent(
        title: String,
        description: String = "Scheduled via JARVIS protocol",
        startTimeMillis: Long,
        endTimeMillis: Long = startTimeMillis + 3600000L,
        location: String = ""
    ): CalendarEventRecord = withContext(Dispatchers.IO) {
        val newRecord = CalendarEventRecord(
            id = "jarvis-${UUID.randomUUID()}",
            title = title,
            description = description,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            location = location,
            isJarvisScheduled = true
        )

        // Try writing to system calendar if permission is present
        val hasWritePermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        if (hasWritePermission) {
            try {
                val values = ContentValues().apply {
                    put(CalendarContract.Events.DTSTART, startTimeMillis)
                    put(CalendarContract.Events.DTEND, endTimeMillis)
                    put(CalendarContract.Events.TITLE, title)
                    put(CalendarContract.Events.DESCRIPTION, description)
                    put(CalendarContract.Events.CALENDAR_ID, 1) // Primary calendar ID
                    put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                    if (location.isNotBlank()) {
                        put(CalendarContract.Events.EVENT_LOCATION, location)
                    }
                }
                context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            } catch (e: Exception) {
                // Ignore and keep local in app
            }
        }

        _events.value = (_events.value + newRecord).sortedBy { it.startTimeMillis }
        newRecord
    }

    fun deleteEvent(id: String): Boolean {
        val exists = _events.value.any { it.id == id }
        if (exists) {
            _events.value = _events.value.filterNot { it.id == id }
            return true
        }
        return false
    }

    fun deleteEventsByTitle(query: String): CalendarEventRecord? {
        val clean = query.trim().lowercase()
        val match = _events.value.firstOrNull {
            it.title.lowercase().contains(clean) || clean.contains(it.title.lowercase())
        }
        if (match != null) {
            _events.value = _events.value.filterNot { it.id == match.id }
        }
        return match
    }

    fun clearAllEvents() {
        _events.value = emptyList()
    }

    fun restoreDefaults() {
        _events.value = defaultStarkEvents
    }
}
