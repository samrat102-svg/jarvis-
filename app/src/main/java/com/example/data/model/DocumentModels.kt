package com.example.data.model

import java.io.File

enum class DocumentType(val extension: String, val displayName: String, val iconLabel: String) {
    PPT("pptx", "PowerPoint Deck", "📽️ PPT"),
    PDF("pdf", "PDF Document", "📑 PDF"),
    EXCEL("xlsx", "Excel Spreadsheet", "📊 EXCEL"),
    WORD("docx", "Word Document", "📝 WORD")
}

data class PresentationSlide(
    val slideNumber: Int,
    val title: String,
    val subtitle: String = "",
    val bulletPoints: List<String> = emptyList(),
    val speakerNotes: String = ""
)

data class SpreadsheetData(
    val sheetName: String,
    val headers: List<String>,
    val rows: List<List<String>>,
    val summaryFooter: String = ""
)

data class GeneratedDocument(
    val id: String,
    val title: String,
    val type: DocumentType,
    val description: String,
    val timestamp: Long,
    val localFilePath: String = "",
    val fileSizeStr: String = "24 KB",
    val slides: List<PresentationSlide> = emptyList(),
    val spreadsheetData: SpreadsheetData? = null,
    val documentBody: String = ""
)
