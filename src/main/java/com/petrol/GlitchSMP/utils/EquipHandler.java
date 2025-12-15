package com.petrol.GlitchSMP.utils;

import com.petrol.GlitchSMP.Registry;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class EquipHandler implements Listener {
    private final Registry registry;
    private final AbilityHandler abilityHandler;

    public EquipHandler(Registry registry, AbilityHandler abilityHandler) {
        this.registry = registry;
        this.abilityHandler = abilityHandler;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        getGlitchFromItem(item).ifPresent(glitchItem -> {
            Player player = event.getPlayer();
            abilityHandler.equip(player, glitchItem.getId());
            player.sendMessage("Equipped " + glitchItem.getAbility().getDisplayName());
            event.setCancelled(true);
        });
    }

    private Optional<GlitchItem<?>> getGlitchFromItem(ItemStack item) {
        for (GlitchItem<? extends AbilityAttributes> glitchItem : registry.getAllItems()) {
            NamespacedKey key = glitchItem.getKey();
            if (item.hasItemMeta() && item.getItemMeta().getLocalizedName().equalsIgnoreCase(key.getKey())) {
                return Optional.of(glitchItem);
            }
        }
        return Optional.empty();
    }
}
