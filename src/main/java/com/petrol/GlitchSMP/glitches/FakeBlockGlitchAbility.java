package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FakeBlockGlitchAbility implements AbilityAttributes {
    private static final long COOLDOWN_SECONDS = 120L;

    private final Plugin plugin = Registry.get().getPlugin();
    private final Map<UUID, BlockDisplay> fakeBlocks = new HashMap<>();

    @Override
    public String getId() {
        return "fake_block";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.GOLD + "Fake Block Glitch";
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    @Override
    public TriggerResult onControlActivation(PlayerInteractEvent event, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || !held.getType().isBlock()) {
            player.sendMessage(ChatColor.RED + "You must be holding a block to use this ability.");
            event.setCancelled(true);
            return TriggerResult.none();
        }

        // Remove all existing fake blocks in the world
        for (BlockDisplay bd : fakeBlocks.values()) {
            bd.remove();
        }
        fakeBlocks.clear();

        // Spawn new fake block
        Location loc = player.getLocation();
        loc.setX(Math.floor(loc.getX()));
        loc.setY(Math.floor(loc.getY()));
        loc.setZ(Math.floor(loc.getZ()));
        BlockDisplay blockDisplay = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
        blockDisplay.setBlock(held.getType().createBlockData());
        // Ensure the block is level and facing north
        Quaternionf northRotation = new Quaternionf().rotateY((float) Math.PI);
        blockDisplay.setTransformation(new Transformation(new Vector3f(), northRotation, new Vector3f(1, 1, 1), new Quaternionf()));
        fakeBlocks.put(player.getUniqueId(), blockDisplay);

        player.sendMessage(ChatColor.GREEN + "Fake block placed! Activate again to remove it and place a new one.");
        return TriggerResult.consume(getCooldownMillis());
    }
}
