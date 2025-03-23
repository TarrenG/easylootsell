package com.easylootsell;

import lombok.NonNull;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.bank.BankSearch;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.easylootsell.EasyLootSellConfig.KEY_HIDE_PLACEHOLDERS;
import static com.easylootsell.EasyLootSellConfig.KEY_HIDE_UNTRADABLES;
import static com.easylootsell.EasyLootSellConfig.KEY_MOVE_UNTRADABLES_TO_LAST;
import static net.runelite.client.plugins.banktags.BankTagsPlugin.BANK_ITEMS_PER_ROW;
import static net.runelite.client.plugins.banktags.BankTagsPlugin.BANK_ITEM_HEIGHT;
import static net.runelite.client.plugins.banktags.BankTagsPlugin.BANK_ITEM_START_X;
import static net.runelite.client.plugins.banktags.BankTagsPlugin.BANK_ITEM_START_Y;
import static net.runelite.client.plugins.banktags.BankTagsPlugin.BANK_ITEM_WIDTH;
import static net.runelite.client.plugins.banktags.BankTagsPlugin.BANK_ITEM_X_PADDING;
import static net.runelite.client.plugins.banktags.BankTagsPlugin.BANK_ITEM_Y_PADDING;

@Singleton
public class LootTabArranger {
    @Inject
    private Client client;

    @Inject
    private EasyLootSellConfig config;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ItemManager itemManager;

    @Inject
    private BankSearch bankSearch;

    private Map<Integer, Integer> itemIdToIndex = null;
    private Item[] previousInventory = new Item[0];
    private int previousScrollHeight = Integer.MAX_VALUE;

    public void shutDown() {
        itemIdToIndex = null;
        clientThread.invokeLater(bankSearch::layoutBank);
    }

