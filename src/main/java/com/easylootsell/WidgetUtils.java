package com.easylootsell;

import net.runelite.api.Client;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;

import java.util.Optional;

public final class WidgetUtils {
    private WidgetUtils() {}

    public static Optional<Widget> getBankWidgetIfOnLootTab(final Client client, final EasyLootSellConfig config) {
        final Widget bankWidget = client.getWidget(ComponentID.BANK_ITEM_CONTAINER);
        if (bankWidget == null)
            return Optional.empty();

        final Widget bankTitleBarWidget = client.getWidget(ComponentID.BANK_TITLE_BAR);
        if (bankTitleBarWidget == null)
            return Optional.empty();

        final String bankTitle = bankTitleBarWidget.getText();
        if (bankTitle == null || !bankTitle.contains(config.lootTabName()))
            return Optional.empty();

        return Optional.of(bankWidget);
    }
}
