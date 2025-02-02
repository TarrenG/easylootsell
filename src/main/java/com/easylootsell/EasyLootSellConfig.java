package com.easylootsell;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import static com.easylootsell.EasyLootSellConfig.GROUP;

@ConfigGroup(GROUP)
public interface EasyLootSellConfig extends Config {
    String GROUP = "easylootsell";

    @ConfigSection(
            name = "Loot Tab Highlighting",
            description = "Set which bank tab is the loot tab, and choose a highlight color",
            position = 0
    )
    String SECTION_LOOT_TAB = "lootTabSection";

    @ConfigItem(
            keyName = "lootTabName",
            name = "Loot Tab Name",
            description = "Name of the desired loot tab, only include the tab name and not the price estimate (ex: Tab 9), or blank for all items",
            position = 1,
            section = SECTION_LOOT_TAB)
    default String lootTabName() {
        return "Tab 9";
    }

    @ConfigItem(
            keyName = "highlightColor",
            name = "Highlight Color",
            description = "The color items within the loot tab will be highlighted for easier viewing",
            position = 2,
            section = SECTION_LOOT_TAB
    )
    default DesiredHighlightColor desiredHighlightColor() {
        return DesiredHighlightColor.GREEN;
    }
}
