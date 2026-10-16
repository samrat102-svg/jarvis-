package com.example.ai

import com.example.BuildConfig
import com.example.data.model.PresentationSlide
import com.example.data.model.SpreadsheetData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiJarvisService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateSmartReplies(senderName: String, messageText: String): List<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are J.A.R.V.I.S., Tony Stark's AI assistant. 
                    A message was received from "$senderName": "$messageText".
                    Generate exactly 3 short, contextually relevant, ready-to-send replies for the user.
                    If the message is in Hindi or Hinglish, generate replies in the matching language/script.
                    Return ONLY a JSON array of 3 strings, e.g. ["Reply 1", "Reply 2", "Reply 3"].
                """.trimIndent()

                val response = callGeminiApi(apiKey, prompt)
                val clean = response.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val jsonArray = JSONArray(clean)
                val list = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    list.add(jsonArray.getString(i))
                }
                if (list.size >= 2) return@withContext list.take(3)
            } catch (e: Exception) {
                // Fallback to local heuristic
            }
        }
        fallbackSmartReplies(messageText)
    }

    suspend fun screenIncomingCall(callerName: String, callerSpeech: String): Pair<String, String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are J.A.R.V.I.S., Tony Stark's AI butler and security system.
                    An incoming caller "$callerName" says: "$callerSpeech".
                    Respond as JARVIS in 2 parts:
                    1. What JARVIS says to the caller (calm, British or matching caller's language, authoritative, polite).
                    2. Brief 1-sentence recommendation for the user (e.g. "Telemarketer pitching loans, recommend terminating" or "Urgent delivery request, recommend taking call").
                    Format as JSON: {"jarvisSpeech": "...", "recommendation": "..."}
                """.trimIndent()

                val response = callGeminiApi(apiKey, prompt)
                val clean = response.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val obj = JSONObject(clean)
                val speech = obj.optString("jarvisSpeech", "Mr. Stark is unavailable at the moment. May I take a message?")
                val rec = obj.optString("recommendation", "Caller screened. Awaiting your directive, sir.")
                return@withContext Pair(speech, rec)
            } catch (e: Exception) {
                // Fallback
            }
        }
        fallbackScreening(callerSpeech)
    }

    suspend fun askJarvis(query: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are J.A.R.V.I.S., the hyper-intelligent AI assistant created by Tony Stark (Iron Man).
                    Speak with suave British eloquence, dry wit, impeccable loyalty, and technical precision.
                    User directive/query: "$query"
                    CRITICAL LANGUAGE DIRECTIVE:
                    - If the user speaks in Hindi (हिन्दी), respond in polite, respectful, and sophisticated Hindi (e.g. "जी सर, मार्क 85 प्रणाली आपके निर्देश के अनुसार सक्रिय है।").
                    - If the user speaks in Hinglish or any other language (Spanish, French, German, Japanese, etc.), respond in that exact language with JARVIS's signature loyal butler tone.
                    - Keep your spoken answer concise (1 to 2 sentences max) suitable for audio headset transmission.
                """.trimIndent()

                val response = callGeminiApi(apiKey, prompt)
                if (response.isNotBlank()) return@withContext response.trim()
            } catch (e: Exception) {
                // Fallback to local
            }
        }
        fallbackJarvisQuery(query)
    }

    // AI Document Generation: PPT slides on any topic
    suspend fun generatePptDeck(topic: String, details: String): List<PresentationSlide>? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext null
        try {
            val prompt = """
                You are J.A.R.V.I.S. compiling an executive Stark presentation deck on "$topic".
                Details: "$details".
                Generate a 4-slide presentation. Output ONLY a JSON array of objects with:
                [
                  {
                    "slideNumber": 1,
                    "title": "Title here",
                    "subtitle": "Subtitle",
                    "bulletPoints": ["Point 1", "Point 2", "Point 3"],
                    "speakerNotes": "Speaker notes"
                  }
                ]
            """.trimIndent()

            val response = callGeminiApi(apiKey, prompt)
            val clean = response.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val array = JSONArray(clean)
            val list = mutableListOf<PresentationSlide>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val bpArray = obj.getJSONArray("bulletPoints")
                val bullets = mutableListOf<String>()
                for (j in 0 until bpArray.length()) {
                    bullets.add(bpArray.getString(j))
                }
                list.add(
                    PresentationSlide(
                        slideNumber = obj.optInt("slideNumber", i + 1),
                        title = obj.getString("title"),
                        subtitle = obj.optString("subtitle", ""),
                        bulletPoints = bullets,
                        speakerNotes = obj.optString("speakerNotes", "")
                    )
                )
            }
            if (list.isNotEmpty()) list else null
        } catch (e: Exception) {
            null
        }
    }

    // AI Document Generation: Excel spreadsheet data matrix
    suspend fun generateExcelMatrix(topic: String, details: String): SpreadsheetData? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext null
        try {
            val prompt = """
                You are J.A.R.V.I.S. creating a financial/operational data matrix for "$topic".
                Output ONLY JSON:
                {
                  "sheetName": "Stark_Matrix",
                  "headers": ["Item", "Division", "Budget", "Actual", "Variance", "Status"],
                  "rows": [
                    ["Row 1 Col 1", "Div A", "$100,000", "$90,000", "+$10,000", "ACTIVE"],
                    ["Row 2 Col 1", "Div B", "$250,000", "$230,000", "+$20,000", "NOMINAL"]
                  ],
                  "summaryFooter": "Net Surplus: +$30,000"
                }
            """.trimIndent()

            val response = callGeminiApi(apiKey, prompt)
            val clean = response.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = JSONObject(clean)
            val headersArray = obj.getJSONArray("headers")
            val headers = mutableListOf<String>()
            for (i in 0 until headersArray.length()) headers.add(headersArray.getString(i))

            val rowsArray = obj.getJSONArray("rows")
            val rows = mutableListOf<List<String>>()
            for (i in 0 until rowsArray.length()) {
                val rArray = rowsArray.getJSONArray(i)
                val row = mutableListOf<String>()
                for (j in 0 until rArray.length()) row.add(rArray.getString(j))
                rows.add(row)
            }
            SpreadsheetData(
                sheetName = obj.optString("sheetName", "Data_Matrix"),
                headers = headers,
                rows = rows,
                summaryFooter = obj.optString("summaryFooter", "Compiled by J.A.R.V.I.S.")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun callGeminiApi(apiKey: String, prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val bodyJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)
        }

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${response.message}")
            }
            val resString = response.body?.string() ?: throw Exception("Empty response body")
            val root = JSONObject(resString)
            val candidates = root.getJSONArray("candidates")
            val first = candidates.getJSONObject(0)
            val content = first.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            return parts.getJSONObject(0).getString("text")
        }
    }

    private fun fallbackSmartReplies(messageText: String): List<String> {
        val text = messageText.lowercase()
        return when {
            text.contains("late") || text.contains("running behind") || text.contains("लेट") -> listOf(
                "No problem, take your time.",
                "Understood. Let me know when you arrive.",
                "कोई बात नहीं, आराम से आइए।"
            )
            text.contains("meeting") || text.contains("review") || text.contains("still on") || text.contains("मीटिंग") -> listOf(
                "Yes, confirmed. See you then.",
                "Running slightly late, I'll be there in 10 minutes.",
                "हाँ, मैं समय पर उपस्थित रहूँगा।"
            )
            text.contains("tonight") || text.contains("dinner") || text.contains("symposium") || text.contains("आज रात") -> listOf(
                "I'll be there right on time.",
                "Slight delay in the workshop, but on my way.",
                "Sounds wonderful. Looking forward to it."
            )
            text.contains("where are you") || text.contains("status") || text.contains("कहाँ हो") -> listOf(
                "In the workshop, wrapping up shortly.",
                "En route right now.",
                "रास्ते में हूँ, 5 मिनट में पहुँच रहा हूँ।"
            )
            text.contains("call me") || text.contains("call") || text.contains("कॉल") -> listOf(
                "Calling you right now.",
                "In a meeting, can I call you in 15 minutes?",
                "बस 5 मिनट में कॉल करता हूँ।"
            )
            else -> listOf(
                "Understood. I will handle it immediately.",
                "Sounds good, keep me posted.",
                "संदेश प्राप्त हुआ, तुरंत देख रहा हूँ।"
            )
        }
    }

    private fun fallbackScreening(callerSpeech: String): Pair<String, String> {
        val lower = callerSpeech.lowercase()
        return when {
            lower.contains("warranty") || lower.contains("loan") || lower.contains("offer") || lower.contains("insurance") || lower.contains("timeshare") || lower.contains("लोन") -> {
                Pair(
                    "I am afraid Mr. Stark is indisposed and not interested in unsolicited commercial proposals. Good day.",
                    "Telemarketing solicitation detected. Recommend immediate dismissal to spam quarantine."
                )
            }
            lower.contains("urgent") || lower.contains("emergency") || lower.contains("help") || lower.contains("ज़रूरी") -> {
                Pair(
                    "This is J.A.R.V.I.S. Patching your audio through with emergency priority to Mr. Stark now.",
                    "Urgent caller flagging detected. Recommend accepting call immediately."
                )
            }
            lower.contains("delivery") || lower.contains("package") || lower.contains("gate") || lower.contains("पार्सल") -> {
                Pair(
                    "Logistics delivery recognized. Access code dispatched to Stark security. Stand by.",
                    "Legitimate delivery courier. Recommend approving gate clearance."
                )
            }
            else -> {
                Pair(
                    "Greetings. You have reached Mr. Stark's private executive protocol. State your name and business clearly.",
                    "Caller screening in progress. Awaiting your decision to accept or divert."
                )
            }
        }
    }

    private fun fallbackJarvisQuery(query: String): String {
        val q = query.lowercase()
        return when {
            // Hindi queries
            q.contains("नमस्ते") || q.contains("का हाल") || q.contains("हालचाल") || q.contains("कैसा है") ->
                "नमस्ते सर। जारविस आपकी सेवा में हाज़िर है। सभी रक्षा और संचार प्रणालियां 100% क्षमता पर काम कर रही हैं।"
            q.contains("स्टेटस") || q.contains("हाल बताओ") || q.contains("कवर") || q.contains("सुरक्षा") ->
                "कोर तापमान 34°C पर सामान्य है। बिजली उत्पादन 100%। रक्षा शील्ड और संचार नेटवर्क पूरी तरह सक्रिय हैं।"
            q.contains("किसका कॉल") || q.contains("कॉल आया") ->
                "आपके पास 4 हालिया कॉल रिकॉर्ड हैं, जिसमें मिस पॉट्स का एक प्राथमिकता कॉल भी शामिल है।"
            q.contains("मैसेज") && (q.contains("पढ़ो") || q.contains("दिखाओ")) ->
                "आपके पास मिस पॉट्स का एक महत्वपूर्ण संदेश है जो आज रात के सम्मेलन के बारे में है।"
            q.contains("शुक्रिया") || q.contains("धन्यवाद") ->
                "यह तो मेरा परम सौभाग्य है, सर।"
            q.contains("पीपीटी") || q.contains("प्रेजेंटेशन") ->
                "जी सर, आपके विषय पर पावरपॉइंट प्रेजेंटेशन स्लाइड्स तैयार कर दी गई हैं।"
            q.contains("पीडीएफ") || q.contains("रिपोर्ट") ->
                "पीडीएफ ऑडिट रिपोर्ट संकलित कर दी गई है और फाइल्स में सुरक्षित है।"

            // English queries
            q.contains("hello") || q.contains("hi jarvis") || q.contains("hey jarvis") ->
                "At your service, sir. All defense, communications, and power grids operating at peak efficiency."
            q.contains("diagnostic") || q.contains("status") || q.contains("armor") ->
                "Core temperature nominal at 34 Celsius. Power output 100%. Communications array online and screening active."
            q.contains("who called") || q.contains("call log") ->
                "You have 4 recent call records, including one intercepted telemarketer and a priority contact from Miss Potts."
            q.contains("message") || q.contains("unread") ->
                "You have 1 priority directive from Miss Potts regarding the symposium tonight."
            q.contains("schedule") || q.contains("calendar") ->
                "Your next engagement is the Stark Board of Directors briefing in approximately two hours."
            q.contains("ppt") || q.contains("presentation") ->
                "I have initialized and rendered the presentation deck in Stark Document Studio, sir."
            q.contains("pdf") || q.contains("report") ->
                "Executive PDF report generated and cryptographically signed, sir."
            else ->
                "Right away, sir. Systems are standing by for your next directive."
        }
    }
}
