package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class WindburstGlitchAbility implements AbilityAttributes {
    private static final long COOLDOWN_SECONDS = 30L; // 30 seconds

    private final Plugin plugin = Registry.get().getPlugin();

    @Override
    public String getId() {
        return "windburst";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.WHITE + "Windburst Glitch";
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    @Override
    public TriggerResult onControlActivation(Player player, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        // Launch the player forward and up
        Vector direction = player.getLocation().getDirection();
        Vector velocity = direction.multiply(6).setY(1); // Adjust for 30 blocks forward and some height
        player.setVelocity(velocity);
        // Spawn wind particles at the launch location
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);
        player.sendMessage(ChatColor.GREEN + "Windburst activated! Launched forward!");
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onOffhandSwap(Player player, AbilityHandler.Slot slot) {
        return onControlActivation(player, slot, ControlHandler.ActivationAction.OFFHAND);
    }
}
