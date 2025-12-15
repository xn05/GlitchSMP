package com.petrol.GlitchSMP.utils;

import com.petrol.GlitchSMP.Registry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AbilityHandler implements Listener {
    private final com.petrol.GlitchSMP.GlitchSMP plugin;
    private final Registry registry;
    private final Map<UUID, String> equipped = new HashMap<>();
    private BukkitTask tickTask;

    public AbilityHandler(com.petrol.GlitchSMP.GlitchSMP plugin, Registry registry) {
        this.plugin = plugin;
        this.registry = registry;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void start() {
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long tick = plugin.getServer().getCurrentTick();
            for (Map.Entry<UUID, String> entry : equipped.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                Optional<AbilityAttributes> ability = registry.getAbility(entry.getValue());
                ability.ifPresent(attr -> {
                    if (player != null) {
                        attr.onTick(player, tick);
                    }
                });
            }
        }, 1L, 1L);
    }

    public void stop() {
        if (tickTask != null) {
            tickTask.cancel();
        }
    }

    public void equip(Player player, String glitchId) {
        equipped.put(player.getUniqueId(), glitchId);
        registry.getAbility(glitchId).ifPresent(attr -> attr.onEquip(player));
    }

    public void unequip(Player player) {
        String removed = equipped.remove(player.getUniqueId());
        if (removed != null) {
            registry.getAbility(removed).ifPresent(attr -> attr.onUnequip(player));
        }
    }

    public Optional<AbilityAttributes> getEquipped(Player player) {
        return Optional.ofNullable(equipped.get(player.getUniqueId())).flatMap(registry::getAbility);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking()) {
            return;
        }
        getEquipped(event.getPlayer()).ifPresent(attr -> {
            switch (event.getAction()) {
                case RIGHT_CLICK_AIR:
                case RIGHT_CLICK_BLOCK:
                    attr.onShiftRightClick(event);
                    break;
                case LEFT_CLICK_AIR:
                case LEFT_CLICK_BLOCK:
                    attr.onShiftLeftClick(event);
                    break;
                default:
                    break;
            }
        });
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            getEquipped(player).ifPresent(attr -> attr.onEntityHit(event));
        }
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            getEquipped(player).ifPresent(attr -> attr.onEntityDamaged(event));
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (!event.getPlayer().isSneaking()) {
            return;
        }
        getEquipped(event.getPlayer()).ifPresent(attr -> attr.onOffhandSwap(event));
    }
}
