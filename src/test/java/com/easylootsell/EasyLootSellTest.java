package com.easylootsell;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class EasyLootSellTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(EasyLootSellPlugin.class);
        RuneLite.main(args);
    }
}
