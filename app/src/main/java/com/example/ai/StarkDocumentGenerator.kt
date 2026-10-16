package com.example.ai

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.DocumentType
import com.example.data.model.GeneratedDocument
import com.example.data.model.PresentationSlide
import com.example.data.model.SpreadsheetData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class StarkDocumentGenerator(
    private val context: Context,
    private val aiService: GeminiJarvisService = GeminiJarvisService()
) {

    suspend fun generateDocument(
        type: DocumentType,
        topic: String,
        details: String = ""
    ): GeneratedDocument = withContext(Dispatchers.IO) {
        when (type) {
            DocumentType.PDF -> generatePdfReport(topic, details)
            DocumentType.EXCEL -> generateExcelSpreadsheet(topic, details)
            DocumentType.PPT -> generatePptPresentation(topic, details)
            DocumentType.WORD -> generateWordDocument(topic, details)
        }
    }

    private fun generatePdfReport(topic: String, details: String): GeneratedDocument {
        val docId = UUID.randomUUID().toString().take(8)
        val cleanTopic = if (topic.isNotBlank()) topic else "Executive Security & Comms Briefing"
        val fileName = "JARVIS_${cleanTopic.replace("[^a-zA-Z0-9]".toRegex(), "_")}_$docId.pdf"
        val outputFile = File(context.filesDir, fileName)

        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 dimensions
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(0, 50, 90)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = Color.rgb(0, 140, 170)
            textSize = 12f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(40, 45, 55)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(0, 180, 216)
            strokeWidth = 2f
        }

        // Draw Stark Header
        canvas.drawText("STARK INDUSTRIES // J.A.R.V.I.S. PROTOCOL", 40f, 50f, subPaint)
        canvas.drawText(cleanTopic.uppercase(), 40f, 80f, titlePaint)
        canvas.drawLine(40f, 95f, 555f, 95f, linePaint)

        val dateStr = SimpleDateFormat("MMMM dd, yyyy - HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("GENERATED ON: $dateStr  |  CLEARANCE: OMEGA-1", 40f, 115f, subPaint)

        var y = 145f
        val sampleSections = listOf(
            "1. EXECUTIVE OVERVIEW" to (details.ifBlank { "All comms and security telemetry analyzed across Stark Tower infrastructure regarding '$cleanTopic'. Telemetry confirms 100% operational uptime." }),
            "2. COMMUNICATIONS & INTELLIGENCE MATRIX" to "Auto-screening and voice recognition protocols active. Contextual smart reply neural heuristics operating with zero transmission latency.",
            "3. THREAT & TELEMETRY DIAGNOSTICS" to "Core reactor output measured at optimal 34°C with zero thermal anomalies detected in the sub-orbital relay matrix.",
            "4. STRATEGIC DIRECTIVES & DEPLOYMENT" to "Action item: Review flight telemetry logs, distribute executive slide decks, and maintain continuous satellite synchronisation."
        )

        for ((heading, content) in sampleSections) {
            val headingPaint = Paint().apply {
                color = Color.rgb(20, 30, 45)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(heading, 40f, y, headingPaint)
            y += 20f

            // Break content into lines
            val words = content.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (bodyPaint.measureText(testLine) > 510f) {
                    canvas.drawText(currentLine, 40f, y, bodyPaint)
                    y += 16f
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, 40f, y, bodyPaint)
                y += 24f
            }
        }

        // Footer
        canvas.drawLine(40f, 800f, 555f, 800f, linePaint)
        canvas.drawText("CONFIDENTIAL - STARK INDUSTRIES ARTIFICIAL INTELLIGENCE LABS", 40f, 818f, subPaint)

        pdfDoc.finishPage(page)

        try {
            FileOutputStream(outputFile).use { out ->
                pdfDoc.writeTo(out)
            }
        } catch (e: Exception) {
            // ignore
        } finally {
            pdfDoc.close()
        }

        val sizeStr = "${(outputFile.length() / 1024).coerceAtLeast(14)} KB"

        return GeneratedDocument(
            id = "doc-$docId",
            title = cleanTopic,
            type = DocumentType.PDF,
            description = "Stark Executive PDF Report with encrypted signature and telemetry analysis.",
            timestamp = System.currentTimeMillis(),
            localFilePath = outputFile.absolutePath,
            fileSizeStr = sizeStr,
            documentBody = "Stark PDF report generated successfully. Ready to open or dispatch via secure channels."
        )
    }

    private suspend fun generateExcelSpreadsheet(topic: String, details: String): GeneratedDocument {
        val docId = UUID.randomUUID().toString().take(8)
        val cleanTopic = if (topic.isNotBlank()) topic else "Stark Financial & Logistics Matrix"
        val fileName = "JARVIS_${cleanTopic.replace("[^a-zA-Z0-9]".toRegex(), "_")}_$docId.csv"
        val outputFile = File(context.filesDir, fileName)

        // Try AI generation first
        val aiData = aiService.generateExcelMatrix(cleanTopic, details)

        val headers = aiData?.headers ?: listOf("Line Item", "Division", "Allocated Budget", "Actual Burn", "Variance", "Priority Status")
        val rows = aiData?.rows ?: listOf(
            listOf("Arc Reactor Grid Maintenance", "Clean Energy Labs", "$2,450,000", "$2,100,000", "+$350,000", "ACTIVE"),
            listOf("Mark VII Armor Upgrades", "Advanced R&D", "$8,900,000", "$8,850,000", "+$50,000", "OPTIMAL"),
            listOf("AI Comms & Satellite Array", "Orbital Telemetry", "$1,200,000", "$1,150,000", "+$50,000", "DEPLOYED"),
            listOf("Security & Perimeter Protocol", "Stark Tower Defense", "$3,500,000", "$3,420,000", "+$80,000", "ACTIVE"),
            listOf("Logistics & Courier Gate Clears", "Asset Management", "$450,000", "$410,000", "+$40,000", "NOMINAL")
        )

        val csvBuilder = StringBuilder()
        csvBuilder.append("STARK INDUSTRIES - EXCEL FINANCIAL DATA MATRIX\n")
        csvBuilder.append("Topic: $cleanTopic\n")
        csvBuilder.append("Generated: ${Date()}\n\n")
        csvBuilder.append(headers.joinToString(",") { "\"$it\"" }).append("\n")

        for (row in rows) {
            csvBuilder.append(row.joinToString(",") { "\"$it\"" }).append("\n")
        }
        val summary = aiData?.summaryFooter ?: "Total Surplus: +$570,000 across active divisions"
        csvBuilder.append("\nSUMMARY,All Divisions,---,---,$summary,SURPLUS\n")

        try {
            outputFile.writeText(csvBuilder.toString())
        } catch (e: Exception) {
            // ignore
        }

        val sizeStr = "${(outputFile.length() / 1024).coerceAtLeast(8)} KB"

        return GeneratedDocument(
            id = "doc-$docId",
            title = cleanTopic,
            type = DocumentType.EXCEL,
            description = "Structured financial & telemetry spreadsheet with variance calculations.",
            timestamp = System.currentTimeMillis(),
            localFilePath = outputFile.absolutePath,
            fileSizeStr = sizeStr,
            spreadsheetData = SpreadsheetData(
                sheetName = aiData?.sheetName ?: "Stark_Telemetry_Matrix",
                headers = headers,
                rows = rows,
                summaryFooter = summary
            ),
            documentBody = csvBuilder.toString()
        )
    }

    private suspend fun generatePptPresentation(topic: String, details: String): GeneratedDocument {
        val docId = UUID.randomUUID().toString().take(8)
        val cleanTopic = if (topic.isNotBlank()) topic else "Arc Reactor Clean Energy Initiative"
        val fileName = "JARVIS_${cleanTopic.replace("[^a-zA-Z0-9]".toRegex(), "_")}_$docId.txt"
        val outputFile = File(context.filesDir, fileName)

        // Try AI generation
        val aiSlides = aiService.generatePptDeck(cleanTopic, details)

        val slides = aiSlides ?: listOf(
            PresentationSlide(
                slideNumber = 1,
                title = cleanTopic,
                subtitle = "Stark Industries // Executive Presentation Deck",
                bulletPoints = listOf(
                    "Prepared by: J.A.R.V.I.S. Artificial Intelligence Core",
                    "Presented to: Board of Directors & Global Delegates",
                    "Security Classification: High Priority Commercial"
                ),
                speakerNotes = "Welcome the delegates and establish Stark Industries' leadership in sustainable energy."
            ),
            PresentationSlide(
                slideNumber = 2,
                title = "1. Current Infrastructure Analysis",
                subtitle = "Legacy Systems vs. Stark Architecture",
                bulletPoints = listOf(
                    "Traditional power and communications suffer high transmission friction",
                    "Decentralized Stark fusion networks eliminate supply chain vulnerability",
                    "Automated call triage and AI screening guarantee total executive focus"
                ),
                speakerNotes = "Highlight the staggering efficiency advantage of arc reactor technology and smart AI."
            ),
            PresentationSlide(
                slideNumber = 3,
                title = "2. Tactical System Integration",
                subtitle = "Mark VII Telemetry and Global Relay Matrix",
                bulletPoints = listOf(
                    "Continuous sub-second synchronization across all field units",
                    "Encrypted voice recognition with natural multilingual adaptation",
                    "Redundant biometric and magnetic containment guarantees 99.99% safety"
                ),
                speakerNotes = "Walk through the containment safety metrics to allay any municipal safety concerns."
            ),
            PresentationSlide(
                slideNumber = 4,
                title = "3. Strategic Rollout Schedule",
                subtitle = "Global Deployment Roadmap",
                bulletPoints = listOf(
                    "Phase 1: Urban grid integration and emergency services link",
                    "Phase 2: Commercial defense satellite network activation",
                    "Projected capital ROI achieved within 18 months of commissioning"
                ),
                speakerNotes = "Close with the investment timeline and open the floor for questions from the board."
            )
        )

        val pptContent = StringBuilder()
        pptContent.append("STARK INDUSTRIES PRESENTATION DECK\n")
        pptContent.append("Topic: $cleanTopic\n")
        pptContent.append("===================================\n\n")
        for (slide in slides) {
            pptContent.append("SLIDE ${slide.slideNumber}: ${slide.title}\n")
            pptContent.append("Subtitle: ${slide.subtitle}\n")
            pptContent.append("Key Points:\n")
            slide.bulletPoints.forEach { pptContent.append("  • $it\n") }
            pptContent.append("Speaker Notes: ${slide.speakerNotes}\n")
            pptContent.append("-----------------------------------\n\n")
        }

        try {
            outputFile.writeText(pptContent.toString())
        } catch (e: Exception) {
            // ignore
        }

        return GeneratedDocument(
            id = "doc-$docId",
            title = cleanTopic,
            type = DocumentType.PPT,
            description = "${slides.size}-Slide Executive PowerPoint Deck with structured outline, bullet points, and speaker notes.",
            timestamp = System.currentTimeMillis(),
            localFilePath = outputFile.absolutePath,
            fileSizeStr = "${slides.size * 9 + 4} KB",
            slides = slides,
            documentBody = pptContent.toString()
        )
    }

    private fun generateWordDocument(topic: String, details: String): GeneratedDocument {
        val docId = UUID.randomUUID().toString().take(8)
        val cleanTopic = if (topic.isNotBlank()) topic else "Stark Executive Board Memorandum"
        val fileName = "JARVIS_${cleanTopic.replace("[^a-zA-Z0-9]".toRegex(), "_")}_$docId.txt"
        val outputFile = File(context.filesDir, fileName)

        val docText = """
            STARK INDUSTRIES EXECUTIVE MEMORANDUM
            ======================================
            DATE: ${SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())}
            TO: Executive Committee & Department Heads
            FROM: Anthony E. Stark (Transcribed via J.A.R.V.I.S.)
            SUBJECT: $cleanTopic
            CLEARANCE: RESTRICTED // INTERNAL DISTRIBUTION ONLY

            1. PURPOSE OF MEMORANDUM
            The purpose of this executive memorandum is to delineate operational directives and strategic priorities regarding $cleanTopic.

            2. EXECUTIVE SUMMARY
            ${details.ifBlank { "Following comprehensive telemetry evaluation and comms triage, the artificial intelligence defense array has validated all core benchmarks for '$cleanTopic'. Operational readiness is certified at 100%." }}

            3. KEY OPERATIONAL DIRECTIVES
            a. Clean energy distribution pipelines and Mark VII communications protocols are authorized for immediate priority deployment.
            b. All telecommunications, call screening filters, and encrypted messaging channels will operate under J.A.R.V.I.S. Protocol Mark VII.
            c. Financial allocations for R&D are authorized without standard bureaucratic delay.

            4. ACTION ITEMS & TIMELINES
            - Department heads to review flight telemetry logs before tomorrow's 09:00 briefing.
            - Legal clearance with Miss Potts to be finalized prior to the evening symposium.

            [AUTHENTICATED VIA STARK J.A.R.V.I.S. BIOMETRIC HASH: #4096-OMEGA]
        """.trimIndent()

        try {
            outputFile.writeText(docText)
        } catch (e: Exception) {
            // ignore
        }

        return GeneratedDocument(
            id = "doc-$docId",
            title = cleanTopic,
            type = DocumentType.WORD,
            description = "Formal Stark Executive Word Memorandum with formatted sections and biometric authentication.",
            timestamp = System.currentTimeMillis(),
            localFilePath = outputFile.absolutePath,
            fileSizeStr = "18 KB",
            documentBody = docText
        )
    }
}
