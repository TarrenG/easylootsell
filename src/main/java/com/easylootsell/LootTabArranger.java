package com.easylootsell;

import lombok.NonNull;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.bank.BankSearch;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.easylootsell.EasyLootSellConfig.KEY_HIDE_PLACEHOLDERS;
import static com.easylootsell.EasyLootSellConfig.KEY_HIDE_UNTRADABLES;

@Singleton
public class LootTabArranger {
    private static final int BANK_ITEM_WIDTH = 36;

    @Inject
    private Client client;

    @Inject
    private EasyLootSellConfig config;

    @Inject
    private ClientThread clientThread;

    @Inject
    private BankSearch bankSearch;

    private Map<Integer, Integer> itemIdToIndex = null;

    public void shutDown() {
        itemIdToIndex = null;
        clientThread.invokeLater(bankSearch::layoutBank);
    }

    @Subscribe
    public void onConfigChanged(final ConfigChanged event) {
        if (!EasyLootSellConfig.GROUP.equals(event.getGroup()))
            return;

        final String key = event.getKey();
        if (KEY_HIDE_PLACEHOLDERS.equals(key) || KEY_HIDE_UNTRADABLES.equals(key)) {
            itemIdToIndex = null;
            if (config.hidePlaceholders() || config.hideUntradables()) {
                clientThread.invokeLater(() -> {
                    // reset to un-hide items in cases where one of our hide settings was just disabled
                    bankSearch.layoutBank();
                    arrangeLootTabIfApplicableClientThreadAssumed();
                });
            } else {
                clientThread.invokeLater(bankSearch::layoutBank);
            }
        }
    }

    @Subscribe
    public void onScriptPostFired(final ScriptPostFired event) {
        if (event.getScriptId() != ScriptID.BANKMAIN_BUILD)
            return;
        arrangeLootTabIfApplicableClientThreadAssumed();
    }

    public void arrangeLootTabIfApplicable() {
        clientThread.invokeLater(this::arrangeLootTabIfApplicableClientThreadAssumed);
    }

    private void arrangeLootTabIfApplicableClientThreadAssumed() {
        if (!config.hidePlaceholders() && !config.hideUntradables())
            return;

        final Optional<Widget> bankWidgetIfOnLootTab = WidgetUtils.getBankWidgetIfOnLootTab(client, config);
        if (bankWidgetIfOnLootTab.isEmpty()) {
            itemIdToIndex = null;
            return;
        }

        final Widget bankWidget = bankWidgetIfOnLootTab.get();
        if (itemIdToIndex == null)
            itemIdToIndex = assignIndices(bankWidget);

        for (final Widget itemWidget : bankWidget.getDynamicChildren()) {
            if (itemWidget.isHidden())
                continue;

            final Integer assignedIndex = itemIdToIndex.get(itemWidget.getItemId());
            if (assignedIndex == null) {
                itemWidget.setHidden(true);
                itemWidget.revalidate();
            } else {
                itemWidget.setOriginalX(getXForIndex(assignedIndex));
                itemWidget.setOriginalY(getYForIndex(assignedIndex));
                itemWidget.revalidate();
            }
        }
    }

    @NonNull
    private Map<Integer, Integer> assignIndices(final Widget bankWidget) {
        final Map<Integer, Integer> map = new HashMap<>();

        int index = 0;
        for (final Widget itemWidget : bankWidget.getDynamicChildren()) {
            if (itemWidget.isHidden())
                continue;

            if (!shouldHide(itemWidget)) {
                map.put(itemWidget.getItemId(), index++);
            }
        }

        return map;
    }

    private boolean shouldHide(final Widget bankItem) {
        final int quantity = bankItem.getItemQuantity();

        if (config.hidePlaceholders() && quantity < 1)
            return true;

        if (config.hideUntradables() && quantity > 0) {
            return !client.getItemDefinition(bankItem.getItemId()).isTradeable();
        }

        return false;
    }

    private static int getXForIndex(final int index) {
        return (index % 8) * 48 + 51;
    }

    private static int getYForIndex(final int index) {
        return (index / 8) * BANK_ITEM_WIDTH;
    }
}
