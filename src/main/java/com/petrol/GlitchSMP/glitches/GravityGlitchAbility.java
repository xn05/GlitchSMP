package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GravityGlitchAbility implements AbilityAttributes {
    private static final long COOLDOWN_SECONDS = 60L; // 1 minute

    private final Plugin plugin = Registry.get().getPlugin();

    @Override
    public String getId() {
        return "gravity";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.BLUE + "Gravity Glitch";
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    @Override
    public TriggerResult onControlActivation(PlayerInteractEvent event, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        Player player = event.getPlayer();
        // Apply slow falling to nearby players
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 7, 7, 7)) {
            if (entity instanceof Player target && !target.equals(player)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 300, 0)); // 15 seconds
            }
        }
        player.sendMessage(ChatColor.GREEN + "Gravity Glitch activated! Nearby players have low gravity for 15 seconds!");
        return TriggerResult.consume(getCooldownMillis());
    }
}
