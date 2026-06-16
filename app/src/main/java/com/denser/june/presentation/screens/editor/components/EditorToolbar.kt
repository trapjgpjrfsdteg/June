package com.denser.june.presentation.screens.editor.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind // Added missing import
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
    val mainBarHeight by animateDpAsState(
        targetValue = if (isKeyboardOpen) 54.dp else 62.dp,
        animationSpec = animSpec,
        label = "mainBarHeight"
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

    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(mainBarHeight + navPaddingAnim),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = toolbarShape,
            tonalElevation = 0.dp,
        ) {}

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = navPaddingAnim)
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
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isFormatToolbarOpen) {
                    FormatRowContent(
                        state = state,
                        buttonSize = buttonSize,
                        leadingShape = leadingButtonShape,
                        trailingShape = trailingButtonShape,
                        onClose = {
                            isFormatToolbarOpen = false
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onAIActionClick = onSummaryClick
                    )
                } else {
                    StandardRowContent(
                        state = state,
                        isKeyboardOpen = isKeyboardOpen,
                        activeTrigger = activeTrigger,
                        tagSuggestions = tagSuggestions,
                        currentTags = currentTags,
                        onTagSelect = onTagSelect,
                        onAddClick = {
                            onAddClick()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onSummaryClick = {
                            onSummaryClick()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        isFormatToolbarOpen = isFormatToolbarOpen,
                        onToggleFormat = {
                            isFormatToolbarOpen = !it
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
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
    val haptic = LocalHapticFeedback.current

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

    Box(modifier = Modifier.weight(1f).height(42.dp), contentAlignment = Alignment.BottomCenter) {
        if (isTagMode && activeTrigger != null) {
            TagChipsContent(
                trigger = activeTrigger,
                tagSuggestions = tagSuggestions,
                currentTags = currentTags,
                hyphenState = state,
                onTagSelect = onTagSelect,
            )
        } else if (!isKeyboardOpen) {
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
                modifier = Modifier.height(36.dp).padding(bottom = 2.dp)
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
        onClick = {
            state.undo()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
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
        onClick = {
            state.redo()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
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
    onClose: () -> Unit,
    onAIActionClick: () -> Unit
) {
    var isListsOpen by rememberSaveable { mutableStateOf(false) }
    var isSizesOpen by rememberSaveable { mutableStateOf(false) }
    var isAdvancedOpen by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "aiGlowTransition")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha animate"
    )
    val glowRadiusScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowRadius animate"
    )

    FormatToggleButton(
        icon = R.drawable.format_bold_24px,
        contentDescription = "Bold",
        isActive = state.hasStyle(MarkupStyle.Bold),
        onClick = { state.toggleStyle(MarkupStyle.Bold) },
        shape = leadingShape,
        modifier = Modifier.size(buttonSize)
    )

    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            // Group 1: Left-aligned core tools
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                FormatToggleButton(
                    icon = R.drawable.format_italic_24px,
                    contentDescription = "Italic",
                    isActive = state.hasStyle(MarkupStyle.Italic),
                    onClick = { state.toggleStyle(MarkupStyle.Italic) },
                    modifier = Modifier.size(buttonSize)
                )
                FormatToggleButton(
                    icon = R.drawable.format_underlined_24px,
                    contentDescription = "Underline",
                    isActive = state.hasStyle(MarkupStyle.Underline),
                    onClick = { state.toggleStyle(MarkupStyle.Underline) },
                    modifier = Modifier.size(buttonSize)
                )
                FormatToggleButton(
                    icon = R.drawable.format_ink_highlighter_24px,
                    contentDescription = "Highlight",
                    isActive = state.hasStyle(MarkupStyle.Highlight),
                    onClick = { state.toggleStyle(MarkupStyle.Highlight) },
                    modifier = Modifier.size(buttonSize)
                )
            }

            // Group 2: Center-aligned custom AI action button
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .align(Alignment.Bottom),
                contentAlignment = Alignment.Center
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                FilledIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAIActionClick()
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(buttonSize)
                        .drawBehind { // Fixed inline reference error
                            drawCircle(
                                color = primaryColor.copy(alpha = glowAlpha),
                                radius = (size.minDimension / 2f) * glowRadiusScale
                            )
                        }
                ) {
                    Box(modifier = Modifier.size(24.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.edit_24px),
                            contentDescription = "AI Assistant Actions",
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.BottomEnd)
                        )
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .size(9.dp)
                                .align(Alignment.TopStart)
                        ) {
                            val starPath = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width / 2f, 0f)
                                quadraticTo(size.width / 2f, size.height / 2f, size.width, size.height / 2f)
                                quadraticTo(size.width / 2f, size.height / 2f, size.width / 2f, size.height)
                                quadraticTo(size.width / 2f, size.height / 2f, 0f, size.height / 2f)
                                quadraticTo(size.width / 2f, size.height / 2f, size.width / 2f, 0f)
                                close()
                            }
                            drawPath(starPath, primaryColor)
                        }
                    }
                }
            }

            // Group 3: Right-aligned collapsible & functional structural options
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isListsOpen,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(expandFrom = Alignment.Bottom),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically(shrinkTowards = Alignment.Bottom)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 4.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FormatToggleButton(
                                    isActive = state.hasStyle(MarkupStyle.BulletList),
                                    onClick = {
                                        state.toggleStyle(MarkupStyle.BulletList)
                                        isListsOpen = false
                                    },
                                    icon = R.drawable.format_list_bulleted_24px,
                                    contentDescription = "Bullet List",
                                    modifier = Modifier.size(buttonSize)
                                )
                                FormatToggleButton(
                                    isActive = state.hasStyle(MarkupStyle.OrderedList),
                                    onClick = {
                                        state.toggleStyle(MarkupStyle.OrderedList)
                                        isListsOpen = false
                                    },
                                    icon = R.drawable.format_list_numbered_24px,
                                    contentDescription = "Ordered List",
                                    modifier = Modifier.size(buttonSize)
                                )
                                FormatToggleButton(
                                    isActive = false,
                                    onClick = { isListsOpen = false },
                                    icon = R.drawable.check_24px,
                                    contentDescription = "Check List Token Placeholder",
                                    modifier = Modifier.size(buttonSize)
                                )
                            }
                        }
                    }

                    FilledIconButton(
                        onClick = {
                            isListsOpen = !isListsOpen
                            if (isListsOpen) isSizesOpen = false
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isListsOpen) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = if (isListsOpen) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        ),
                        modifier = Modifier.size(buttonSize)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.format_list_bulleted_24px),
                            contentDescription = "Lists Pop-out Trigger Menu",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSizesOpen,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(expandFrom = Alignment.Bottom),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically(shrinkTowards = Alignment.Bottom)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 4.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val sizeToggles = listOf(
                                    MarkupStyle.H1 to R.drawable.format_h1_24px to "Heading 1",
                                    MarkupStyle.H2 to R.drawable.format_h2_24px to "Heading 2",
                                    MarkupStyle.H3 to R.drawable.format_h3_24px to "Heading 3",
                                    MarkupStyle.H4 to R.drawable.format_h4_24px to "Heading 4",
                                    MarkupStyle.H5 to R.drawable.format_h5_24px to "Heading 5",
                                    MarkupStyle.H6 to R.drawable.format_h6_24px to "Heading 6"
                                )
                                sizeToggles.forEach { (pair, desc) ->
                                    val (style, iconRes) = pair
                                    FormatToggleButton(
                                        isActive = state.hasStyle(style),
                                        onClick = {
                                            state.toggleStyle(style)
                                            isSizesOpen = false
                                        },
                                        icon = iconRes,
                                        contentDescription = desc,
                                        modifier = Modifier.size(buttonSize)
                                    )
                                }
                            }
                        }
                    }

                    FilledIconButton(
                        onClick = {
                            isSizesOpen = !isSizesOpen
                            if (isSizesOpen) isListsOpen = false
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isSizesOpen) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = if (isSizesOpen) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        ),
                        modifier = Modifier.size(buttonSize)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.format_size_24px),
                            contentDescription = "Sizes Typography Menu Button",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilledIconButton(
                        onClick = {
                            isAdvancedOpen = !isAdvancedOpen
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isAdvancedOpen) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = if (isAdvancedOpen) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        ),
                        modifier = Modifier.size(buttonSize)
                    ) {
                        Icon(
                            painter = painterResource(if (isAdvancedOpen) R.drawable.chevron_left_24px else R.drawable.chevron_right_24px),
                            contentDescription = "Advanced Options Expand/Collapse Button",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = isAdvancedOpen,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(expandFrom = Alignment.Start),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally(shrinkTowards = Alignment.Start)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            FormatToggleButton(
                                isActive = state.hasStyle(MarkupStyle.Strikethrough),
                                onClick = { state.toggleStyle(MarkupStyle.Strikethrough) },
                                icon = R.drawable.strikethrough_s_24px,
                                contentDescription = "Strikethrough",
                                modifier = Modifier.size(buttonSize)
                            )
                            FormatToggleButton(
                                isActive = state.hasStyle(MarkupStyle.Blockquote),
                                onClick = { state.toggleStyle(MarkupStyle.Blockquote) },
                                icon = R.drawable.format_quote_24px,
                                contentDescription = "Blockquote",
                                modifier = Modifier.size(buttonSize)
                            )
                            FormatToggleButton(
                                isActive = state.hasStyle(MarkupStyle.InlineCode),
                                onClick = { state.toggleStyle(MarkupStyle.InlineCode) },
                                icon = R.drawable.code_24px,
                                contentDescription = "Inline Code",
                                modifier = Modifier.size(buttonSize)
                            )
                            FormatToggleButton(
                                isActive = state.hasStyle(MarkupStyle.Link("")),
                                onClick = { state.toggleLink() },
                                icon = R.drawable.link_24px,
                                contentDescription = "Link Connection Option",
                                modifier = Modifier.size(buttonSize)
                            )
                        }
                    }
                }
            }
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
            contentDescription = "Close Formatting Strip Menu",
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
    // Replaced by expanded minimalistic dynamic items inside FormatRowContent
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
    val haptic = LocalHapticFeedback.current
    IconToggleButton(
        checked = isActive,
        onCheckedChange = {
            onClick()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        modifier = modifier.focusProperties { canFocus = false },
        shape = shape,
        colors = IconButtonDefaults.iconToggleButtonColors(
            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f),
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
