package com.cometchat.calendarintegration

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.gson.Gson

class EventDetailsActivity : AppCompatActivity() {
    private lateinit var credential: GoogleAccountCredential
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)
        val gson = Gson()
        val credentialJson = intent?.getStringExtra("credential")
        credential = gson.fromJson(credentialJson, GoogleAccountCredential::class.java)

        val createEventButton: Button = findViewById(R.id.createEventButton)
        val eventTitleEditText: EditText = findViewById(R.id.eventTitleEditText)
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)

        createEventButton.setOnClickListener {
            val eventTitle: String = eventTitleEditText.text.toString()
            val description: String = descriptionEditText.text.toString()

            if (eventTitle.isNotEmpty() && description.isNotEmpty()) {
                createCalendarEvent(eventTitle, description)
            }
        }
    }

    private fun createCalendarEvent(eventTitle: String, description: String) {
        val event = Event()
            .setSummary(eventTitle)
            // .setLocation("800 Howard St., San Francisco, CA 94103")
            .setDescription(description)

        val startDateTime = DateTime("2024-05-28T09:00:00-07:00")
        val start = EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone("America/Los_Angeles")
        event.start = start

        val endDateTime = DateTime("2024-05-28T17:00:00-07:00")
        val end = EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone("America/Los_Angeles")
        event.end = end

        val service = Calendar.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Calendar Integration")
            .build()

        AsyncTask.execute {
            try {
                val calendarEvent = service.events().insert("primary", event).execute()
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Event created: ${calendarEvent.htmlLink}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: UserRecoverableAuthIOException) {
                startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}