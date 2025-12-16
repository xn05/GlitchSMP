package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.utils.AbilityAttributes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CrashGlitchAbility implements AbilityAttributes {
    private static final long WINDOW_MILLIS = 15_000L;
    private static final long COOLDOWN_MILLIS = 120_000L;

    private final Map<UUID, Long> activeWindows = new HashMap<>();

    @Override
    public String getId() {
        return "crash";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.LIGHT_PURPLE + "Crash Glitch";
    }

    @Override
    public String getGlyph() {
        return "\uE908";
    }

    @Override
    public long getBaseCooldownMillis() {
        return COOLDOWN_MILLIS;
    }

    public boolean isWindowActive(Player player) {
        Long expires = activeWindows.get(player.getUniqueId());
        return expires != null && expires > System.currentTimeMillis();
    }

    public long getWindowRemaining(Player player) {
        Long expires = activeWindows.get(player.getUniqueId());
        return expires == null ? 0L : Math.max(0L, expires - System.currentTimeMillis());
    }

    @Override
    public long getActivationWindowRemaining(Player player) {
        return getWindowRemaining(player);
    }

    @Override
    public TriggerResult onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        activeWindows.put(player.getUniqueId(), System.currentTimeMillis() + WINDOW_MILLIS);
        player.sendMessage(ChatColor.AQUA + "Crash Glitch armed. Hit a player within 15s!");
        return TriggerResult.consume(0);
    }

    @Override
    public TriggerResult onEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return TriggerResult.none();
        }
        if (!(event.getEntity() instanceof Player target)) {
            return TriggerResult.none();
        }
        if (!isWindowActive(player)) {
            return TriggerResult.none();
        }
        activeWindows.remove(player.getUniqueId());
        banTarget(target, player);
        player.sendMessage(ChatColor.RED + "You crashed " + target.getName() + "!");
        return TriggerResult.consume(COOLDOWN_MILLIS);
    }

    private void banTarget(Player target, Player source) {
        String message = ChatColor.RED + "CONNECTION THROTTLED";
        long expiry = System.currentTimeMillis() + WINDOW_MILLIS;
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(target.getName(), message, new java.util.Date(expiry), source.getName());
        target.kickPlayer(message);
    }
}
