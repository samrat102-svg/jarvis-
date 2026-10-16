package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.core.content.FileProvider
import com.example.data.model.DocumentType
import com.example.data.model.GeneratedDocument
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDim
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisGreenSuccess
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DocumentScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val documents by viewModel.generatedDocuments.collectAsState()
    val isGenerating by viewModel.isGeneratingDocument.collectAsState()

    var selectedTypeFilter by remember { mutableStateOf<DocumentType?>(null) }
    var newDocTopic by remember { mutableStateOf("") }
    var selectedCreationType by remember { mutableStateOf(DocumentType.PPT) }

    var expandedDocId by remember { mutableStateOf<String?>(documents.firstOrNull()?.id) }

    val filteredDocs = if (selectedTypeFilter == null) {
        documents
    } else {
        documents.filter { it.type == selectedTypeFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Stark Header Banner
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ArcCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("document_studio_banner"),
                colors = CardDefaults.elevatedCardColors(containerColor = JarvisSurfaceVariant),
                shape = RoundedCornerShape(16.dp)
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
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ArcCyanGlow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STARK DOCUMENT & INTELLIGENCE LABS",
                                color = ArcCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "OFFICE SUITE v4.2",
                            color = StarkGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "J.A.R.V.I.S. compiles full PowerPoint decks, official PDF reports, Excel financial matrices, and Word executive memos on voice or text directive.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    // Generation Form
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DocumentType.entries.forEach { type ->
                            val isSelected = selectedCreationType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ArcCyan else JarvisSurfaceElevated)
                                    .clickable { selectedCreationType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.iconLabel,
                                    color = if (isSelected) Color.Black else ArcCyanGlow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newDocTopic,
                            onValueChange = { newDocTopic = it },
                            placeholder = {
                                Text(
                                    text = when (selectedCreationType) {
                                        DocumentType.PPT -> "e.g., Clean Energy Grid Deployment..."
                                        DocumentType.PDF -> "e.g., Comms & Flight Security Audit..."
                                        DocumentType.EXCEL -> "e.g., Q4 Armor R&D Budget..."
                                        DocumentType.WORD -> "e.g., Board Directive: Threat Response..."
                                    },
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("doc_topic_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = BorderCyan,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.createDocument(
                                    selectedCreationType,
                                    newDocTopic.ifBlank {
                                        when (selectedCreationType) {
                                            DocumentType.PPT -> "Stark Arc Reactor Initiative"
                                            DocumentType.PDF -> "Executive Comms & Telemetry Audit"
                                            DocumentType.EXCEL -> "Stark Division Financial Matrix"
                                            DocumentType.WORD -> "Executive Operational Memorandum"
                                        }
                                    }
                                )
                                newDocTopic = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("generate_doc_button")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.PostAdd, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Generate", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Quick Preset Creation Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "QUICK INTELLIGENCE PRESETS:",
                    color = ArcCyanGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            viewModel.createDocument(DocumentType.PPT, "Mark VII Armor Telemetry & Thrust Vectors")
                        },
                        label = { Text("📽️ PPT: Mark VII Telemetry", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = JarvisSurfaceElevated, labelColor = TextPrimary),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = BorderCyan),
                        modifier = Modifier.testTag("preset_ppt_chip")
                    )

                    FilterChip(
                        selected = false,
                        onClick = {
                            viewModel.createDocument(DocumentType.PDF, "Weekly Call Triage & Spam Audit")
                        },
                        label = { Text("📑 PDF: Spam Triage Audit", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = JarvisSurfaceElevated, labelColor = TextPrimary),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = BorderCyan),
                        modifier = Modifier.testTag("preset_pdf_chip")
                    )

                    FilterChip(
                        selected = false,
                        onClick = {
                            viewModel.createDocument(DocumentType.EXCEL, "Stark Logistics & Supply Allocations")
                        },
                        label = { Text("📊 Excel: Supply Matrix", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = JarvisSurfaceElevated, labelColor = TextPrimary),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = BorderCyan),
                        modifier = Modifier.testTag("preset_excel_chip")
                    )

                    FilterChip(
                        selected = false,
                        onClick = {
                            viewModel.createDocument(DocumentType.WORD, "Symposium Keynote Address Draft")
                        },
                        label = { Text("📝 Word: Keynote Speech", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = JarvisSurfaceElevated, labelColor = TextPrimary),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = BorderCyan),
                        modifier = Modifier.testTag("preset_word_chip")
                    )
                }
            }
        }

        // Filter Strip & Count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GENERATED INTELLIGENCE ARCHIVE",
                    color = ArcCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${filteredDocs.size} COMPILED",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Type Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ArcCyan,
                        selectedLabelColor = Color.Black,
                        containerColor = JarvisSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )

                DocumentType.entries.forEach { type ->
                    val isSel = selectedTypeFilter == type
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedTypeFilter = if (isSel) null else type },
                        label = { Text(type.iconLabel, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ArcCyan,
                            selectedLabelColor = Color.Black,
                            containerColor = JarvisSurfaceElevated,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        // Document Cards List
        items(filteredDocs, key = { it.id }) { doc ->
            val isExpanded = expandedDocId == doc.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        when (doc.type) {
                            DocumentType.PPT -> StarkGold.copy(alpha = 0.6f)
                            DocumentType.PDF -> ArcCyan.copy(alpha = 0.6f)
                            DocumentType.EXCEL -> JarvisGreenSuccess.copy(alpha = 0.6f)
                            DocumentType.WORD -> ArcCyanGlow.copy(alpha = 0.6f)
                        },
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        expandedDocId = if (isExpanded) null else doc.id
                    }
                    .testTag("doc_card_${doc.id}"),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (doc.type) {
                                            DocumentType.PPT -> StarkGold.copy(alpha = 0.2f)
                                            DocumentType.PDF -> ArcCyan.copy(alpha = 0.2f)
                                            DocumentType.EXCEL -> JarvisGreenSuccess.copy(alpha = 0.2f)
                                            DocumentType.WORD -> ArcCyanGlow.copy(alpha = 0.2f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (doc.type) {
                                        DocumentType.PPT -> Icons.Default.Slideshow
                                        DocumentType.PDF -> Icons.Default.Description
                                        DocumentType.EXCEL -> Icons.Default.TableChart
                                        DocumentType.WORD -> Icons.Default.Description
                                    },
                                    contentDescription = null,
                                    tint = when (doc.type) {
                                        DocumentType.PPT -> StarkGold
                                        DocumentType.PDF -> ArcCyan
                                        DocumentType.EXCEL -> JarvisGreenSuccess
                                        DocumentType.WORD -> ArcCyanGlow
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = doc.title,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${doc.type.displayName} • ${doc.fileSizeStr}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Type Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (doc.type) {
                                        DocumentType.PPT -> StarkGold.copy(alpha = 0.2f)
                                        DocumentType.PDF -> ArcCyan.copy(alpha = 0.2f)
                                        DocumentType.EXCEL -> JarvisGreenSuccess.copy(alpha = 0.2f)
                                        DocumentType.WORD -> ArcCyanGlow.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = doc.type.extension.uppercase(),
                                color = when (doc.type) {
                                    DocumentType.PPT -> StarkGold
                                    DocumentType.PDF -> ArcCyanGlow
                                    DocumentType.EXCEL -> JarvisGreenSuccess
                                    DocumentType.WORD -> ArcCyanGlow
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = doc.description,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    // Expanded Interactive Content
                    AnimatedVisibility(visible = isExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Specialized Viewer based on type
                            when (doc.type) {
                                DocumentType.PPT -> {
                                    PptDeckViewer(slides = doc.slides)
                                }
                                DocumentType.EXCEL -> {
                                    doc.spreadsheetData?.let { ExcelSpreadsheetViewer(data = it) }
                                }
                                DocumentType.PDF -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(JarvisSurfaceElevated)
                                            .border(1.dp, ArcCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "PDF RENDER STATUS: COMPILED & VALIDATED",
                                                color = ArcCyan,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "Real PDF vector binary created via Android Graphics Canvas. Contains Stark Industries header, telemetry diagnostics, and biometric authentication.",
                                                color = TextPrimary,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                                DocumentType.WORD -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(JarvisSurfaceElevated)
                                            .border(1.dp, BorderCyan, RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = doc.documentBody,
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            // Share / Export Action Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        shareDocument(context, doc)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("share_doc_button_${doc.id}")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share / Export File", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        viewModel.voiceManager.speak("Reading summary of ${doc.title}: ${doc.description}")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("read_doc_button_${doc.id}")
                                ) {
                                    Text("Vocal Briefing", color = ArcCyanGlow, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun PptDeckViewer(slides: List<com.example.data.model.PresentationSlide>) {
    var activeSlideIndex by remember { mutableStateOf(0) }
    val currentSlide = slides.getOrNull(activeSlideIndex) ?: return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(JarvisSurfaceElevated)
            .border(1.dp, StarkGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Slide Header with Prev/Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SLIDE ${currentSlide.slideNumber} OF ${slides.size}",
                    color = StarkGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row {
                    IconButton(
                        onClick = {
                            if (activeSlideIndex > 0) activeSlideIndex--
                        },
                        enabled = activeSlideIndex > 0,
                        modifier = Modifier.size(28.dp).testTag("prev_slide_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Slide",
                            tint = if (activeSlideIndex > 0) StarkGold else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (activeSlideIndex < slides.size - 1) activeSlideIndex++
                        },
                        enabled = activeSlideIndex < slides.size - 1,
                        modifier = Modifier.size(28.dp).testTag("next_slide_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Slide",
                            tint = if (activeSlideIndex < slides.size - 1) StarkGold else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = currentSlide.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            if (currentSlide.subtitle.isNotBlank()) {
                Text(
                    text = currentSlide.subtitle,
                    color = ArcCyanGlow,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Bullet Points
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                currentSlide.bulletPoints.forEach { point ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = "• ", color = StarkGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = point, color = TextPrimary, fontSize = 11.sp, lineHeight = 15.sp)
                    }
                }
            }

            if (currentSlide.speakerNotes.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(JarvisSurface)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "SPEAKER NOTES: ${currentSlide.speakerNotes}",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun ExcelSpreadsheetViewer(data: com.example.data.model.SpreadsheetData) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(JarvisSurfaceElevated)
            .border(1.dp, JarvisGreenSuccess.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SHEET: ${data.sheetName.uppercase()}",
                color = JarvisGreenSuccess,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${data.rows.size} ROWS",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Horizontal scrolling table
        Box(modifier = Modifier.horizontalScroll(scrollState)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Header Row
                Row(
                    modifier = Modifier
                        .background(JarvisSurface)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    data.headers.forEach { header ->
                        Text(
                            text = header,
                            color = JarvisGreenSuccess,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }

                // Data Rows
                data.rows.forEach { row ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { cell ->
                            Text(
                                text = cell,
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(110.dp)
                            )
                        }
                    }
                }
            }
        }

        if (data.summaryFooter.isNotBlank()) {
            Text(
                text = "TOTAL: ${data.summaryFooter}",
                color = StarkGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun shareDocument(context: android.content.Context, doc: GeneratedDocument) {
    try {
        val file = File(doc.localFilePath)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = when (doc.type) {
                DocumentType.PDF -> "application/pdf"
                DocumentType.EXCEL -> "text/csv"
                DocumentType.PPT -> "text/plain"
                DocumentType.WORD -> "text/plain"
            }
            putExtra(Intent.EXTRA_SUBJECT, "STARK REPORT: ${doc.title}")
            putExtra(Intent.EXTRA_TEXT, doc.documentBody)
            if (file.exists()) {
                val fileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(shareIntent, "Dispatch via Stark Comms").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    } catch (e: Exception) {
        // Fallback plain share
        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "STARK REPORT: ${doc.title}")
            putExtra(Intent.EXTRA_TEXT, doc.documentBody)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(fallbackIntent, "Export Document Text").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}
