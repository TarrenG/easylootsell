package com.easylootsell;

import net.runelite.api.Client;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class EasyLootSellOverlay extends WidgetItemOverlay {

    private final Client client;
    private final ItemManager itemManager;
    private final EasyLootSellConfig config;

    @Inject
    public EasyLootSellOverlay(final Client client, final ItemManager itemManager, final EasyLootSellConfig config) {
        this.client = client;
        this.itemManager = itemManager;
        this.config = config;

        showOnInventory();
        showOnBank();
    }

    @Override
    public void renderItemOverlay(final Graphics2D graphics, final int itemId, final WidgetItem itemWidget) {
        if (!interfaceIsHighlightable(itemWidget))
            return;

        final int qty = itemWidget.getQuantity();
        if (qty < 1)
            return;

        // highlight item if we have at least 1 in the loot tab for easy identification when selling
        final Color colorToUse = config.desiredHighlightColor().toJavaColor();
        final Rectangle bounds = itemWidget.getCanvasBounds();
        final BufferedImage outline = itemManager.getItemOutline(itemId, itemWidget.getQuantity(), colorToUse);
        graphics.drawImage(outline, bounds.x, bounds.y, null);
    }

    private boolean interfaceIsHighlightable(final WidgetItem itemWidget) {
        final Widget bankWidget = client.getWidget(ComponentID.BANK_ITEM_CONTAINER);
        if (bankWidget == null)
            return false;

        final Widget bankTitleBarWidget = client.getWidget(ComponentID.BANK_TITLE_BAR);
        if (bankTitleBarWidget == null)
            return false;

        final String bankTitle = bankTitleBarWidget.getText();
        if (bankTitle == null || !bankTitle.contains(config.lootTabName()))
            return false;

        return bankWidget.getId() == itemWidget.getWidget().getId();
    }
}
