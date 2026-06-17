package com.denser.june.presentation.screens.editor.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.insert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import com.denser.hyphen.model.StyleSets
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.ui.link.HyphenLinkConfig
import com.denser.hyphen.ui.mention.HyphenMentionConfig
import com.denser.hyphen.ui.style.HyphenStyleConfig
import com.denser.june.core.R
import com.denser.june.presentation.screens.editor.ToggleState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuneRichEditor(
    state: HyphenTextState,
    toggleState: ToggleState,
    modifier: Modifier = Modifier,
    onMarkdownChange: (String) -> Unit = {},
    styleConfig: HyphenStyleConfig = HyphenStyleConfig(),
    linkConfig: HyphenLinkConfig = HyphenLinkConfig(),
    mentionConfig: HyphenMentionConfig = HyphenMentionConfig(),
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    textStyle: TextStyle = LocalTextStyle.current,
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = LocalDensity.current

    LaunchedEffect(state.text, state.spans.toList()) {
        onMarkdownChange(state.toMarkdown())
    }

    val toggleHeaders = remember(state.text, toggleState.collapsed) {
        findToggleHeaders(state.text)
    }

    Box(modifier = modifier) {
        val wrappedDecorator = remember(state, toggleState, linkConfig, mentionConfig, textStyle) {
            object : TextFieldDecorator {
                @Composable
                override fun Decoration(innerTextField: @Composable () -> Unit) {
                    JuneInlineContentHost(
                        state = state,
                        toggleState = toggleState,
                        textLayoutResult = { textLayoutResult },
                        linkConfig = linkConfig,
                        mentionConfig = mentionConfig,
                        textStyle = textStyle,
                        modifier = Modifier,
                    ) {
                        innerTextField()
                    }
                }
            }
        }

        BasicTextField(
            state = state.textFieldState,
            modifier = Modifier.fillMaxWidth(),
            textStyle = textStyle.copy(color = colors.focusedTextColor),
            keyboardOptions = keyboardOptions,
            cursorBrush = SolidColor(colors.cursorColor),
            onTextLayout = { getResult ->
                textLayoutResult = getResult()
            },
            decorator = TextFieldDefaults.decorator(
                state = state.textFieldState,
                enabled = true,
                lineLimits = TextFieldLineLimits.Default,
                outputTransformation = null,
                interactionSource = remember { MutableInteractionSource() },
                placeholder = placeholder,
                colors = colors,
                container = {
                    TextFieldDefaults.Container(
                        enabled = true,
                        isError = false,
                        interactionSource = remember { MutableInteractionSource() },
                        colors = colors,
                    )
                }
            ).let { baseDecorator ->
                TextFieldDecorator { finalInner ->
                    baseDecorator.Decoration {
                        wrappedDecorator.Decoration {
                            finalInner()
                        }
                    }
                }
            },
            outputTransformation = {
                // 1. Calculate Hidden Ranges (Collapsible children)
                val hiddenRanges = calculateHiddenRanges(state.text, toggleState)
                
                // 2. Apply Collapsing (Remove children)
                for (range in hiddenRanges.reversed()) {
                    if (range.first < length && range.last <= length) {
                        replace(range.first, range.last, "") 
                    }
                }
                
                // 3. Hide '>> ' prefix visually
                val textAfterCollapse = asCharSequence().toString()
                var offset = 0
                val lines = textAfterCollapse.split('\n')
                val headersToDim = mutableListOf<IntRange>()
                for (line in lines) {
                    if (line.startsWith(">> ")) {
                        headersToDim.add(offset..offset + 3)
                    }
                    offset += line.length + 1
                }
                for (range in headersToDim.reversed()) {
                    if (range.first < length && range.last <= length) {
                        replace(range.first, range.last, "   ") // Replace '>> ' with 3 spaces
                    }
                }

                // 4. Apply Markdown Styles
                applyJuneMarkdownStyles(state, styleConfig, textStyle, this, hiddenRanges)
            },
            inputTransformation = {
                state.processInput(this)
            }
        )

        // Render Chevrons
        textLayoutResult?.let { layout ->
            val hiddenRanges = calculateHiddenRanges(state.text, toggleState)
            toggleHeaders.forEach { headerOffset ->
                val visualOffset = JuneOffsetMapper.toVisualOnlyHidden(headerOffset, hiddenRanges)
                if (visualOffset >= layout.layoutInput.text.length) return@forEach
                
                val lineIndex = layout.getLineForOffset(visualOffset)
                val top = layout.getLineTop(lineIndex)
                val isCollapsed = toggleState.isCollapsed(headerOffset)
                
                val rotation by animateFloatAsState(if (isCollapsed) 0f else 90f)

                Box(
                    modifier = Modifier
                        .offset(y = with(density) { top.toDp() } + 4.dp)
                        .padding(start = 4.dp)
                        .size(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            toggleState.toggle(headerOffset)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.keyboard_arrow_down_24px),
                        contentDescription = if (isCollapsed) "Expand" else "Collapse",
                        modifier = Modifier.size(16.dp).rotate(rotation),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
internal fun JuneInlineContentHost(
    state: HyphenTextState,
    toggleState: ToggleState,
    textLayoutResult: () -> TextLayoutResult?,
    linkConfig: HyphenLinkConfig,
    mentionConfig: HyphenMentionConfig,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val standardConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val contentMeasurables = subcompose("content", content)
        val contentPlaceable = contentMeasurables.first().measure(constraints)

        val overlaySpans = state.spans.filter {
            it.style is MarkupStyle.CheckboxChecked ||
                    it.style is MarkupStyle.CheckboxUnchecked ||
                    it.style is MarkupStyle.Link ||
                    it.style is MarkupStyle.Mention
        }

        val layoutResult = textLayoutResult()
        val hiddenRanges = calculateHiddenRanges(state.text, toggleState)

        val inlinePlaceables = overlaySpans.flatMap { span ->
            val key = "${span.start}_${span.style.hashCode()}"
            subcompose(key) {
                when (val style = span.style) {
                    is MarkupStyle.CheckboxUnchecked, is MarkupStyle.CheckboxChecked -> {
                        Box(modifier = Modifier.size(24.dp)) {
                            Checkbox(
                                checked = style is MarkupStyle.CheckboxChecked,
                                onCheckedChange = { state.toggleCheckbox(span.start) },
                                modifier = Modifier.scale(0.8f).align(Alignment.Center)
                            )
                        }
                    }
                    is MarkupStyle.Link -> {
                        val uriHandler = LocalUriHandler.current
                        Box(
                            modifier = Modifier.fillMaxSize().clickable {
                                if (style.url.isNotBlank()) {
                                    linkConfig.onFollowLink?.invoke(style.url) ?: uriHandler.openUri(style.url)
                                }
                            }
                        )
                    }
                    else -> {}
                }
            }.map { measurable ->
                val transformedStart = JuneOffsetMapper.toVisual(span.start, state, hiddenRanges)
                val transformedEnd = JuneOffsetMapper.toVisual(span.end, state, hiddenRanges)
                
                val finalConstraints = if ((span.style is MarkupStyle.Link) && layoutResult != null) {
                    val textLen = layoutResult.layoutInput.text.length
                    if (textLen == 0 || transformedStart >= textLen) {
                        Constraints.fixed(0, 0)
                    } else {
                        val startIdx = transformedStart.coerceIn(0, textLen - 1)
                        val endIdx = (transformedEnd - 1).coerceIn(startIdx, textLen - 1)
                        
                        val startBox = layoutResult.getBoundingBox(startIdx)
                        val endBox = layoutResult.getBoundingBox(endIdx)

                        val width = (endBox.right - startBox.left).coerceAtLeast(0f).roundToInt()
                        val height = (endBox.bottom - startBox.top).coerceAtLeast(0f).roundToInt()
                        Constraints.fixed(width, height)
                    }
                } else {
                    standardConstraints
                }
                Triple(span, transformedStart, measurable.measure(finalConstraints))
            }
        }

        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.placeRelative(0, 0)
            
            if (layoutResult != null) {
                val textLen = layoutResult.layoutInput.text.length
                inlinePlaceables.forEach { (span, visualStart, placeable) ->
                    if (textLen == 0 || visualStart >= textLen) return@forEach
                    val boundingBox = layoutResult.getBoundingBox(visualStart)

                    val lineTop = boundingBox.top.roundToInt()
                    val lineBottom = boundingBox.bottom.roundToInt()
                    val lineHeight = lineBottom - lineTop
                    val x = boundingBox.left.roundToInt()

                    if (span.style is MarkupStyle.Link || span.style is MarkupStyle.Mention) {
                        placeable.placeRelative(x, lineTop)
                    } else {
                        val y = lineTop + (lineHeight - placeable.height) / 2
                        placeable.placeRelative(x, y)
                    }
                }
            }
        }
    }
}

private fun findToggleHeaders(text: String): List<Int> {
    val headers = mutableListOf<Int>()
    val lines = text.split('\n')
    var offset = 0
    
    for (line in lines) {
        if (line.startsWith(">> ")) {
            headers.add(offset)
        }
        offset += line.length + 1
    }
    return headers
}

private fun calculateHiddenRanges(
    originalText: String,
    toggleState: ToggleState
): List<IntRange> {
    val lines = originalText.split('\n')
    var offset = 0
    var currentHeaderCollapsed = false
    val rangesToHide = mutableListOf<IntRange>()
    
    for (line in lines) {
        if (line.startsWith(">> ")) {
            currentHeaderCollapsed = toggleState.isCollapsed(offset)
        } else if (line.startsWith(" ") || line.isEmpty()) {
            // Child line (indented or empty line within a block)
            if (currentHeaderCollapsed) {
                rangesToHide.add(offset..offset + line.length)
            }
        } else {
            // Non-indented line, breaks the block
            currentHeaderCollapsed = false
        }
        offset += line.length + 1
    }
    return rangesToHide
}

private fun applyJuneMarkdownStyles(
    state: HyphenTextState,
    styleConfig: HyphenStyleConfig,
    baseTextStyle: TextStyle,
    buffer: androidx.compose.foundation.text.input.TextFieldBuffer,
    hiddenRanges: List<IntRange> = emptyList()
) {
    with(buffer) {
        val needsBaselineAnchor = state.spans.any { it.start == 0 && it.style in StyleSets.allHeadings }
        if (needsBaselineAnchor) {
            insert(0, "\u200B")
        }

        val checkboxes = state.spans
            .filter { it.style is MarkupStyle.CheckboxUnchecked || it.style is MarkupStyle.CheckboxChecked }
            .sortedByDescending { it.start }

        val adjustment = if (needsBaselineAnchor) 1 else 0
        checkboxes.forEach { cb ->
            val visualStart = JuneOffsetMapper.toVisualOnlyHidden(cb.start + adjustment, hiddenRanges).coerceIn(0, length)
            val visualEnd = (visualStart + 6).coerceAtMost(length)
            if (visualStart < visualEnd) {
                replace(visualStart, visualEnd, "  ")
            }
        }

        val baseSpanStyle = baseTextStyle.toSpanStyle()
        val currentTextSeq = asCharSequence()
        for (i in currentTextSeq.indices) {
            if (currentTextSeq[i] == '\n') {
                addStyle(baseSpanStyle, i, i + 1)
            }
        }

        state.spans.forEach { span ->
            val visualStart = JuneOffsetMapper.toVisual(span.start, state, hiddenRanges).coerceIn(0, length)
            val visualEnd = JuneOffsetMapper.toVisual(span.end, state, hiddenRanges).coerceIn(0, length)
            if (visualStart >= visualEnd) return@forEach

            when (val style = span.style) {
                is MarkupStyle.Bold -> addStyle(styleConfig.boldStyle, visualStart, visualEnd)
                is MarkupStyle.Italic -> addStyle(styleConfig.italicStyle, visualStart, visualEnd)
                is MarkupStyle.Underline -> addStyle(styleConfig.underlineStyle, visualStart, visualEnd)
                is MarkupStyle.Strikethrough -> addStyle(styleConfig.strikethroughStyle, visualStart, visualEnd)
                is MarkupStyle.Highlight -> addStyle(styleConfig.highlightStyle, visualStart, visualEnd)
                is MarkupStyle.InlineCode -> addStyle(styleConfig.inlineCodeStyle, visualStart, visualEnd)
                is MarkupStyle.Link -> addStyle(styleConfig.linkStyle, visualStart, visualEnd)
                is MarkupStyle.Mention -> {
                    val customStyle = styleConfig.mentionStyles[style.scheme]
                    addStyle(customStyle ?: styleConfig.mentionStyle, visualStart, visualEnd)
                }
                is MarkupStyle.Blockquote -> addStyle(styleConfig.blockquoteSpanStyle, visualStart, visualEnd)

                is MarkupStyle.BulletList -> {
                    val prefixEnd = (visualStart + 2).coerceAtMost(visualEnd)
                    styleConfig.bulletListStyle.prefixStyle?.let { addStyle(it, visualStart, prefixEnd) }
                    styleConfig.bulletListStyle.contentStyle?.let { addStyle(it, prefixEnd, visualEnd) }
                }

                is MarkupStyle.OrderedList -> {
                    val lineText = currentTextSeq.substring(visualStart, visualEnd)
                    val dotIndex = lineText.indexOf('.')
                    val prefixLen = if (dotIndex != -1) (dotIndex + 2).coerceAtMost(lineText.length) else 3
                    val prefixEnd = (visualStart + prefixLen).coerceAtMost(visualEnd)
                    styleConfig.orderedListStyle.prefixStyle?.let { addStyle(it, visualStart, prefixEnd) }
                    styleConfig.orderedListStyle.contentStyle?.let { addStyle(it, prefixEnd, visualEnd) }
                }

                is MarkupStyle.CheckboxUnchecked -> {
                    val slotEnd = (visualStart + 2).coerceAtMost(visualEnd)
                    addStyle(SpanStyle(letterSpacing = 0.8.em), visualStart, slotEnd)
                    styleConfig.checkboxUncheckedStyle?.let { addStyle(it, slotEnd, visualEnd) }
                }

                is MarkupStyle.CheckboxChecked -> {
                    val slotEnd = (visualStart + 2).coerceAtMost(visualEnd)
                    addStyle(SpanStyle(letterSpacing = 0.8.em), visualStart, slotEnd)
                    styleConfig.checkboxCheckedStyle?.let { addStyle(it, slotEnd, visualEnd) }
                }

                is MarkupStyle.H1 -> addStyle(styleConfig.h1Style, visualStart, visualEnd)
                is MarkupStyle.H2 -> addStyle(styleConfig.h2Style, visualStart, visualEnd)
                is MarkupStyle.H3 -> addStyle(styleConfig.h3Style, visualStart, visualEnd)
                is MarkupStyle.H4 -> addStyle(styleConfig.h4Style, visualStart, visualEnd)
                is MarkupStyle.H5 -> addStyle(styleConfig.h5Style, visualStart, visualEnd)
                is MarkupStyle.H6 -> addStyle(styleConfig.h6Style, visualStart, visualEnd)
            }
        }
    }
}

private object JuneOffsetMapper {
    fun toVisual(originalOffset: Int, state: HyphenTextState, hiddenRanges: List<IntRange>): Int {
        var offset = originalOffset
        
        // Account for hidden ranges first
        for (range in hiddenRanges) {
            if (range.first < originalOffset) {
                val hiddenCount = (minOf(range.last, originalOffset) - range.first)
                offset -= hiddenCount
            }
        }

        val hasHeadingAnchor = state.spans.any { it.start == 0 && it.style in StyleSets.allHeadings }
        var visualOffset = offset + (if (hasHeadingAnchor) 1 else 0)

        val checkboxes = state.spans
            .filter { it.style is MarkupStyle.CheckboxUnchecked || it.style is MarkupStyle.CheckboxChecked }
            .sortedBy { it.start }

        for (cb in checkboxes) {
            val visualCbStart = toVisualOnlyHidden(cb.start, hiddenRanges)
            if (visualCbStart < visualOffset) {
                val charsBeforePrefix = (visualOffset - visualCbStart).coerceAtMost(6)
                if (charsBeforePrefix >= 6) {
                    visualOffset -= 4
                } else if (charsBeforePrefix >= 3) {
                    visualOffset -= (charsBeforePrefix - 1)
                } else {
                    visualOffset -= charsBeforePrefix
                }
            }
        }
        return visualOffset
    }

    fun toVisualOnlyHidden(originalOffset: Int, hiddenRanges: List<IntRange>): Int {
        var offset = originalOffset
        for (range in hiddenRanges) {
            if (range.first < originalOffset) {
                val hiddenCount = (minOf(range.last, originalOffset) - range.first)
                offset -= hiddenCount
            }
        }
        return offset
    }
}
