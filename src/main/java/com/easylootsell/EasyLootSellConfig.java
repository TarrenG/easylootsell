package com.easylootsell;

import lombok.RequiredArgsConstructor;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(EasyLootSellPlugin.CONFIG_GROUP)
public interface EasyLootSellConfig extends Config {
    @ConfigSection(
            name = "Loot Tab Name",
            description = "Determines what bank tab is the loot tab, only include name, not estimated price (ex: Tab 9)",
            position = 0
    )
    String lootTabSection = "lootTabSection";

    @ConfigItem(
            keyName = "lootTabName",
            name = "Loot Tab Name",
            description = "Name of the desired loot tab, only include the tab name and not the price estimate (ex: Tab 9), or blank for all items",
            position = 1,
            section = "lootTabSection"
    )
    default String lootTabName() {return "Tab 9";}

    @RequiredArgsConstructor
    enum desiredHighlightColor {

        GREEN("GREEN"),
        BLUE("BLUE"),
        YELLOW("YELLOW"),
        PINK("PINK"),
        ;

        private final String value;

        @Override
        public String toString() {
            return value;
        }
    }

    @ConfigItem(
            keyName = "highlightColor",
            name = "Highlight Color",
            description = "The color items within the loot tab will be highlighted for easier viewing",
            position = 2,
            section = "lootTabSection"
    )
    default desiredHighlightColor desiredHighlightColor() {return desiredHighlightColor.GREEN;}
}
