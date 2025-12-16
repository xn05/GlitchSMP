package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DreamGlitchAbility implements AbilityAttributes {
    private static final long DURATION_MILLIS = 120_000L; // 2 minutes
    private static final long COOLDOWN_SECONDS = 300L; // 5 minutes

    private final Plugin plugin = Registry.get().getPlugin();
    private final Map<UUID, Long> activeUntil = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private static boolean listenerRegistered;

    public DreamGlitchAbility() {
        if (!listenerRegistered) {
            Bukkit.getPluginManager().registerEvents(new DreamDeathListener(), plugin);
            listenerRegistered = true;
        }
    }

    @Override
    public String getId() {
        return "dream";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.LIGHT_PURPLE + "Dream Glitch";
    }

    @Override
    public String getGlyph() {
        return "∛";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    private boolean isActive(Player player) {
        Long until = activeUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    @Override
    public TriggerResult onControlActivation(PlayerInteractEvent event, AbilityHandler.Slot slot,
                                             ControlHandler.ActivationAction action) {
        Player player = event.getPlayer();
        if (isActive(player)) {
            player.sendMessage(ChatColor.RED + "Dream Glitch is already active!");
            return TriggerResult.none();
        }
        long expiry = System.currentTimeMillis() + DURATION_MILLIS;
        activeUntil.put(player.getUniqueId(), expiry);
        showBossBar(player, expiry);
        player.sendMessage(ChatColor.AQUA + "Dream Glitch activated for 2 minutes!");
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onTick(Player player, long tick) {
        if (!isActive(player)) {
            clearActive(player.getUniqueId());
            return TriggerResult.none();
        }
        long remaining = activeUntil.get(player.getUniqueId()) - System.currentTimeMillis();
        updateBossBar(player, remaining);
        return TriggerResult.none();
    }

    @Override
    public void onUnequip(Player player) {
        clearActive(player.getUniqueId());
    }

    private void showBossBar(Player player, long expiry) {
        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), id -> {
            BossBar created = Bukkit.createBossBar(ChatColor.LIGHT_PURPLE + "Dream Glitch", BarColor.GREEN, BarStyle.SEGMENTED_10);
            created.addPlayer(player);
            return created;
        });
        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
        bar.setVisible(true);
    }

    private void updateBossBar(Player player, long remainingMillis) {
        BossBar bar = bossBars.get(player.getUniqueId());
        if (bar != null) {
            double progress = Math.max(0, remainingMillis / (double) DURATION_MILLIS);
            bar.setProgress(progress);
            long seconds = remainingMillis / 1000;
            bar.setTitle(ChatColor.LIGHT_PURPLE + "Dream Glitch (" + seconds + "s)");
        }
    }

    private void clearActive(UUID uuid) {
        activeUntil.remove(uuid);
        BossBar bar = bossBars.remove(uuid);
        if (bar != null) {
            bar.setVisible(false);
            bar.removeAll();
        }
    }

    private class DreamDeathListener implements Listener {
        @EventHandler
        public void onEntityDeath(EntityDeathEvent event) {
            if (event.getEntity().getKiller() == null) return;
            Player killer = (Player) event.getEntity().getKiller();
            if (!isActive(killer)) return;

            for (ItemStack drop : event.getDrops()) {
                if (drop.getType() == Material.SHULKER_SHELL) {
                    // do not multiply shulker shells
                } else {
                    drop.setAmount(drop.getAmount() * 20);
                }
            }
        }
    }
}
