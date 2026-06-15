package com.denser.june.presentation.screens.editor.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.june.core.R
import com.denser.june.core.utils.AudioRecorder
import java.io.File
import java.util.Locale

@Composable
fun VoiceRecorderDialog(
    onDismiss: () -> Unit,
    onRecordingSaved: (String) -> Unit
) {
    val context = LocalContext.current
    val recorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    var seconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                kotlinx.coroutines.delay(1000)
                seconds++
            }
        } else {
            seconds = 0
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (isRecording) {
                recorder.stop()
            }
            onDismiss()
        },
        confirmButton = {
            if (!isRecording && recordingFile != null) {
                Button(onClick = {
                    onRecordingSaved(recordingFile!!.absolutePath)
                    onDismiss()
                }) {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (isRecording) {
                    recorder.stop()
                }
                onDismiss()
            }) {
                Text("Cancel")
            }
        },
        title = {
            Text("Voice Recording", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(seconds),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(contentAlignment = Alignment.Center) {
                    if (isRecording) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(64.dp * scale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            if (isRecording) {
                                recorder.stop()
                                isRecording = false
                            } else {
                                val file = File(context.cacheDir, "recording_${System.currentTimeMillis()}.mp4")
                                recordingFile = file
                                recorder.start(file)
                                isRecording = true
                            }
                        },
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            painter = painterResource(if (isRecording) R.drawable.pause_24px else R.drawable.music_note_24px),
                            contentDescription = if (isRecording) "Stop" else "Record",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isRecording) "Recording..." else if (recordingFile != null) "Recording saved" else "Press to start",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}