    @Subscribe
    public void onConfigChanged(final ConfigChanged event) {
        if (!EasyLootSellConfig.GROUP.equals(event.getGroup()))
            return;

        final String key = event.getKey();
        if (KEY_HIDE_PLACEHOLDERS.equals(key) || KEY_HIDE_UNTRADABLES.equals(key) || KEY_MOVE_UNTRADABLES_TO_LAST.equals(key)) {
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

    @Subscribe(priority = -2f) // "Bank Tags" and "Bank Tag Layouts" plugins also set the scroll bar height; run after them
    public void onScriptPreFired(ScriptPreFired event) {
        if (event.getScriptId() != ScriptID.BANKMAIN_FINISHBUILDING)
            return;
        setScrollBarIfApplicableOnBankBuild();
    }

    @Subscribe
    public void onScriptPostFired(final ScriptPostFired event) {
        if (event.getScriptId() != ScriptID.BANKMAIN_BUILD)
            return;
        arrangeLootTabIfApplicableClientThreadAssumed();
    }

    @Subscribe
    public void onItemContainerChanged(final ItemContainerChanged itemContainerChanged) {
        if (InventoryID.INVENTORY.getId() != itemContainerChanged.getContainerId())
            return;

        final Item[] inventory = itemContainerChanged.getItemContainer().getItems();
        // Recalc positions on deposits, but not withdrawals (the method '..IfApplicable' will cancel if it wasn't a bank interaction)
        final Set<Item> depositedItems = findDepositedItems(inventory, previousInventory);
        if (!depositedItems.isEmpty())
            arrangeLootTabIfApplicable(depositedItems);

        previousInventory = inventory;
    }

    private Set<Item> findDepositedItems(final Item[] inventory, final Item[] previousInventory) {
        return Arrays.stream(previousInventory)
                .filter(item -> item.getQuantity() > 0)
                .filter(item -> quantityOfItem(inventory, item.getId()) < item.getQuantity())
                .collect(Collectors.toSet());
    }

    private int quantityOfItem(final Item[] inventory, final int itemId) {
        for (Item invItem : inventory) {
            if (invItem.getId() == itemId)
                return invItem.getQuantity();
        }
        return 0;
    }

    private void setScrollBarIfApplicableOnBankBuild() {
        if (itemIdToIndex == null)
            return;

        if (WidgetUtils.getBankWidgetIfOnLootTab(client, config).isEmpty())
            return;

        final int scrollHeight = getYForIndex(findMaxIndex()) + BANK_ITEM_HEIGHT;

        // Below technique taken from bank tag layouts
        // This is prior to bankmain_finishbuilding running, so the arguments are still on the stack. Overwrite
        // argument int12 (7 from the end) which is the height passed to if_setscrollsize
        client.getIntStack()[client.getIntStackSize() - 7] = scrollHeight;
    }

    private int findMaxIndex() {
        return itemIdToIndex.values().stream()
                .max(Integer::compareTo)
                .orElse(0);
    }

    public void arrangeLootTabIfApplicable() {
        arrangeLootTabIfApplicable(Set.of());
    }

    public void arrangeLootTabIfApplicable(final Set<Item> depositedItems) {
        clientThread.invokeLater(() -> arrangeLootTabIfApplicableClientThreadAssumed(depositedItems));
    }

    private void arrangeLootTabIfApplicableClientThreadAssumed() {
        arrangeLootTabIfApplicableClientThreadAssumed(Set.of());
    }

    private void arrangeLootTabIfApplicableClientThreadAssumed(final Set<Item> depositedItems) {
        if (!config.hidePlaceholders() && !config.hideUntradables())
            return;

        final Optional<Widget> bankWidgetIfOnLootTab = WidgetUtils.getBankWidgetIfOnLootTab(client, config);
        if (bankWidgetIfOnLootTab.isEmpty()) {
            itemIdToIndex = null;
            return;
        }

        final Widget bankWidget = bankWidgetIfOnLootTab.get();
        if (itemIdToIndex == null || !depositedItems.isEmpty())
            itemIdToIndex = assignIndices(bankWidget, depositedItems);

        for (final Widget itemWidget : bankWidget.getDynamicChildren()) {
            final Integer assignedIndex = itemIdToIndex.get(itemWidget.getItemId());
            if (assignedIndex == null) {
                itemWidget.setHidden(true);
                itemWidget.revalidate();
            } else {
                itemWidget.setHidden(false);
                itemWidget.setOriginalX(getXForIndex(assignedIndex));
                itemWidget.setOriginalY(getYForIndex(assignedIndex));
                itemWidget.revalidate();
            }
        }

        previousScrollHeight = setScrollBarForRearrange(bankWidget);
    }

    private int setScrollBarForRearrange(final Widget bankWidget) {
        final int scrollHeight = getYForIndex(findMaxIndex()) + BANK_ITEM_HEIGHT;

        if (scrollHeight == previousScrollHeight || previousScrollHeight == Integer.MAX_VALUE)
            return scrollHeight;

        bankWidget.setScrollHeight(scrollHeight);
        clientThread.invokeLater(() ->
                client.runScript(ScriptID.UPDATE_SCROLLBAR, ComponentID.BANK_SCROLLBAR, ComponentID.BANK_ITEM_CONTAINER, bankWidget.getScrollY())
        );

         return scrollHeight;
    }

    @NonNull
    private Map<Integer, Integer> assignIndices(final Widget bankWidget, final Set<Item> depositedItems) {
        final Map<Integer, Integer> map = new HashMap<>();
        final List<Integer> untradables = new ArrayList<>();

        int index = 0;
        for (final Widget itemWidget : bankWidget.getDynamicChildren()) {
            final boolean isDepositedItem = depositedItems.stream().anyMatch(deposited -> itemManager.canonicalize(deposited.getId()) == itemWidget.getItemId());
            if (!isDepositedItem && (itemWidget.isHidden() || shouldHide(itemWidget)))
                continue;

            final int itemId = itemWidget.getItemId();
            final boolean tradable = client.getItemDefinition(itemId).isTradeable();
            if (tradable || (!config.hideUntradables() && !config.moveUntradablesToLast())) {
                map.put(itemId, index++);
            } else {
                untradables.add(itemId);
            }
        }

        if (!config.hideUntradables() && config.moveUntradablesToLast()) {
            index += 2 * BANK_ITEMS_PER_ROW - 1;
            index -= index % BANK_ITEMS_PER_ROW;
            for (final int itemId : untradables) {
                map.put(itemId, index++);
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
        return (index % BANK_ITEMS_PER_ROW) * (BANK_ITEM_WIDTH + BANK_ITEM_X_PADDING) + BANK_ITEM_START_X;
    }

    private static int getYForIndex(final int index) {
        return (index / BANK_ITEMS_PER_ROW) * (BANK_ITEM_HEIGHT + BANK_ITEM_Y_PADDING) + BANK_ITEM_START_Y;
    }
}
