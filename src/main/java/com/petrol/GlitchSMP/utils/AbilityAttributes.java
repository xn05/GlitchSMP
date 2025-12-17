package com.petrol.GlitchSMP.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * Contract implemented by every glitch ability. Each hook returns a {@link TriggerResult}
 * describing whether the action consumed the ability and which cooldown (if any) to apply.
 */
public interface AbilityAttributes {
    enum Stage {
        BASE,
        STAGE_MATRIX,
        STAGE_CALAMITY
    }

    String DEFAULT_GLYPH = "\uE901";

    /**
     * Encapsulates the outcome of an ability trigger.
     */
    record TriggerResult(boolean startCooldown, long cooldownMillis) {
        public static TriggerResult none() {
            return new TriggerResult(false, 0L);
        }

        public static TriggerResult consume(long cooldownMillis) {
            return new TriggerResult(true, cooldownMillis);
        }
    }

    /** Unique lowercase identifier used across handlers/configs. */
    String getId();

    /** Player-facing name for HUDs and logs. */
    String getDisplayName();

    /**
     * Unicode glyph rendered through the resource pack to represent this glitch in the actionbar HUD.
     * Defaults to the blank slot glyph if an ability does not override it.
     */
    default String getGlyph() {
        return DEFAULT_GLYPH;
    }

    /** Stage grouping for menus. */
    default Stage getStage() {
        return Stage.BASE;
    }

    /**
     * Base cooldown applied after a trigger consumes the ability (in milliseconds).
     * Override this or {@link #getCooldownSeconds()} to configure how long the ability
     * should be unusable once activated.
     */
    default long getCooldownMillis() {
        long seconds = getCooldownSeconds();
        return (seconds <= 0L) ? 0L : seconds * 1000L;
    }

    /**
     * Convenience hook allowing ability files to specify cooldowns in seconds.
     * Implementations may override this instead of {@link #getCooldownMillis()}.
     */
    default long getCooldownSeconds() {
        return 0L;
    }

    /** Called when the player equips the glitch. */
    default TriggerResult onEquip(Player player) {
        return TriggerResult.none();
    }

    /** Called when the glitch is unequipped. */
    default void onUnequip(Player player) {
    }

    /** Passive tick while equipped. */
    default TriggerResult onTick(Player player, long tick) {
        return TriggerResult.none();
    }

    /** Routed activation based on the player's configured control. */
    default TriggerResult onControlActivation(Player player, AbilityHandler.Slot slot,
                                             ControlHandler.ActivationAction action) {
        return TriggerResult.none();
    }

    /** Triggered when the player hits another entity. */
    default TriggerResult onEntityHit(EntityDamageByEntityEvent event) {
        return TriggerResult.none();
    }

    /** Triggered when the player is hit by another entity. */
    default TriggerResult onEntityDamaged(EntityDamageByEntityEvent event) {
        return TriggerResult.none();
    }

    /** Triggered when the player swaps items with their offhand. */
    default TriggerResult onOffhandSwap(Player player, AbilityHandler.Slot slot) {
        return TriggerResult.none();
    }

    /** Triggered on projectile launch by the player. */
    default TriggerResult onProjectileLaunch(ProjectileLaunchEvent event) {
        return TriggerResult.none();
    }

    /** Triggered when a projectile fired by the player lands. */
    default TriggerResult onProjectileHit(ProjectileHitEvent event) {
        return TriggerResult.none();
    }

    /** Triggered anytime the equipped player moves. */
    default TriggerResult onPlayerMove(PlayerMoveEvent event) {
        return TriggerResult.none();
    }

    /** Reacts to sneak toggles (useful for invisibility style glitches). */
    default TriggerResult onSneakToggle(PlayerToggleSneakEvent event) {
        return TriggerResult.none();
    }

    /** Called when the player's inventory is altered via clicks (for inventory scramble glitches). */
    default TriggerResult onInventoryClick(InventoryClickEvent event) {
        return TriggerResult.none();
    }

    /**
     * Catch-all hook for custom triggers (e.g., scripted trials or timeline events).
     */
    default TriggerResult onCustomEvent(String eventId, Player player, Object payload) {
        return TriggerResult.none();
    }

    /**
     * Allows an ability to keep receiving event callbacks even while its cooldown is running.
     * Useful for glitches that create short-lived windows (e.g., Crash) that must finish resolving.
     */
    default boolean allowWhileCooling(Player player) {
        return false;
    }

    /** Called on plugin reload to reset all timers, bossbars, and states. */
    default void reset() {
    }
}
