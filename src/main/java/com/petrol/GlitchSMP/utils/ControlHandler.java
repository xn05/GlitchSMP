package com.petrol.GlitchSMP.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ControlHandler implements Listener {
    public enum ActivationAction {
        RIGHT,
        LEFT,
        SHIFT_RIGHT,
        SHIFT_LEFT,
        OFFHAND,
        MOVE,
        SNEAK;

        public static ActivationAction fromString(String raw) {
            if (raw == null || raw.isEmpty()) {
                return null;
            }
            try {
                return ActivationAction.valueOf(raw.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

        public static String displayList() {
            StringBuilder builder = new StringBuilder();
            ActivationAction[] values = values();
            for (int i = 0; i < values.length; i++) {
                builder.append(values[i].name().toLowerCase(Locale.ROOT));
                if (i < values.length - 1) {
                    builder.append(", ");
                }
            }
            return builder.toString();
        }
    }

    private final DataHandler dataHandler;
    private final Map<UUID, EnumMap<AbilityHandler.Slot, ActivationAction>> cache = new ConcurrentHashMap<>();

    public ControlHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public ActivationAction getActivation(UUID uuid, AbilityHandler.Slot slot) {
        EnumMap<AbilityHandler.Slot, ActivationAction> map = cache.computeIfAbsent(uuid, __ -> new EnumMap<>(AbilityHandler.Slot.class));
        ActivationAction action = map.get(slot);
        if (action == null) {
            action = dataHandler.getActivation(uuid, slot)
                    .map(ActivationAction::fromString)
                    .orElse(defaultAction(slot));
            map.put(slot, action);
        }
        return action;
    }

    public void setActivation(UUID uuid, AbilityHandler.Slot slot, ActivationAction action) {
        EnumMap<AbilityHandler.Slot, ActivationAction> map = cache.computeIfAbsent(uuid, __ -> new EnumMap<>(AbilityHandler.Slot.class));
        map.put(slot, action);
        dataHandler.setActivation(uuid, slot, action.name());
    }

    public boolean matches(Player player, AbilityHandler.Slot slot, ActivationAction triggeredAction) {
        if (triggeredAction == null) {
            return false;
        }
        return getActivation(player.getUniqueId(), slot) == triggeredAction;
    }

    public void hydrate(Player player) {
        for (AbilityHandler.Slot slot : AbilityHandler.Slot.values()) {
            getActivation(player.getUniqueId(), slot);
        }
    }

    public void clear(UUID uuid) {
        cache.remove(uuid);
    }

    private ActivationAction defaultAction(AbilityHandler.Slot slot) {
        return slot == AbilityHandler.Slot.PRIMARY ? ActivationAction.RIGHT : ActivationAction.SHIFT_RIGHT;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        hydrate(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clear(event.getPlayer().getUniqueId());
    }
}
