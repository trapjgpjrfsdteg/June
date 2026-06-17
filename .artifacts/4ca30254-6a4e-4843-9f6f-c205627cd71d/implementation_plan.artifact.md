# Implementation Plan - EditorToolbar UI Update

This plan outlines the changes to `EditorToolbar.kt` to update the layout, add a new AI button with a glow effect, and polish the pop-out menus.

## Proposed Changes

### [Component] EditorToolbar (`app/src/main/java/com/denser/june/presentation/screens/editor/components/EditorToolbar.kt`)

#### [MODIFY] [EditorToolbar.kt](file:///C:/Users/trai/Documents/GitHub/June%20(fixed)/app/src/main/java/com/denser/june/presentation/screens/editor/components/EditorToolbar.kt)

1.  **AI Button Implementation**:
    *   Create a new private `@Composable` `AIButton` that includes:
        *   An `InfiniteTransition` to animate a glow effect (alpha and radius).
        *   A `Box` with `drawBehind` to render the breathing glow.
        *   A `FilledIconButton` with a pencil icon (`R.drawable.edit_24px`).
        *   A custom `Sparkles` overlay positioned at `Alignment.TopStart`.
2.  **Toolbar Rearrangement**:
    *   Update `FormatRowContent` to restructure the layout.
    *   Place the `Bold` button at the start.
    *   Group `Italic`, `Underline`, and `Highlight` into a scrollable row on the left side of the center.
    *   Insert the `AIButton` in the absolute center.
    *   Group `Lists`, `Sizes`, and `Advanced` into a scrollable row on the right side of the center.
    *   Keep the `Close` button at the end.
    *   Use `Modifier.weight(1f)` on the two scrollable rows to ensure the `AIButton` stays centered.
3.  **Pop-out Menu Polish**:
    *   Update the `Surface` shape in `Lists` and `Sizes` menus to use a consistent `RoundedCornerShape` (e.g., `12.dp` or `16.dp`).
    *   Remove the `border = BorderStroke(...)` from these `Surface` components to eliminate the "top line" effect.

## Verification Plan

### Manual Verification
*   Deploy the app and open the editor.
*   Toggle the formatting toolbar.
*   Verify the AI button is centered and has a breathing glow effect.
*   Verify the pencil icon has sparkles at the top-left.
*   Verify `Lists`, `Sizes`, and `Advanced` buttons are on the right.
*   Open `Lists` and `Sizes` menus and verify:
    *   Corners are smoothly rounded.
    *   The top border/line is gone.
    *   Existing logic (toggling styles) still works.
