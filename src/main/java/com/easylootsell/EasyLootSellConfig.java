package com.easylootsell;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import static com.easylootsell.EasyLootSellConfig.GROUP;

@ConfigGroup(GROUP)
public interface EasyLootSellConfig extends Config {
    String GROUP = "easylootsell";

    @ConfigItem(
            keyName = "lootTabName",
            name = "Loot Tab Name",
            description = "Name of the desired loot tab, only include the tab name and not the price estimate (ex: Tab 9), or blank for all items",
            position = 0)
    default String lootTabName() {
        return "Tab 9";
    }

    @ConfigSection(
            name = "Highlighting",
            description = "Set which bank tab is the loot tab, and choose a highlight color",
            position = 10
    )
    String SECTION_LOOT_TAB = "lootTabSection";

    @ConfigItem(
            keyName = "highlightColor",
            name = "Highlight Color",
            description = "The color items within the loot tab will be highlighted for easier viewing",
            position = 11,
            section = SECTION_LOOT_TAB
    )
    default DesiredHighlightColor desiredHighlightColor() {
        return DesiredHighlightColor.GREEN;
    }

    @ConfigSection(
            name = "Rearranging",
            description = "Optionally hide placeholders and untradables, or move untradables to last, to make grabbing loot even easier",
            position = 20
    )
    String SECTION_ITEM_HIDING = "itemRearrangingSection";

    String KEY_HIDE_PLACEHOLDERS = "hidePlaceholders";
    @ConfigItem(
            keyName = KEY_HIDE_PLACEHOLDERS,
            name = "Hide Placeholders",
            description = "Hide placeholders and shift all item stacks up and left to close the gaps between them for easier loot grabbing",
            position = 21,
            section = SECTION_ITEM_HIDING
    )
    default boolean hidePlaceholders() {
        return false;
    }

    String KEY_HIDE_UNTRADABLES = "hideUntradables";
    @ConfigItem(
            keyName = KEY_HIDE_UNTRADABLES,
            name = "Hide Untradables",
            description = "Hide untradables and shift items up and left to close the gaps between them for easier loot grabbing",
            position = 22,
            section = SECTION_ITEM_HIDING
    )
    default boolean hideUntradables() {
        return false;
    }

    String KEY_MOVE_UNTRADABLES_TO_LAST = "moveUntradablesToLast";
    @ConfigItem(
            keyName = KEY_MOVE_UNTRADABLES_TO_LAST,
            name = "Move Untradables to last",
            description = "Move untradables to the bottom, below all other item stacks and placeholders (if untradables are not hidden)",
            position = 23,
            section = SECTION_ITEM_HIDING
    )
    default boolean moveUntradablesToLast() {
        return false;
    }
}
