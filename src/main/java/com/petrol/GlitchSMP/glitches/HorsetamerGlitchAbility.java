package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class HorsetamerGlitchAbility implements AbilityAttributes {
    private static final long COOLDOWN_SECONDS = 600L; // 10 minutes

    private final Plugin plugin = Registry.get().getPlugin();

    @Override
    public String getId() {
        return "horsetamer";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.GOLD + "Horsetamer Glitch";
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    @Override
    public TriggerResult onControlActivation(Player player, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        SkeletonHorse horse = player.getWorld().spawn(player.getLocation(), SkeletonHorse.class);
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        horse.setTamed(true);
        horse.setOwner(player);
        player.sendMessage(ChatColor.GREEN + "Skeleton horse summoned!");
        horse.getAttribute(Attribute.MAX_HEALTH).setBaseValue(13 + Math.random() * 2); // Set health to 13-15
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onOffhandSwap(Player player, AbilityHandler.Slot slot) {
        return onControlActivation(player, slot, ControlHandler.ActivationAction.OFFHAND);
    }
}
