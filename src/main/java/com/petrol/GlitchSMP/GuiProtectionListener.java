package com.petrol.GlitchSMP;

import com.petrol.GlitchSMP.utils.CommandHandler;
import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
        if (holder instanceof Player player) {
            ItemStack item = event.getCurrentItem();
            if (item != null && event.isShiftClick() && player.isOp()) {
                // Find the glitch item
                for (ItemAttributes attributes : Registry.get().getAllItems()) {
                    ItemStack glitchItem = attributes.createItemStack();
                    if (item.isSimilar(glitchItem)) {
                        // Give the item
                        if (player.getInventory().firstEmpty() != -1) {
                            player.getInventory().addItem(glitchItem);
                            player.sendMessage(ChatColor.GREEN + "Given " + attributes.getDisplayName());
                        } else {
                            player.sendMessage(ChatColor.RED + "Inventory full!");
                        }
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            event.setCancelled(true);
        }
    }
}
