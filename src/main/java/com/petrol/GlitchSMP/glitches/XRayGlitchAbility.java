package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.MagmaCube;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class XRayGlitchAbility implements AbilityAttributes {
    private static final long DURATION_MILLIS = 20_000L; // 20 seconds
    private static final long COOLDOWN_SECONDS = 240L; // 4 minutes

    private final Plugin plugin = Registry.get().getPlugin();
    private final Set<Entity> glowingEntities = new HashSet<>();
    private final Set<MagmaCube> oreIndicators = new HashSet<>();

    @Override
    public String getId() {
        return "xray";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.AQUA + "XRay Glitch";
    }

    @Override
    public String getGlyph() {
        return "ⅶ";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    @Override
    public TriggerResult onControlActivation(Player player, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        // Apply glowing to players and living entities within 25 blocks
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof LivingEntity living && !entity.equals(player) && entity.getLocation().distance(player.getLocation()) <= 25) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 400, 0, false, false, false)); // 20 seconds
                glowingEntities.add(entity);
            }
        }

        // Highlight ores with glowing magma cubes
        int maxIndicators = 100; // Limit to prevent lag
        int count = 0;
        for (int x = -25; x <= 25 && count < maxIndicators; x++) {
            for (int y = -25; y <= 25 && count < maxIndicators; y++) {
                for (int z = -25; z <= 25 && count < maxIndicators; z++) {
                    Block block = player.getWorld().getBlockAt(player.getLocation().add(x, y, z));
                    if (isOre(block.getType())) {
                        MagmaCube cube = player.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), MagmaCube.class);
                        cube.setInvisible(true);
                        cube.setAI(false);
                        cube.setInvulnerable(true);
                        cube.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 400, 0, false, false, false));
                        cube.setSize(1);
                        oreIndicators.add(cube);
                        count++;
                    }
                }
            }
        }

        // Schedule to remove glowing after 20 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : glowingEntities) {
                    if (entity instanceof LivingEntity living) {
                        living.removePotionEffect(PotionEffectType.GLOWING);
                    }
                }
                glowingEntities.clear();
                for (MagmaCube cube : oreIndicators) {
                    cube.remove();
                }
                oreIndicators.clear();
            }
        }.runTaskLater(plugin, 400); // 20 seconds

        player.sendMessage(ChatColor.GREEN + "XRay Glitch activated! Nearby entities and ores are glowing for 20 seconds.");
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onOffhandSwap(Player player, AbilityHandler.Slot slot) {
        return onControlActivation(player, slot, ControlHandler.ActivationAction.OFFHAND);
    }

    private boolean isOre(Material material) {
        String name = material.name();
        return name.endsWith("_ORE");
    }
}
