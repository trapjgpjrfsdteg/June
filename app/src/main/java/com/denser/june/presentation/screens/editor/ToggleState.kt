package com.denser.june.presentation.screens.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap

class ToggleState(
    collapsedOffsets: Map<Int, Boolean> = emptyMap()
) {
    private val _collapsed = mutableStateMapOf<Int, Boolean>().apply {
        putAll(collapsedOffsets)
    }

    val collapsed: Map<Int, Boolean> get() = _collapsed

    fun isCollapsed(offset: Int): Boolean = _collapsed[offset] ?: false

    fun toggle(offset: Int) {
        _collapsed[offset] = !isCollapsed(offset)
    }

    companion object {
        val Saver: Saver<ToggleState, *> = listSaver(
            save = { state -> state.collapsed.filter { it.value }.keys.toList() },
            restore = { offsets -> ToggleState(offsets.associate { it to true }) }
        )
    }
}

@Composable
fun rememberToggleState(): ToggleState {
    return rememberSaveable(saver = ToggleState.Saver) {
        ToggleState()
    }
}
