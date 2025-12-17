package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.ChatColor;
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
    public TriggerResult onControlActivation(Player player, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        // Apply low gravity to nearby players except self
        player.getWorld().getPlayers().stream()
                .filter(p -> !p.equals(player))
                .filter(p -> p.getLocation().distance(player.getLocation()) <= 7)
                .forEach(p -> {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 300, 0)); // 15 seconds
                    p.sendMessage(ChatColor.BLUE + "You feel lighter!");
                });
        player.sendMessage(ChatColor.GREEN + "Gravity Glitch activated! Nearby players have low gravity for 15 seconds.");
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onOffhandSwap(Player player, AbilityHandler.Slot slot) {
        return onControlActivation(player, slot, ControlHandler.ActivationAction.OFFHAND);
    }
}
