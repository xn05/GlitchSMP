package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DualityGlitchAbility implements AbilityAttributes, Listener {
    private static final long COOLDOWN_SECONDS = 180L;
    private static final long DURATION_MILLIS = 10000L; // 10 seconds

    private final Plugin plugin = Registry.get().getPlugin();
    private final Map<UUID, Long> activeUntil = new HashMap<>();
    private final Set<UUID> frozenEntities = new HashSet<>();

    public DualityGlitchAbility() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        // Apply fire damage every second
        Bukkit.getScheduler().runTaskTimer(plugin, this::applyFireDamage, 0L, 20L);
    }

    @Override
    public String getId() {
        return "duality";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.AQUA + "Duality Glitch";
    }

    @Override
    public String getGlyph() {
        return "ↆ";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    @Override
    public TriggerResult onControlActivation(Player player, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        activeUntil.put(player.getUniqueId(), System.currentTimeMillis() + DURATION_MILLIS);
        player.sendMessage(ChatColor.GREEN + "Duality Glitch activated! Freeze on hit, fire to nearby enemies.");
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onOffhandSwap(Player player, AbilityHandler.Slot slot) {
        return onControlActivation(player, slot, ControlHandler.ActivationAction.OFFHAND);
    }

    @EventHandler
    public void handleEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!activeUntil.containsKey(player.getUniqueId())) {
            return;
        }
        if (System.currentTimeMillis() > activeUntil.get(player.getUniqueId())) {
            return;
        }
        Entity target = event.getEntity();
        if (!(target instanceof LivingEntity living)) {
            return;
        }
        frozenEntities.add(target.getUniqueId());
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 255, false, false)); // 2 seconds, max slowness
        // Remove freeze after 2 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> frozenEntities.remove(target.getUniqueId()), 40L);
    }

    private void applyFireDamage() {
        long now = System.currentTimeMillis();
        activeUntil.entrySet().removeIf(entry -> now > entry.getValue()); // Clean up expired
        for (Map.Entry<UUID, Long> entry : activeUntil.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;
            for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
                if (!(entity instanceof LivingEntity living)) continue;
                if (frozenEntities.contains(entity.getUniqueId())) {
                    // Nullify: remove freeze and don't apply fire
                    frozenEntities.remove(entity.getUniqueId());
                    living.removePotionEffect(PotionEffectType.SLOWNESS);
                    living.setFireTicks(0);
                } else {
                    living.setFireTicks(Math.max(living.getFireTicks(), 60)); // 3 seconds fire
                }
            }
        }
    }
}
