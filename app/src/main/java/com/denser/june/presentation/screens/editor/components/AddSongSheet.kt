package com.denser.june.presentation.screens.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denser.june.core.domain.model.SongDetails
import com.denser.june.core.domain.preferences.PrivacyPreferences
import com.denser.june.core.domain.repository.SongRepository
import com.denser.june.presentation.components.InternetRestrictedBanner
import com.denser.june.presentation.components.RestrictedAsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

import com.denser.june.core.R
import com.denser.june.presentation.components.JuneFloatingAction
import com.denser.june.presentation.components.JuneFloatingActionBar
import com.denser.june.presentation.utils.UiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongSheet(
    songDetails: SongDetails? = null,
    isFetching: Boolean = false,
    onFetchDetails: (String) -> Unit,
    onRemoveSong: () -> Unit,
    onDismiss: () -> Unit
) {
    val privacyPreferences = koinInject<PrivacyPreferences>()
    val songRepo = koinInject<SongRepository>()
    val isInternetAllowed by privacyPreferences.getIsInternetAllowedFlow()
        .collectAsStateWithLifecycle(initialValue = true)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var songLink by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SongDetails>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current

    LaunchedEffect(songLink) {
        val trimmed = songLink.trim()
        if (trimmed.isNotBlank() && !trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            searchQuery = trimmed
        } else {
            searchQuery = ""
            searchResults = emptyList()
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            searchResults = emptyList()
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(500)
        isSearching = true
        val result = songRepo.searchSongs(searchQuery)
        isSearching = false
        if (result.isSuccess) {
            searchResults = result.getOrDefault(emptyList())
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = null
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = if(isInternetAllowed) 600.dp else 660.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            topBar = { AddSongSheetHeader() },
            floatingActionButton = {
                JuneFloatingActionBar(
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    if (songDetails != null) {
                        FilledIconButton(
                            onClick = {
                                onRemoveSong()
                                songLink = ""
                            },
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                painterResource(R.drawable.delete_24px),
                                contentDescription = "Delete"
                            )
                        }
                    }
                    JuneFloatingAction(
                        onClick = {
                            if (isInternetAllowed) {
                                scope.launch {
                                    val clipEntry = clipboard.getClipEntry()
                                    val text = clipEntry?.clipData?.getItemAt(0)?.text?.toString() ?: ""
                                    songLink = text
                                    onFetchDetails(text)
                                }
                            }
                        },
                        label = "Paste",
                        icon = {
                            Icon(
                                painterResource(R.drawable.content_paste_go_24px),
                                contentDescription = null
                            )
                        },
                        containerColor = if (isInternetAllowed) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isInternetAllowed) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                        enabled = isInternetAllowed
                    )
                    JuneFloatingAction(
                        onClick = onDismiss,
                        label = "Done",
                        icon = {
                            Icon(
                                painterResource(R.drawable.check_24px),
                                contentDescription = null
                            )
                        }
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isInternetAllowed) {
                    Spacer(modifier = Modifier.height(16.dp))
                    InternetRestrictedBanner(
                        description = "Enable internet access to fetch song details."
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                SongInputCard(
                    songLink = songLink,
                    onLinkChange = { songLink = it },
                    isFetching = isFetching,
                    onFetchClick = { onFetchDetails(songLink) },
                    enabled = isInternetAllowed
                )
                
                if (isSearching) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularWavyProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (searchResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Search Results",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                            searchResults.forEach { song ->
                                SearchResultItem(
                                    song = song,
                                    onClick = {
                                        song.links.appleMusic?.let { url ->
                                            songLink = ""
                                            onFetchDetails(url)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                SongPreviewCard(
                    songDetails = songDetails,
                    isFetching = isFetching,
                    onRemoveSong = onRemoveSong
                )
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SongInputCard(
    songLink: String,
    onLinkChange: (String) -> Unit,
    isFetching: Boolean,
    onFetchClick: () -> Unit,
    enabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Song Search",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.spotify), null, Modifier.size(12.dp))
                    Spacer(Modifier.width(6.dp))
                    Icon(painterResource(R.drawable.applemusic), null, Modifier.size(12.dp))
                    Spacer(Modifier.width(6.dp))
                    Icon(painterResource(R.drawable.youtubemusic), null, Modifier.size(12.dp))
                    Spacer(Modifier.width(6.dp))
                    Icon(painterResource(R.drawable.soundcloud), null, Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Icon(painterResource(R.drawable.amazonmusic), null, Modifier.size(12.dp))
                    Spacer(Modifier.width(6.dp))
                    Icon(painterResource(R.drawable.deezer), null, Modifier.size(12.dp))
                    Spacer(Modifier.width(6.dp))
                    Icon(painterResource(R.drawable.tidal), null, Modifier.size(12.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            TextField(
                value = songLink,
                onValueChange = onLinkChange,
                placeholder = { Text("Search song or paste link...") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isFetching && enabled,
                leadingIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val clipEntry = clipboard.getClipEntry()
                                val text = clipEntry?.clipData?.getItemAt(0)?.text?.toString() ?: ""
                                if (text.isNotBlank()) {
                                    onLinkChange(text)
                                    if (text.startsWith("http://") || text.startsWith("https://")) {
                                        onFetchClick()
                                    }
                                }
                            }
                        },
                        enabled = enabled && !isFetching
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.link_24px),
                            contentDescription = "Paste URL Link",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                trailingIcon = {
                    if (isFetching) {
                        CircularWavyProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (songLink.isNotBlank() && enabled) {
                        val isUrl = songLink.trim().startsWith("http://") || songLink.trim().startsWith("https://")
                        if (isUrl) {
                            FilledTonalIconButton(
                                onClick = onFetchClick,
                                shape = IconButtonDefaults.extraSmallRoundShape,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_forward_24px),
                                    contentDescription = "Fetch Song",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { onLinkChange("") }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.close_24px),
                                    contentDescription = "Clear search",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                colors = UiUtils.getTransparentTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true
            )
        }
    }
}

@Composable
fun SongPreviewCard(
    songDetails: SongDetails?,
    isFetching: Boolean,
    onRemoveSong: () -> Unit
) {
    JournalSongItem(
        details = songDetails,
        isFetching = isFetching,
        onRemove = onRemoveSong,
        onEdit = {}
    )
}

@Composable
private fun SearchResultItem(
    song: SongDetails,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                RestrictedAsyncImage(
                    imageUrl = song.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.arrow_forward_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddSongSheetHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .background(
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Attach Song",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}