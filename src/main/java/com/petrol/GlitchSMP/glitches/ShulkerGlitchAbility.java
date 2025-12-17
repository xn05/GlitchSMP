package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShulkerGlitchAbility implements AbilityAttributes, Listener {
    private static final long COOLDOWN_SECONDS = 240L; // 4 minutes
    private static final String GUI_TITLE = ChatColor.DARK_PURPLE + "Shulker Glitch";

    private final Plugin plugin = Registry.get().getPlugin();
    private final Set<UUID> activePlayers = new HashSet<>();

    public ShulkerGlitchAbility() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getId() {
        return "shulker";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.DARK_PURPLE + "Shulker Glitch";
    }

    @Override
    public String getGlyph() {
        return "Ⅳ";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    @Override
    public TriggerResult onControlActivation(Player player, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        Inventory gui = createGui();

        player.openInventory(gui);
        activePlayers.add(player.getUniqueId());
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onOffhandSwap(Player player, AbilityHandler.Slot slot) {
        return onControlActivation(player, slot, ControlHandler.ActivationAction.OFFHAND);
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }
        Material mat = item.getType();
        if (mat == Material.CRAFTING_TABLE) {
            player.openWorkbench(null, true);
        } else if (mat == Material.ENDER_CHEST) {
            player.openInventory(player.getEnderChest());
        } else if (mat == Material.ANVIL) {
            player.openAnvil(null, true);
        }
    }

    @EventHandler
    public void handleInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (!activePlayers.contains(uuid)) {
            return;
        }
        String title = event.getView().getTitle();
        if (title.equals(GUI_TITLE)) {
            // Exiting the main menu
            activePlayers.remove(uuid);
        } else {
            // Exiting a sub-inventory, reopen the GUI
            Inventory gui = createGui();
            player.openInventory(gui);
        }
    }

    private Inventory createGui() {
        Inventory gui = Bukkit.createInventory(null, 9, GUI_TITLE);

        // Crafting Table
        ItemStack crafting = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta meta = crafting.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Crafting Table");
            crafting.setItemMeta(meta);
        }
        gui.setItem(0, crafting);

        // Ender Chest
        ItemStack echest = new ItemStack(Material.ENDER_CHEST);
        meta = echest.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_AQUA + "Ender Chest");
            echest.setItemMeta(meta);
        }
        gui.setItem(4, echest);

        // Anvil
        ItemStack anvil = new ItemStack(Material.ANVIL);
        meta = anvil.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Anvil");
            anvil.setItemMeta(meta);
        }
        gui.setItem(8, anvil);

        return gui;
    }
}
