package com.denser.june.presentation.screens.editor.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.TriggerState
import com.denser.hyphen.state.HyphenTextState
import com.denser.june.core.R
import com.denser.june.presentation.utils.TagUtils

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape

@Composable
fun EditorToolbar(
    state: HyphenTextState,
    modifier: Modifier = Modifier,
    activeTrigger: TriggerState? = null,
    tagSuggestions: List<String> = emptyList(),
    currentTags: List<String> = emptyList(),
    onTagSelect: (tag: String) -> Unit = {},
    onAddClick: () -> Unit = {},
    onSummaryClick: () -> Unit = {},
    onSendClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onReadClick: () -> Unit = {},
) {
    val triggerPrefix = activeTrigger?.config?.trigger
    val isTagMode = triggerPrefix == "@" || triggerPrefix == "#"
    var isFormatToolbarOpen by rememberSaveable { mutableStateOf(false) }

    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val isKeyboardOpen = imeBottom > 0.dp

    val navBarsPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    
    val animSpec = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val toolbarTopRadius by animateDpAsState(
        targetValue = if (isKeyboardOpen) 0.dp else 20.dp,
        animationSpec = animSpec,
        label = "toolbarTopRadius"
    )
    val toolbarBottomRadius by animateDpAsState(
        targetValue = if (isKeyboardOpen) 0.dp else 40.dp,
        animationSpec = animSpec,
        label = "toolbarBottomRadius"
    )
    val sidePadding by animateDpAsState(
        targetValue = if (isKeyboardOpen) 8.dp else 10.dp,
        animationSpec = animSpec,
        label = "sidePadding"
    )
    val bottomPadding by animateDpAsState(
        targetValue = if (isKeyboardOpen) 8.dp else 12.dp,
        animationSpec = animSpec,
        label = "bottomPadding"
    )
    val buttonSize by animateDpAsState(
        targetValue = if (isKeyboardOpen) 38.dp else 42.dp,
        animationSpec = animSpec,
        label = "buttonSize"
    )
    val navPaddingAnim by animateDpAsState(
        targetValue = if (isKeyboardOpen) 0.dp else navBarsPadding,
        animationSpec = animSpec,
        label = "navPadding"
    )

    val leadingBottomStartRadius by animateDpAsState(
        targetValue = if (isKeyboardOpen) 19.dp else 34.dp,
        animationSpec = animSpec,
        label = "leadingBottomStartRadius"
    )

    val trailingBottomEndRadius by animateDpAsState(
        targetValue = if (isKeyboardOpen) 19.dp else 34.dp,
        animationSpec = animSpec,
        label = "trailingBottomEndRadius"
    )

    val toolbarShape = RoundedCornerShape(
        topStart = toolbarTopRadius,
        topEnd = toolbarTopRadius,
        bottomStart = toolbarBottomRadius,
        bottomEnd = toolbarBottomRadius
    )

    val leadingButtonShape = RoundedCornerShape(
        topStart = if (isKeyboardOpen) 19.dp else 18.dp,
        topEnd = if (isKeyboardOpen) 19.dp else 18.dp,
        bottomStart = leadingBottomStartRadius,
        bottomEnd = if (isKeyboardOpen) 19.dp else 18.dp
    )

    val trailingButtonShape = RoundedCornerShape(
        topStart = if (isKeyboardOpen) 19.dp else 18.dp,
        topEnd = if (isKeyboardOpen) 19.dp else 18.dp,
        bottomStart = if (isKeyboardOpen) 19.dp else 18.dp,
        bottomEnd = trailingBottomEndRadius
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = toolbarShape,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(bottom = navPaddingAnim)
        ) {
            if (isKeyboardOpen) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = sidePadding,
                        end = sidePadding,
                        top = 8.dp,
                        bottom = bottomPadding
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isFormatToolbarOpen) {
                    // Fullscreen Formatting Mode
                    FormatRowContent(
                        state = state,
                        buttonSize = buttonSize,
                        leadingShape = leadingButtonShape,
                        trailingShape = trailingButtonShape,
                        onClose = { isFormatToolbarOpen = false }
                    )
                } else {
                    // Standard Mode
                    StandardRowContent(
                        state = state,
                        isKeyboardOpen = isKeyboardOpen,
                        activeTrigger = activeTrigger,
                        tagSuggestions = tagSuggestions,
                        currentTags = currentTags,
                        onTagSelect = onTagSelect,
                        onAddClick = onAddClick,
                        onSummaryClick = onSummaryClick,
                        isFormatToolbarOpen = isFormatToolbarOpen,
                        onToggleFormat = { isFormatToolbarOpen = !it },
                        buttonSize = buttonSize,
                        leadingButtonShape = leadingButtonShape,
                        isTagMode = isTagMode
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.StandardRowContent(
    state: HyphenTextState,
    isKeyboardOpen: Boolean,
    activeTrigger: TriggerState?,
    tagSuggestions: List<String>,
    currentTags: List<String>,
    onTagSelect: (String) -> Unit,
    onAddClick: () -> Unit,
    onSummaryClick: () -> Unit,
    isFormatToolbarOpen: Boolean,
    onToggleFormat: (Boolean) -> Unit,
    buttonSize: androidx.compose.ui.unit.Dp,
    leadingButtonShape: Shape,
    isTagMode: Boolean
) {
    FilledIconButton(
        onClick = onAddClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        ),
        shape = leadingButtonShape,
        modifier = Modifier.size(buttonSize)
    ) {
        Icon(
            painter = painterResource(R.drawable.add_circle_24px),
            contentDescription = "Add Item",
            modifier = Modifier.size(20.dp)
        )
    }

    FilledIconButton(
        onClick = { onToggleFormat(isFormatToolbarOpen) },
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (isFormatToolbarOpen) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = if (isFormatToolbarOpen) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        ),
        shape = CircleShape,
        modifier = Modifier.size(buttonSize)
    ) {
        Icon(
            painter = painterResource(R.drawable.format_color_text_24px),
            contentDescription = "Format Text",
            modifier = Modifier.size(20.dp)
        )
    }

    Box(modifier = Modifier.weight(1f).height(42.dp), contentAlignment = Alignment.Center) {
        if (isTagMode && activeTrigger != null) {
            TagChipsContent(
                trigger = activeTrigger,
                tagSuggestions = tagSuggestions,
                currentTags = currentTags,
                hyphenState = state,
                onTagSelect = onTagSelect,
            )
        } else if (!isKeyboardOpen) {
            // AI Summary Pill
            Surface(
                onClick = onSummaryClick,
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        )
                    )
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.routine_24px_fill),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    FilledIconButton(
        onClick = { state.undo() },
        enabled = state.canUndo,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        modifier = Modifier.size(buttonSize)
    ) {
        Icon(
            painter = painterResource(R.drawable.undo_24px),
            contentDescription = "Undo",
            modifier = Modifier.size(20.dp)
        )
    }

    FilledIconButton(
        onClick = { state.redo() },
        enabled = state.canRedo,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        modifier = Modifier.size(buttonSize)
    ) {
        Icon(
            painter = painterResource(R.drawable.redo_24px),
            contentDescription = "Redo",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun RowScope.FormatRowContent(
    state: HyphenTextState,
    buttonSize: androidx.compose.ui.unit.Dp,
    leadingShape: Shape,
    trailingShape: Shape,
    onClose: () -> Unit
) {
    FormatToggleButton(
        icon = R.drawable.format_bold_24px,
        contentDescription = "Bold",
        isActive = state.hasStyle(MarkupStyle.Bold),
        onClick = { state.toggleStyle(MarkupStyle.Bold) },
        shape = leadingShape,
        modifier = Modifier.size(buttonSize)
    )

    Box(modifier = Modifier.weight(1f).height(42.dp), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FormatToggleButton(
                icon = R.drawable.format_italic_24px,
                contentDescription = "Italic",
                isActive = state.hasStyle(MarkupStyle.Italic),
                onClick = { state.toggleStyle(MarkupStyle.Italic) }
            )
            FormatToggleButton(
                icon = R.drawable.format_underlined_24px,
                contentDescription = "Underline",
                isActive = state.hasStyle(MarkupStyle.Underline),
                onClick = { state.toggleStyle(MarkupStyle.Underline) }
            )
            FormatToggleButton(
                icon = R.drawable.strikethrough_s_24px,
                contentDescription = "Strikethrough",
                isActive = state.hasStyle(MarkupStyle.Strikethrough),
                onClick = { state.toggleStyle(MarkupStyle.Strikethrough) }
            )
            FormatToggleButton(
                icon = R.drawable.format_ink_highlighter_24px,
                contentDescription = "Highlight",
                isActive = state.hasStyle(MarkupStyle.Highlight),
                onClick = { state.toggleStyle(MarkupStyle.Highlight) }
            )
            FormatToggleButton(
                icon = R.drawable.link_24px,
                contentDescription = "Link",
                isActive = state.hasStyle(MarkupStyle.Link("")),
                onClick = { state.toggleLink() }
            )

            VerticalDivider(
                modifier = Modifier.height(16.dp).padding(horizontal = 2.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            FormatToggleButton(
                icon = R.drawable.format_quote_24px,
                contentDescription = "Blockquote",
                isActive = state.hasStyle(MarkupStyle.Blockquote),
                onClick = { state.toggleStyle(MarkupStyle.Blockquote) }
            )
            FormatToggleButton(
                icon = R.drawable.code_24px,
                contentDescription = "Inline Code",
                isActive = state.hasStyle(MarkupStyle.InlineCode),
                onClick = { state.toggleStyle(MarkupStyle.InlineCode) }
            )
            FormatToggleButton(
                icon = R.drawable.format_list_bulleted_24px,
                contentDescription = "Bullet List",
                isActive = state.hasStyle(MarkupStyle.BulletList),
                onClick = { state.toggleStyle(MarkupStyle.BulletList) }
            )
            FormatToggleButton(
                icon = R.drawable.format_list_numbered_24px,
                contentDescription = "Ordered List",
                isActive = state.hasStyle(MarkupStyle.OrderedList),
                onClick = { state.toggleStyle(MarkupStyle.OrderedList) }
            )

            VerticalDivider(
                modifier = Modifier.height(16.dp).padding(horizontal = 2.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            FormatToggleButton(
                icon = R.drawable.format_h1_24px,
                contentDescription = "Heading 1",
                isActive = state.hasStyle(MarkupStyle.H1),
                onClick = { state.toggleStyle(MarkupStyle.H1) }
            )
            FormatToggleButton(
                icon = R.drawable.format_h2_24px,
                contentDescription = "Heading 2",
                isActive = state.hasStyle(MarkupStyle.H2),
                onClick = { state.toggleStyle(MarkupStyle.H2) }
            )
            FormatToggleButton(
                icon = R.drawable.format_h3_24px,
                contentDescription = "Heading 3",
                isActive = state.hasStyle(MarkupStyle.H3),
                onClick = { state.toggleStyle(MarkupStyle.H3) }
            )
            FormatToggleButton(
                icon = R.drawable.format_h4_24px,
                contentDescription = "Heading 4",
                isActive = state.hasStyle(MarkupStyle.H4),
                onClick = { state.toggleStyle(MarkupStyle.H4) }
            )
            FormatToggleButton(
                icon = R.drawable.format_h5_24px,
                contentDescription = "Heading 5",
                isActive = state.hasStyle(MarkupStyle.H5),
                onClick = { state.toggleStyle(MarkupStyle.H5) }
            )
            FormatToggleButton(
                icon = R.drawable.format_h6_24px,
                contentDescription = "Heading 6",
                isActive = state.hasStyle(MarkupStyle.H6),
                onClick = { state.toggleStyle(MarkupStyle.H6) }
            )
        }
    }

    FilledIconButton(
        onClick = onClose,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        ),
        shape = trailingShape,
        modifier = Modifier.size(buttonSize)
    ) {
        Icon(
            painter = painterResource(R.drawable.close_24px),
            contentDescription = "Close Formatting",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun TagChipsContent(
    trigger: TriggerState,
    tagSuggestions: List<String>,
    currentTags: List<String>,
    hyphenState: HyphenTextState,
    onTagSelect: (tag: String) -> Unit,
) {
    val prefix = trigger.config.trigger
    val query  = trigger.query.trim()
    val fullTag = "$prefix$query"

    val combinedSuggestions = remember(tagSuggestions, currentTags) {
        (tagSuggestions + currentTags).distinct()
    }

    val filtered = remember(combinedSuggestions, prefix, query) {
        combinedSuggestions
            .filter { it.startsWith(prefix) }
            .filter { query.isBlank() || it.removePrefix(prefix).contains(query, ignoreCase = true) }
    }

    val showCreate = query.isNotBlank() &&
            filtered.none { it.equals(fullTag, ignoreCase = true) }

    Row(
        modifier = Modifier
            .fillMaxHeight()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        filtered.forEach { tag ->
            FilterChip(
                selected = false,
                onClick = {
                    onTagSelect(tag)
                    hyphenState.completeMention(
                        id = tag.removePrefix(prefix),
                        display = tag,
                        trigger = trigger,
                    )
                },
                label = { Text(tag, fontSize = 13.sp) },
                shape = RoundedCornerShape(8.dp),
            )
        }

        if (filtered.isEmpty() && !showCreate) {
            Text(
                text = "Type to search…",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        if (showCreate) {
            SuggestionChip(
                onClick = {
                    onTagSelect(fullTag)
                    hyphenState.completeMention(
                        id = query,
                        display = fullTag,
                        trigger = trigger,
                    )
                },
                label = {
                    Text(
                        fullTag,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                colors = TagUtils.getTagSuggestionChipColors(fullTag),
                border = null,
                shape = RoundedCornerShape(8.dp),
            )
        }
    }
}

@Composable
private fun FormatButtonsContent(state: HyphenTextState) {
    // This is now replaced by FormatRowContent when isFormatToolbarOpen is true
}

@Composable
private fun FormatToggleButton(
    isActive: Boolean,
    onClick: () -> Unit,
    icon: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    IconToggleButton(
        checked = isActive,
        onCheckedChange = { onClick() },
        modifier = modifier
            .focusProperties { canFocus = false },
        shape = shape,
        colors = IconButtonDefaults.iconToggleButtonColors(
            checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            painterResource(icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}