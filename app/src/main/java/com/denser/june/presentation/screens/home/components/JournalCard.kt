package com.denser.june.presentation.screens.home.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.denser.june.core.R
import com.denser.june.core.domain.model.Journal
import com.denser.june.core.utils.toDayOfMonth
import com.denser.june.core.utils.toFullDate
import com.denser.june.core.utils.toShortMonth
import com.denser.june.presentation.navigation.AppNavigator
import com.denser.june.presentation.navigation.Route
import com.denser.june.presentation.components.JuneSongPlayerCard
import com.denser.june.presentation.screens.editor.components.JournalMosaicCard
import com.denser.june.presentation.screens.editor.components.MediaOperations
import com.denser.june.presentation.screens.home.journals.JournalsVM
import com.denser.june.presentation.utils.rememberDynamicThemeColors
import com.denser.june.presentation.utils.rememberSongPlayerState
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

enum class AttachmentType {
    NONE, AUDIO, IMAGES, LOCATION
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JournalCard(
    journal: Journal,
    modifier: Modifier,
    actionIcon: Int? = null,
    onActionClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val viewModel: JournalsVM = koinViewModel()
    val navigator = koinInject<AppNavigator>()
    val context = LocalContext.current
    val sharedPrefs = remember(context) {
        context.getSharedPreferences("journal_locks", android.content.Context.MODE_PRIVATE)
    }
    val isLocked = remember(journal.id) {
        sharedPrefs.getBoolean(journal.id, false)
    }

    var expandedType by remember { mutableStateOf(AttachmentType.NONE) }
    var isDrawerPlaying by remember { mutableStateOf(false) }
    val isAudioPlaying = isDrawerPlaying && expandedType == AttachmentType.AUDIO

    val infiniteTransition = rememberInfiniteTransition(label = "playback_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val borderBrush = if (isAudioPlaying) {
        Brush.sweepGradient(
            listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                MaterialTheme.colorScheme.tertiary.copy(alpha = glowAlpha),
                MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha)
            )
        )
    } else {
        null
    }


    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick = { navigator.navigateTo(Route.Editor(journal.id), isSingleTop = true) },
                onLongClick = onLongClick
            )
            .then(
                if (isAudioPlaying) {
                    Modifier.border(2.dp, borderBrush!!, RoundedCornerShape(24.dp))
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(96.dp, 60.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                ) {
                    if (isLocked) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.lock_24px),
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.book_5_24px),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = journal.dateTime.toFullDate(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        if (!isLocked) {
                            Spacer(modifier = Modifier.width(8.dp))
                            AttachmentBadges(
                                journal = journal,
                                expandedType = expandedType,
                                onToggle = { expandedType = it }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isLocked) journal.title.ifBlank { "Locked Entry" }
                               else journal.title.ifBlank { journal.content.ifBlank { "Add title" } },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                FilledIconButton(
                    onClick = { onActionClick?.invoke() ?: viewModel.toggleBookmark(journal.id) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = IconButtonDefaults.smallRoundShape
                ) {
                    Icon(
                        painter = painterResource(
                            actionIcon ?: if (journal.isBookmarked) R.drawable.bookmark_added_24px_fill 
                            else R.drawable.bookmark_24px
                        ),
                        contentDescription = if (actionIcon != null) "Action" else "Toggle Bookmark",
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            AnimatedVisibility(
                visible = !isLocked && expandedType != AttachmentType.NONE,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(bottom = 12.dp, start = 12.dp, end = 12.dp)) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    when (expandedType) {
                        AttachmentType.AUDIO -> AudioDrawer(
                            journal = journal,
                            onPlayingStateChanged = { isDrawerPlaying = it },
                            glowAlpha = if (isAudioPlaying) glowAlpha else 0f
                        )
                        AttachmentType.IMAGES -> ImagesDrawer(journal = journal)
                        AttachmentType.LOCATION -> LocationDrawer(journal = journal)
                        else -> {}
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecentJournalCard(
    journal: Journal,
    modifier: Modifier,
    actionIcon: Int? = null,
    onActionClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val sharedPrefs = remember(context) {
        context.getSharedPreferences("journal_locks", android.content.Context.MODE_PRIVATE)
    }
    val isLocked = remember(journal.id) {
        sharedPrefs.getBoolean(journal.id, false)
    }

    if (journal.images.isEmpty() || isLocked) {
        JournalCard(
            journal = journal,
            modifier = modifier,
            actionIcon = actionIcon,
            onActionClick = onActionClick,
            onLongClick = onLongClick
        )
    } else {
        val viewModel: JournalsVM = koinViewModel()
        val navigator = koinInject<AppNavigator>()

        var expandedType by remember { mutableStateOf(AttachmentType.NONE) }
        var isDrawerPlaying by remember { mutableStateOf(false) }
        val isAudioPlaying = isDrawerPlaying && expandedType == AttachmentType.AUDIO

        val infiniteTransition = rememberInfiniteTransition(label = "playback_glow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        )

        val borderBrush = if (isAudioPlaying) {
            Brush.sweepGradient(
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = glowAlpha),
                    MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha)
                )
            )
        } else {
            null
        }


        val mediaOperations = MediaOperations(onMediaClick = null)

        Card(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize()
                .clip(RoundedCornerShape(24.dp))
                .combinedClickable(
                    onClick = {
                        navigator.navigateTo(
                            Route.Editor(journal.id),
                            isSingleTop = true
                        )
                    },
                    onLongClick = onLongClick
                )
                .then(
                    if (isAudioPlaying) {
                        Modifier.border(2.dp, borderBrush!!, RoundedCornerShape(24.dp))
                    } else Modifier
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = journal.dateTime.toDayOfMonth(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = journal.dateTime.toShortMonth(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        AttachmentBadges(
                            journal = journal,
                            expandedType = expandedType,
                            onToggle = { expandedType = it }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = journal.title.ifBlank { journal.content.ifBlank { "Untitled" } },
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    FilledIconButton(
                        onClick = { onActionClick?.invoke() ?: viewModel.toggleBookmark(journal.id) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = IconButtonDefaults.smallRoundShape
                    ) {
                        Icon(
                            painter = painterResource(
                                actionIcon ?: if (journal.isBookmarked) R.drawable.bookmark_added_24px_fill 
                                else R.drawable.bookmark_24px
                            ),
                            contentDescription = if (actionIcon != null) "Action" else "Toggle Bookmark",
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))


                AnimatedVisibility(
                    visible = expandedType != AttachmentType.NONE,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        when (expandedType) {
                            AttachmentType.AUDIO -> AudioDrawer(
                                journal = journal,
                                onPlayingStateChanged = { isDrawerPlaying = it },
                                glowAlpha = if (isAudioPlaying) glowAlpha else 0f
                            )
                            AttachmentType.IMAGES -> ImagesDrawer(journal = journal)
                            AttachmentType.LOCATION -> LocationDrawer(journal = journal)
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentBadges(
    journal: Journal,
    expandedType: AttachmentType,
    onToggle: (AttachmentType) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (journal.songDetails != null) {
            AttachmentBadge(
                iconRes = R.drawable.music_note_24px,
                isSelected = expandedType == AttachmentType.AUDIO,
                onClick = {
                    onToggle(if (expandedType == AttachmentType.AUDIO) AttachmentType.NONE else AttachmentType.AUDIO)
                }
            )
        }
        if (journal.location != null) {
            AttachmentBadge(
                iconRes = R.drawable.location_on_24px,
                isSelected = expandedType == AttachmentType.LOCATION,
                onClick = {
                    onToggle(if (expandedType == AttachmentType.LOCATION) AttachmentType.NONE else AttachmentType.LOCATION)
                }
            )
        }
        if (journal.images.isNotEmpty()) {
            AttachmentBadge(
                iconRes = R.drawable.photo_24px,
                isSelected = expandedType == AttachmentType.IMAGES,
                onClick = {
                    onToggle(if (expandedType == AttachmentType.IMAGES) AttachmentType.NONE else AttachmentType.IMAGES)
                }
            )
        }
    }
}

@Composable
private fun AttachmentBadge(
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier
            .size(14.dp)
            .clickable(onClick = onClick),
        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
private fun AudioDrawer(
    journal: Journal,
    onPlayingStateChanged: (Boolean) -> Unit,
    glowAlpha: Float
) {
    val song = journal.songDetails ?: return
    val playerState = rememberSongPlayerState(previewUrl = song.previewUrl)

    LaunchedEffect(playerState.isPlaying) {
        onPlayingStateChanged(playerState.isPlaying)
    }

    JuneSongPlayerCard(
        details = song,
        isPlaying = playerState.isPlaying,
        isLoading = playerState.isLoading,
        sliderValue = playerState.sliderValue,
        isRepeatEnabled = playerState.isRepeatEnabled,
        onPlayPause = playerState.onPlayPause,
        onSeek = playerState.onSeek,
        onSeekFinished = playerState.onSeekFinished,
        onToggleRepeat = playerState.onToggleRepeat,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Adjusted height for drawer
            .then(
                if (glowAlpha > 0) {
                    Modifier.border(
                        1.5.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                        RoundedCornerShape(32.dp)
                    )
                } else Modifier
            )
    )
}

@Composable
private fun ImagesDrawer(journal: Journal) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        journal.images.forEach { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .height(60.dp)
                    .width(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun LocationDrawer(journal: Journal) {
    val location = journal.location ?: return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.location_on_24px),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = location.name ?: location.address ?: "Unknown Location",
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}