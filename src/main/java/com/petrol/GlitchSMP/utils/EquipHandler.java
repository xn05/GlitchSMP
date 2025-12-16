package com.petrol.GlitchSMP.utils;

import com.petrol.GlitchSMP.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

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
            AbilityHandler.Slot slot = player.isSneaking() ? AbilityHandler.Slot.SECONDARY : AbilityHandler.Slot.PRIMARY;
            registry.getAbility(glitchId).ifPresent(ability -> {
                registry.getItem(glitchId).ifPresent(attrs -> {
                    ItemStack asStack = attrs.createItemStack();
                    if (!player.getInventory().containsAtLeast(asStack, 1)) {
                        return;
                    }
                    player.getInventory().removeItem(asStack);
                });
                abilityHandler.equip(player, slot, glitchId);
                player.sendMessage("Equipped " + ability.getDisplayName() + " in " + (slot == AbilityHandler.Slot.PRIMARY ? "slot1" : "slot2"));
                event.setCancelled(true);
            });
        });
    }

    private Optional<String> findGlitchId(ItemStack item) {
        String localized = item.getItemMeta().getLocalizedName();
        if (localized == null || localized.isEmpty()) {
            return Optional.empty();
        }
        String normalized = localized.toLowerCase(Locale.ROOT);
        return registry.getAllItems().stream()
                .filter(attrs -> attrs.getKey().getKey().equalsIgnoreCase(normalized))
                .map(ItemAttributes::getId)
                .findFirst();
    }
}
