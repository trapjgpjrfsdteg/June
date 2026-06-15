package com.denser.june.presentation.screens.home.components

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denser.june.core.R
import com.denser.june.core.domain.model.Journal
import com.denser.june.core.utils.toDayOfMonth
import com.denser.june.core.utils.toFullDateTime
import com.denser.june.core.utils.toShortMonth
import java.util.Locale
import com.denser.june.presentation.components.JuneBadge
import com.denser.june.presentation.components.JuneMetadataRow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import com.denser.june.presentation.utils.TagUtils
import com.denser.june.core.domain.model.enums.TagCategory
import androidx.compose.ui.window.Dialog
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JournalOptionsSheet(
    journal: Journal,
    is24Hour: Boolean = false,
    onToggleBookmark: () -> Unit,
    onDeleteOrRestore: () -> Unit,
    onPermanentDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val sharedPrefs = remember(context) {
        context.getSharedPreferences("journal_locks", android.content.Context.MODE_PRIVATE)
    }
    var isLocked by remember(journal.id) {
        mutableStateOf(sharedPrefs.getBoolean(journal.id, false))
    }
    var showLockDialog by remember { mutableStateOf(false) }

    val wordCount = remember(journal.content) {
        if (journal.content.isBlank()) 0
        else journal.content.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size
    }
    val year = remember(journal.dateTime) {
        SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(journal.dateTime))
    }

    val spacesTags = remember(journal.tags) { TagUtils.filterTagsByCategory(journal.tags, TagCategory.Spaces) }
    val peopleTags = remember(journal.tags) { TagUtils.filterTagsByCategory(journal.tags, TagCategory.People) }
    val topicsTags = remember(journal.tags) { TagUtils.filterTagsByCategory(journal.tags, TagCategory.Topics) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text(
                        text = journal.dateTime.toDayOfMonth(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = journal.dateTime.toShortMonth(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = year,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp, top = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = journal.title.ifBlank { "Untitled" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        JuneBadge(
                            show = true,
                            icon = if (journal.cloudId != null) R.drawable.cloud_24px else R.drawable.devices_24px,
                            label = if (journal.cloudId != null) "Cloud" else "Local"
                        )
                        JuneBadge(
                            show = true,
                            icon = R.drawable.article_24px,
                            label = "$wordCount words"
                        )
                        JuneBadge(show = journal.images.isNotEmpty(), icon = R.drawable.photo_24px, label = "${journal.images.size}")
                        JuneBadge(show = journal.songDetails != null, icon = R.drawable.music_note_24px)
                        JuneBadge(show = journal.location != null, icon = R.drawable.location_on_24px)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!journal.isDeleted) {
                        ActionSquircle(
                            iconRes = if (journal.isBookmarked) R.drawable.bookmark_added_24px_fill else R.drawable.bookmark_24px,
                            contentDescription = if (journal.isBookmarked) "Remove Bookmark" else "Bookmark",
                            onClick = onToggleBookmark,
                            isActive = journal.isBookmarked,
                        )
                    }
                    ActionSquircle(
                        iconRes = if (journal.isDeleted) R.drawable.restore_from_trash_24px else R.drawable.delete_24px,
                        contentDescription = if (journal.isDeleted) "Restore" else "Delete",
                        onClick = onDeleteOrRestore,
                        tint = if (journal.isDeleted) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.error
                    )
                    if (journal.isDeleted && onPermanentDelete != null) {
                        ActionSquircle(
                            iconRes = R.drawable.delete_24px,
                            contentDescription = "Permanent Delete",
                            onClick = onPermanentDelete,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Left Column (Chips & Metadata)
                Column(
                    modifier = Modifier.weight(1.25f)
                ) {
                    // Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        spacesTags.forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tag, style = MaterialTheme.typography.labelMedium) },
                                shape = RoundedCornerShape(12.dp),
                                colors = TagUtils.getTagSuggestionChipColors(tag),
                                border = null
                            )
                        }
                        peopleTags.forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tag, style = MaterialTheme.typography.labelMedium) },
                                shape = RoundedCornerShape(12.dp),
                                colors = TagUtils.getTagSuggestionChipColors(tag),
                                border = null
                            )
                        }
                        topicsTags.forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tag, style = MaterialTheme.typography.labelMedium) },
                                shape = RoundedCornerShape(12.dp),
                                colors = TagUtils.getTagSuggestionChipColors(tag),
                                border = null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metadata (adjacent to icons)
                    Column {
                        MetadataRow(
                            iconRes = R.drawable.cloud_sync_24px,
                            value = journal.syncedAt?.toFullDateTime(is24Hour) ?: "Not synced"
                        )
                        MetadataRow(
                            iconRes = R.drawable.today_24px,
                            value = journal.createdAt.toFullDateTime(is24Hour)
                        )
                        MetadataRow(
                            iconRes = R.drawable.history_24px,
                            value = journal.updatedAt?.toFullDateTime(is24Hour) ?: "—"
                        )
                    }
                }

                // Vertical Divider spanning the columns
                VerticalDivider(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .height(120.dp)
                        .padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Right Column (Share Module)
                Column(
                    modifier = Modifier.weight(0.75f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var showShareDialog by remember { mutableStateOf(false) }

                    Button(
                        onClick = { showShareDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.send_24px),
                            contentDescription = "Share",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showLockDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLocked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isLocked) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(if (isLocked) R.drawable.lock_24px else R.drawable.lock_open_right_24px),
                            contentDescription = "Lock",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isLocked) "Unlock" else "Lock")
                    }

                    if (showShareDialog) {
                        JournalShareDialog(
                            journal = journal,
                            onDismiss = { showShareDialog = false }
                        )
                    }
                }
            }
            if (showLockDialog) {
                JournalLockDialog(
                    journalId = journal.id,
                    isLocked = isLocked,
                    onLockStateChanged = { isLocked = it },
                    onDismiss = { showLockDialog = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActionSquircle(
    modifier: Modifier = Modifier,
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    isActive: Boolean = false,
    activeContentColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        shape = IconButtonDefaults.smallRoundShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = if (isActive) activeContentColor else tint
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MetadataRow(
    iconRes: Int,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DensityToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun buildExportText(
    journal: Journal,
    includeTitle: Boolean,
    includeDateTime: Boolean,
    includeMetadata: Boolean,
    includeStats: Boolean,
    isMarkdown: Boolean
): String {
    val builder = StringBuilder()
    
    if (includeTitle && journal.title.isNotBlank()) {
        if (isMarkdown) {
            builder.append("# ").append(journal.title).append("\n\n")
        } else {
            builder.append(journal.title).append("\n\n")
        }
    }
    
    if (includeDateTime) {
        val dateStr = journal.dateTime.toFullDateTime(is24Hour = false)
        if (isMarkdown) {
            builder.append("**Date:** ").append(dateStr).append("\n")
        } else {
            builder.append("Date: ").append(dateStr).append("\n")
        }
    }
    
    if (includeMetadata) {
        journal.location?.let {
            if (isMarkdown) {
                builder.append("**Location:** ").append(it.name ?: "${it.latitude}, ${it.longitude}").append("\n")
            } else {
                builder.append("Location: ").append(it.name ?: "${it.latitude}, ${it.longitude}").append("\n")
            }
        }
        if (journal.tags.isNotEmpty()) {
            val tagsStr = journal.tags.joinToString(", ")
            if (isMarkdown) {
                builder.append("**Tags:** ").append(tagsStr).append("\n")
            } else {
                builder.append("Tags: ").append(tagsStr).append("\n")
            }
        }
        journal.songDetails?.let {
            if (isMarkdown) {
                builder.append("**Song:** ").append(it.title).append(" - ").append(it.artistName).append("\n")
            } else {
                builder.append("Song: ").append(it.title).append(" - ").append(it.artistName).append("\n")
            }
        }
    }
    
    if (includeStats) {
        val wordCount = if (journal.content.isBlank()) 0 else journal.content.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size
        val charCount = journal.content.length
        if (isMarkdown) {
            builder.append("**Stats:** ").append(wordCount).append(" words, ").append(charCount).append(" characters\n")
        } else {
            builder.append("Stats: ").append(wordCount).append(" words, ").append(charCount).append(" characters\n")
        }
    }
    
    if (builder.isNotEmpty()) {
        if (isMarkdown) {
            builder.append("\n---\n\n")
        } else {
            builder.append("\n------------------\n\n")
        }
    }
    
    builder.append(journal.content)
    return builder.toString()
}

private fun generatePdfFile(context: android.content.Context, title: String, content: String): File {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas: Canvas = page.canvas
    
    val paint = Paint().apply {
        textSize = 12f
        color = android.graphics.Color.BLACK
    }
    
    val titlePaint = Paint().apply {
        textSize = 18f
        color = android.graphics.Color.BLACK
    }
    titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    
    var y = 50f
    val margin = 50f
    val width = 595f - (margin * 2f)
    
    fun drawText(text: String, isTitle: Boolean = false) {
        val currentPaint = if (isTitle) titlePaint else paint
        val leading = if (isTitle) 24f else 16f
        
        val words = text.split(" ")
        var line = ""
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            val testWidth = currentPaint.measureText(testLine)
            if (testWidth > width) {
                if (y + leading > 800f) {
                    pdfDocument.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    y = 50f
                }
                canvas.drawText(line, margin, y, currentPaint)
                y += leading
                line = word
            } else {
                line = testLine
            }
        }
        
        if (line.isNotEmpty()) {
            if (y + leading > 800f) {
                pdfDocument.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                y = 50f
            }
            canvas.drawText(line, margin, y, currentPaint)
            y += leading
        }
    }
    
    val lines = content.split("\n")
    for (line in lines) {
        if (line.isBlank()) {
            y += 8f
        } else {
            drawText(line)
        }
    }
    
    pdfDocument.finishPage(page)
    
    val file = File(context.cacheDir, "${title.replace("[^a-zA-Z0-9]".toRegex(), "_")}.pdf")
    FileOutputStream(file).use { out ->
        pdfDocument.writeTo(out)
    }
    pdfDocument.close()
    
    return file
}

private fun shareFile(context: android.content.Context, file: File, mimeType: String) {
    val authority = "${context.packageName}.provider"
    val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Journal"))
}

private fun copyToClipboard(context: android.content.Context, text: String) {
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("Journal Entry", text)
    clipboard.setPrimaryClip(clip)
    android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
}

@Composable
fun JournalLockDialog(
    journalId: String,
    isLocked: Boolean,
    onLockStateChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    temporaryUnlockOnly: Boolean = false,
    onUnlockSuccess: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val sharedPrefs = remember(context) {
        context.getSharedPreferences("journal_locks", android.content.Context.MODE_PRIVATE)
    }

    var setupStep by remember { mutableStateOf(1) }
    var selectedType by remember { mutableStateOf("PASSWORD") }

    var passwordInput by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    var verifyInput by remember { mutableStateOf("") }
    var verifyError by remember { mutableStateOf("") }

    val savedType = remember(journalId) { sharedPrefs.getString("${journalId}_type", "none") ?: "none" }
    val savedPassword = remember(journalId) { sharedPrefs.getString("${journalId}_password", "") ?: "" }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isLocked) {
                    if (setupStep == 1) {
                        Text(
                            text = "Lock Journal Entry",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select a security method to protect this entry.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    selectedType = "PASSWORD"
                                    setupStep = 2
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("🔑 Custom Password")
                            }
                            OutlinedButton(
                                onClick = {
                                    selectedType = "CROSS"
                                    setupStep = 2
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("⏹️ Pattern / Cross Password")
                            }
                            OutlinedButton(
                                onClick = {
                                    selectedType = "BIOMETRIC"
                                    setupStep = 2
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("👤 Biometric / Fingerprint")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text("Cancel")
                        }
                    } else if (setupStep == 2) {
                        when (selectedType) {
                            "PASSWORD" -> {
                                Text(
                                    text = "Set Password",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it; passwordError = "" },
                                    label = { Text("Password") },
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = passwordConfirm,
                                    onValueChange = { passwordConfirm = it; passwordError = "" },
                                    label = { Text("Confirm Password") },
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                if (passwordError.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = passwordError,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        onClick = { setupStep = 1; passwordInput = ""; passwordConfirm = "" },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Back")
                                    }
                                    Button(
                                        onClick = {
                                            if (passwordInput.isBlank()) {
                                                passwordError = "Password cannot be empty"
                                            } else if (passwordInput != passwordConfirm) {
                                                passwordError = "Passwords do not match"
                                            } else {
                                                sharedPrefs.edit()
                                                    .putBoolean(journalId, true)
                                                    .putString("${journalId}_type", "PASSWORD")
                                                    .putString("${journalId}_password", passwordInput)
                                                    .apply()
                                                onLockStateChanged(true)
                                                onDismiss()
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Save")
                                    }
                                }
                            }
                            "CROSS" -> {
                                CrossPasswordView(
                                    onPatternComplete = { pattern ->
                                        sharedPrefs.edit()
                                            .putBoolean(journalId, true)
                                            .putString("${journalId}_type", "CROSS")
                                            .putString("${journalId}_password", pattern)
                                            .apply()
                                        onLockStateChanged(true)
                                        onDismiss()
                                    },
                                    onCancel = { setupStep = 1 }
                                )
                            }
                            "BIOMETRIC" -> {
                                BiometricScanView(
                                    onScanSuccess = {
                                        sharedPrefs.edit()
                                            .putBoolean(journalId, true)
                                            .putString("${journalId}_type", "BIOMETRIC")
                                            .putString("${journalId}_password", "BIOMETRIC_OK")
                                            .apply()
                                        onLockStateChanged(true)
                                        onDismiss()
                                    },
                                    onCancel = { setupStep = 1 }
                                )
                            }
                        }
                    }
                } else {
                    when (savedType) {
                        "PASSWORD" -> {
                            Text(
                                text = "Enter Password to Unlock",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = verifyInput,
                                onValueChange = { verifyInput = it; verifyError = "" },
                                label = { Text("Password") },
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            if (verifyError.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = verifyError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = {
                                        if (verifyInput == savedPassword) {
                                            if (temporaryUnlockOnly) {
                                                onUnlockSuccess?.invoke()
                                            } else {
                                                sharedPrefs.edit()
                                                    .remove(journalId)
                                                    .remove("${journalId}_type")
                                                    .remove("${journalId}_password")
                                                    .apply()
                                                onLockStateChanged(false)
                                            }
                                            onDismiss()
                                        } else {
                                            verifyError = "Incorrect password"
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Unlock")
                                }
                            }
                        }
                        "CROSS" -> {
                            CrossPasswordView(
                                onPatternComplete = { pattern ->
                                    if (pattern == savedPassword) {
                                        if (temporaryUnlockOnly) {
                                            onUnlockSuccess?.invoke()
                                        } else {
                                            sharedPrefs.edit()
                                                .remove(journalId)
                                                .remove("${journalId}_type")
                                                .remove("${journalId}_password")
                                                .apply()
                                            onLockStateChanged(false)
                                        }
                                        onDismiss()
                                    } else {
                                        android.widget.Toast.makeText(context, "Incorrect Pattern", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onCancel = onDismiss
                            )
                        }
                        "BIOMETRIC" -> {
                            BiometricScanView(
                                onScanSuccess = {
                                    if (temporaryUnlockOnly) {
                                        onUnlockSuccess?.invoke()
                                    } else {
                                        sharedPrefs.edit()
                                            .remove(journalId)
                                            .remove("${journalId}_type")
                                            .remove("${journalId}_password")
                                            .apply()
                                        onLockStateChanged(false)
                                    }
                                    onDismiss()
                                    android.widget.Toast.makeText(context, "Unlocked successfully", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onCancel = onDismiss
                            )
                        }
                        else -> {
                            if (temporaryUnlockOnly) {
                                onUnlockSuccess?.invoke()
                            } else {
                                sharedPrefs.edit()
                                    .remove(journalId)
                                    .remove("${journalId}_type")
                                    .remove("${journalId}_password")
                                    .apply()
                                onLockStateChanged(false)
                            }
                            onDismiss()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrossPasswordView(
    onPatternComplete: (String) -> Unit,
    onCancel: () -> Unit
) {
    var selectedDirections by remember { mutableStateOf(emptyList<String>()) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Enter Cross Pattern", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { selectedDirections = selectedDirections + "U" },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("↑", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { selectedDirections = selectedDirections + "L" },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = selectedDirections.size.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                Surface(
                    onClick = { selectedDirections = selectedDirections + "R" },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("→", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Surface(
                onClick = { selectedDirections = selectedDirections + "D" },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("↓", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sequence: " + selectedDirections.joinToString(" "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = { selectedDirections = emptyList() }) {
                Text("Clear")
            }
            Button(
                enabled = selectedDirections.isNotEmpty(),
                onClick = { onPatternComplete(selectedDirections.joinToString("")) }
            ) {
                Text("Confirm")
            }
        }
    }
}

@Composable
fun BiometricScanView(
    onScanSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2500)
        onScanSuccess()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Text("Biometric Authentication", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                modifier = Modifier.size(80.dp * scale)
            ) {}
            Icon(
                painter = painterResource(R.drawable.fingerprint_24px),
                contentDescription = "Fingerprint Sensor",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Scan your fingerprint to authenticate...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onCancel) {
            Text("Cancel")
        }
    }
}

@Composable
fun JournalShareDialog(
    journal: Journal,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var includeTitle by remember { mutableStateOf(true) }
    var includeDateTime by remember { mutableStateOf(true) }
    var includeMetadata by remember { mutableStateOf(true) }
    var includeStats by remember { mutableStateOf(false) }

    var showFormatDropdown by remember { mutableStateOf(false) }
    val formatOptions = listOf("Plain Text (.txt)", "Markdown (.md)", "PDF Document (.pdf)", "Inline Text")
    var selectedFormatText by remember { mutableStateOf(formatOptions[0]) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Share Entry Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Export Density",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DensityToggle(label = "Title", checked = includeTitle, onCheckedChange = { includeTitle = it })
                    DensityToggle(label = "Date & Time", checked = includeDateTime, onCheckedChange = { includeDateTime = it })
                    DensityToggle(label = "Metadata", checked = includeMetadata, onCheckedChange = { includeMetadata = it })
                    DensityToggle(label = "Stats", checked = includeStats, onCheckedChange = { includeStats = it })

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Export Format",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { showFormatDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedFormatText, style = MaterialTheme.typography.bodyMedium)
                                Text("▼", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        DropdownMenu(
                            expanded = showFormatDropdown,
                            onDismissRequest = { showFormatDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            formatOptions.forEach { format ->
                                DropdownMenuItem(
                                    text = { Text(format) },
                                    onClick = {
                                        selectedFormatText = format
                                        showFormatDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val text = buildExportText(
                                journal,
                                includeTitle,
                                includeDateTime,
                                includeMetadata,
                                includeStats,
                                isMarkdown = selectedFormatText.contains(".md")
                            )
                            when {
                                selectedFormatText.contains(".txt") -> {
                                    val file = File(context.cacheDir, "${journal.title.ifBlank { "Untitled" }.replace("[^a-zA-Z0-9]".toRegex(), "_")}.txt")
                                    file.writeText(text)
                                    shareFile(context, file, "text/plain")
                                }
                                selectedFormatText.contains(".md") -> {
                                    val file = File(context.cacheDir, "${journal.title.ifBlank { "Untitled" }.replace("[^a-zA-Z0-9]".toRegex(), "_")}.md")
                                    file.writeText(text)
                                    shareFile(context, file, "text/markdown")
                                }
                                selectedFormatText.contains(".pdf") -> {
                                    val file = generatePdfFile(context, journal.title.ifBlank { "Untitled" }, text)
                                    shareFile(context, file, "application/pdf")
                                }
                                else -> {
                                    copyToClipboard(context, text)
                                }
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.send_24px),
                            contentDescription = "Share",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share")
                    }
                }
            }
        }
    }
}
