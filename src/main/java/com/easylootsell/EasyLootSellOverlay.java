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
    public EasyLootSellOverlay(Client client, ItemManager itemManager, EasyLootSellPlugin plugin, EasyLootSellConfig config)
    {
        this.client = client;
        this.itemManager = itemManager;
        this.config = config;

        showOnInventory();
        showOnBank();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
    {
        if (checkInterfaceIsHighlightable()) {
            int qty = itemWidget.getQuantity();
            /* highlight item if we have at least 1 in the loot tab for easy identification when selling */
            if (qty >= 1)
            {

                Color colorToUse =  getDesiredColor(config.desiredHighlightColor());
                Rectangle bounds = itemWidget.getCanvasBounds();
                final BufferedImage outline = itemManager.getItemOutline(itemId, itemWidget.getQuantity(), colorToUse);
                graphics.drawImage(outline, (int) bounds.getX(), (int) bounds.getY(), null);
            }
        }
    }

    private boolean checkInterfaceIsHighlightable()
    {
            Widget bankWidget = client.getWidget(ComponentID.BANK_ITEM_CONTAINER);
            if (bankWidget != null)
            {
                String bankTitle = client.getWidget(ComponentID.BANK_TITLE_BAR).getText();
                /* if the item is within the desired tab, it should be highlighted */

                if (bankTitle.contains(config.lootTabName())) {
                    return true;
                }
                return false;
            }
        return true;
    }

    private Color getDesiredColor(EasyLootSellConfig.desiredHighlightColor value) {
        switch (value.toString()){
            case "PINK":
                return Color.PINK;
            case "BLUE":
                return Color.BLUE;
            case "YELLOW":
                return Color.YELLOW;
            default:
                return Color.GREEN;
        }
    }

}
