
package com.easylootsell;


import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;


import javax.inject.Inject;

@PluginDescriptor(
        name = "Easy Loot Sell",
        description = "Highlights items with more than 0 qty for easy identification while selling off a loot tab",
        tags = {"loot tab", "sell", "highlight", "loot", "prices", "deposit", "easy"}
)
public class EasyLootSellPlugin extends Plugin {
    static final String CONFIG_GROUP = "easylootsell";

    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    @Inject
    private EasyLootSellConfig config;

    @Inject
    private MenuManager menuManager;

    @Inject
    private EasyLootSellOverlay overlay;

    @Inject
    private OverlayManager overlayManager;

    @Provides
    EasyLootSellConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(EasyLootSellConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);

    }
}
