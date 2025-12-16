package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FreezeGlitchAbility implements AbilityAttributes, Listener {
    private static final long DURATION_READY_MILLIS = 15_000L;
    private static final long DURATION_FROZEN_MILLIS = 4_000L;
    private static final long COOLDOWN_SECONDS = 90L;

    private final Plugin plugin = Registry.get().getPlugin();
    private final Map<UUID, Long> readyToFreeze = new HashMap<>();
    private static final Map<UUID, Long> frozen = new HashMap<>();

    public FreezeGlitchAbility() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getId() {
        return "freeze";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.BLUE + "Freeze Glitch";
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    @Override
    public TriggerResult onControlActivation(PlayerInteractEvent event, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        Player player = event.getPlayer();
        long expiry = System.currentTimeMillis() + DURATION_READY_MILLIS;
        readyToFreeze.put(player.getUniqueId(), expiry);
        player.sendMessage(ChatColor.AQUA + "Freeze Glitch activated! Your next hit within 15 seconds will freeze the target.");
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onEntityHit(EntityDamageByEntityEvent event) {
        Player player = resolvePlayer(event);
        if (player == null) {
            return TriggerResult.none();
        }
        if (!(event.getEntity() instanceof Player target)) {
            return TriggerResult.none();
        }
        Long readyExpiry = readyToFreeze.get(player.getUniqueId());
        if (readyExpiry == null || readyExpiry < System.currentTimeMillis()) {
            return TriggerResult.none();
        }
        readyToFreeze.remove(player.getUniqueId());
        long freezeExpiry = System.currentTimeMillis() + DURATION_FROZEN_MILLIS;
        frozen.put(target.getUniqueId(), freezeExpiry);
        target.sendMessage(ChatColor.RED + "You have been frozen for 4 seconds!");
        player.sendMessage(ChatColor.GREEN + "Target frozen!");
        return TriggerResult.none();
    }

    @Override
    public TriggerResult onTick(Player player, long tick) {
        // Cleanup expired entries
        readyToFreeze.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());
        frozen.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());
        return TriggerResult.none();
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handlePlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handlePlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    private boolean isFrozen(Player player) {
        Long expiry = frozen.get(player.getUniqueId());
        if (expiry == null || expiry < System.currentTimeMillis()) {
            frozen.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    private Player resolvePlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }
        if (event.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                return shooter;
            }
        }
        return null;
    }
}
