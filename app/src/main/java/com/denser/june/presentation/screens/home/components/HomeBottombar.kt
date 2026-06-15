package com.denser.june.presentation.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.june.presentation.screens.home.HomeTab
import kotlinx.coroutines.launch

import com.denser.june.core.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeBottomBar(
    pagerState: PagerState,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Navigation Capsule Centered
        Surface(
            shape = CircleShape,
            color = Color(0xFF1E2022),
            border = BorderStroke(1.dp, Color(0xFF374151).copy(alpha = 0.4f)),
            shadowElevation = 8.dp,
            modifier = Modifier
                .height(64.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HomeTab.entries.forEachIndexed { index, tab ->
                    val isSelected = pagerState.currentPage == index

                    ToolbarTab(
                        selected = isSelected,
                        iconRes = tab.iconRes,
                        filledIconRes = tab.filledIconRes,
                        label = tab.label,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }

        // Decoupled FAB on the Right
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Surface(
                onClick = onFabClick,
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF004D53),
                shadowElevation = 8.dp,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.add_2_24px),
                        contentDescription = "New Journal",
                        tint = Color(0xFF76F1FF),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolbarTab(
    selected: Boolean,
    onClick: () -> Unit,
    iconRes: Int,
    filledIconRes: Int,
    label: String
) {
    val backgroundColor = if (selected) Color(0xFF2C3539) else Color.Transparent
    val contentColor = if (selected) Color.White else Color(0xFF9CA3AF)

    Surface(
        color = backgroundColor,
        shape = CircleShape,
        modifier = Modifier
            .height(44.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = if (selected) 16.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(if (selected) filledIconRes else iconRes),
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            
            AnimatedVisibility(
                visible = selected,
                enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
            ) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        color = contentColor,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            letterSpacing = 0.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}