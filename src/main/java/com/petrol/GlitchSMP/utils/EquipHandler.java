package com.petrol.GlitchSMP.utils;

import com.petrol.GlitchSMP.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;
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
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        findGlitchId(item).ifPresent(glitchId -> {
            Player player = event.getPlayer();
            AbilityHandler.Slot slot = determineSlot(player);
            if (slot == null) {
                player.sendMessage("Both slots are full!");
                return;
            }
            registry.getAbility(glitchId).ifPresent(ability -> {
                // Remove the item from hand
                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() <= 0) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    player.getInventory().setItemInMainHand(item);
                }
                abilityHandler.equip(player, slot, glitchId);
                player.sendMessage("Equipped " + ability.getDisplayName() + " in " + (slot == AbilityHandler.Slot.PRIMARY ? "slot1" : "slot2"));
                event.setCancelled(true);
            });
        });
    }

    private AbilityHandler.Slot determineSlot(Player player) {
        boolean primaryEmpty = abilityHandler.getEquipped(player, AbilityHandler.Slot.PRIMARY).isEmpty();
        boolean secondaryEmpty = abilityHandler.getEquipped(player, AbilityHandler.Slot.SECONDARY).isEmpty();
        if (primaryEmpty) {
            return AbilityHandler.Slot.PRIMARY;
        } else if (secondaryEmpty) {
            return AbilityHandler.Slot.SECONDARY;
        } else {
            return null;
        }
    }

    private Optional<String> findGlitchId(ItemStack item) {
        return registry.getAllItems().stream()
                .filter(attrs -> item.getItemMeta().getPersistentDataContainer().has(attrs.getKey(), PersistentDataType.STRING))
                .map(ItemAttributes::getId)
                .findFirst();
    }
}
