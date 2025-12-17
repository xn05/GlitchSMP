package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class FreezeGlitchAbility implements AbilityAttributes, Listener {
    private static final long DURATION_READY_MILLIS = 15_000L;
    private static final long DURATION_FROZEN_MILLIS = 4_000L;
    private static final long COOLDOWN_SECONDS = 90L;

    private final Plugin plugin = Registry.get().getPlugin();
    private final Map<UUID, Long> readyToFreeze = new HashMap<>();
    private final Map<UUID, BossBar> activeBossBars = new HashMap<>();
    private static final Map<UUID, Long> frozen = new HashMap<>();
    private static final Map<UUID, Location> frozenPositions = new HashMap<>();
    private static final Map<UUID, Location> iceBlocks = new HashMap<>();

    public FreezeGlitchAbility() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        // Global task for handling frozen players
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Handle frozen players
            for (UUID uuid : new HashSet<>(frozen.keySet())) {
                Long expiry = frozen.get(uuid);
                if (expiry != null && expiry < System.currentTimeMillis()) {
                    // unfreeze
                    Location iceLoc = iceBlocks.get(uuid);
                    if (iceLoc != null) {
                        iceLoc.getBlock().setType(Material.AIR);
                        iceBlocks.remove(uuid);
                    }
                    frozenPositions.remove(uuid);
                    frozen.remove(uuid);
                } else if (expiry != null) {
                    // teleport back
                    Player frozenPlayer = Bukkit.getPlayer(uuid);
                    if (frozenPlayer != null && frozenPlayer.isOnline()) {
                        Location pos = frozenPositions.get(uuid);
                        if (pos != null) {
                            frozenPlayer.teleport(pos);
                        }
                    }
                }
            }
        }, 0L, 1L);
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
        // Create bossbar
        BossBar bossBar = Bukkit.createBossBar(ChatColor.AQUA + "Freeze Glitch Active - Hit to Freeze!", BarColor.BLUE, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);
        activeBossBars.put(player.getUniqueId(), bossBar);
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
        // Remove bossbar
        BossBar bar = activeBossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
        long freezeExpiry = System.currentTimeMillis() + DURATION_FROZEN_MILLIS;
        frozen.put(target.getUniqueId(), freezeExpiry);
        frozenPositions.put(target.getUniqueId(), target.getLocation());
        Location iceLoc = target.getLocation().clone().subtract(0, 0, 0);
        iceBlocks.put(target.getUniqueId(), iceLoc);
        target.getWorld().getBlockAt(iceLoc).setType(Material.ICE);
        target.sendMessage(ChatColor.RED + "You have been frozen for 4 seconds!");
        player.sendMessage(ChatColor.GREEN + "Target frozen!");
        return TriggerResult.none();
    }

    @Override
    public TriggerResult onTick(Player player, long tick) {
        // Update bossbar progress and cleanup expired readyToFreeze
        for (Map.Entry<UUID, Long> entry : new HashSet<>(readyToFreeze.entrySet())) {
            UUID uuid = entry.getKey();
            Long expiry = entry.getValue();
            if (expiry < System.currentTimeMillis()) {
                // remove bar
                BossBar bar = activeBossBars.remove(uuid);
                if (bar != null) {
                    bar.removeAll();
                }
                readyToFreeze.remove(uuid);
            } else {
                // update progress
                long remaining = expiry - System.currentTimeMillis();
                double progress = remaining / (double) DURATION_READY_MILLIS;
                BossBar bar = activeBossBars.get(uuid);
                if (bar != null) {
                    bar.setProgress(progress);
                }
            }
        }
        return TriggerResult.none();
    }

    @Override
    public TriggerResult onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return TriggerResult.none();
        }
        if (isFrozen(player)) {
            event.setCancelled(true);
        }
        return TriggerResult.none();
    }

    @EventHandler
    public void handlePlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleEntityDamage(EntityDamageByEntityEvent event) {
        onEntityHit(event);
    }

    @Override
    public TriggerResult onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            event.setCancelled(true);
        }
        return TriggerResult.none();
    }

    private boolean isFrozen(Player player) {
        Long expiry = frozen.get(player.getUniqueId());
        return expiry != null && expiry >= System.currentTimeMillis();
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

    @Override
    public void reset() {
        // Clear all maps and remove bossbars
        readyToFreeze.clear();
        frozen.clear();
        frozenPositions.clear();
        iceBlocks.clear();
        for (BossBar bar : activeBossBars.values()) {
            bar.removeAll();
        }
        activeBossBars.clear();
    }
}
