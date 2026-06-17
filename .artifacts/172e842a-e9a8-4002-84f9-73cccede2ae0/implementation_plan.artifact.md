# Implementation Plan: Notion-like Toggle Lists & Refined Toolbar Icons

Refactor the "Toggle List" feature to use a custom Markdown syntax (`>> `) instead of the built-in blockquote style. Implement Notion-like child-hiding based on indentation and polish the list icons to match the "two-line" specification.

## User Review Required

> [!IMPORTANT]
> I am moving away from the library's `Blockquote` style for toggles. The new syntax will be `>> Header` for the toggle header. Child content must be indented with spaces to be recognized as part of the toggle, matching Notion's behavior.

> [!TIP]
> The "Toggle List" feature will now hide all subsequent indented lines when the header is collapsed. This allows for rich content (bullets, checkboxes, images) to be nested inside a toggle.

## Proposed Changes

### [Component] Editor Toolbar (`app`)

#### [MODIFY] [EditorToolbar.kt](file:///C:/Users/trai/Documents/GitHub/June (fixed)/app/src/main/java/com/denser/june/presentation/screens/editor/components/EditorToolbar.kt)
*   **Action Update**: Change the "Toggle List" action to insert/remove the `>> ` prefix manually instead of using `state.toggleStyle(MarkupStyle.Blockquote)`.
*   **Icon Updates**:
    *   Refine `ic_list_bulleted_2`, `ic_list_numbered_2`, `ic_list_check_2`, and `ic_list_toggle_2` to be strictly two lines long.
    *   Ensure "Check off list" icon has ticks for both lines.
    *   Ensure "Toggle list" icon looks like `>--` (chevron followed by line) for both lines.

### [Component] Rich Text Editor (`app`)

#### [MODIFY] [JuneRichEditor.kt](file:///C:/Users/trai/Documents/GitHub/June (fixed)/app/src/main/java/com/denser/june/presentation/screens/editor/components/JuneRichEditor.kt)
*   **Detection**: Update `findToggleHeaders` to look for lines starting with `>> `.
*   **Notion-like Hiding**: Update `calculateHiddenRanges` to identify child lines by their indentation. If a line following a collapsed `>> ` header starts with whitespace, hide it.
*   **Chevron Logic**: Adjust chevron placement to look for `>> `.
*   **Formatting**: Ensure the `>> ` prefix itself is styled or handled visually (e.g., dimmed or replaced by the chevron).

### [Component] Resources (`core`)

#### [MODIFY] [ic_list_bulleted_2.xml](file:///C:/Users/trai/Documents/GitHub/June (fixed)/core/src/main/res/drawable/ic_list_bulleted_2.xml)
#### [MODIFY] [ic_list_numbered_2.xml](file:///C:/Users/trai/Documents/GitHub/June (fixed)/core/src/main/res/drawable/ic_list_numbered_2.xml)
#### [MODIFY] [ic_list_check_2.xml](file:///C:/Users/trai/Documents/GitHub/June (fixed)/core/src/main/res/drawable/ic_list_check_2.xml)
#### [MODIFY] [ic_list_toggle_2.xml](file:///C:/Users/trai/Documents/GitHub/June (fixed)/core/src/main/res/drawable/ic_list_toggle_2.xml)

## Verification Plan

### Manual Verification
1.  Open the lists menu and select **Toggle List**.
2.  Type `>> Header` and press Enter.
3.  Type indented content: `  Child 1`, `  Child 2`.
4.  Collapse the header. Verify both child lines disappear.
5.  Type a non-indented line below: `Normal line`. Verify it stays visible when the toggle is collapsed.
6.  Verify the toolbar icons match the "two lines" requirement and specific tick/chevron styles.
