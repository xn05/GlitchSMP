package com.petrol.GlitchSMP;

import com.petrol.GlitchSMP.utils.CommandHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class GuiProtectionListener implements Listener {
    private final CommandHandler commandHandler;

    public GuiProtectionListener(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clicked = event.getInventory();
        if (clicked == null || !event.getView().getTitle().equals(CommandHandler.GUI_TITLE)) {
            return;
        }
        Object holder = clicked.getHolder();
        if (holder instanceof Player || holder == null) {
            event.setCancelled(true);
        }
    }
}
