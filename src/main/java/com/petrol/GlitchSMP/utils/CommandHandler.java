package com.petrol.GlitchSMP.utils;

import com.petrol.GlitchSMP.Registry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final com.petrol.GlitchSMP.GlitchSMP plugin;
    private final Registry registry;
    private final AbilityHandler abilityHandler;

    public CommandHandler(com.petrol.GlitchSMP.GlitchSMP plugin, Registry registry, AbilityHandler abilityHandler) {
        this.plugin = plugin;
        this.registry = registry;
        this.abilityHandler = abilityHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "/glitch give <player> <id>, /glitch equip <id>, /glitch unequip");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "give":
                return handleGive(sender, args);
            case "equip":
                return handleEquip(sender, args);
            case "unequip":
                return handleUnequip(sender);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
                return true;
        }
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /glitch give <player> <id>");
            return true;
        }
        Player target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        Optional<ItemStack> stack = registry.getItem(args[2]).map(item -> item.createItem());
        if (stack.isPresent()) {
            target.getInventory().addItem(stack.get());
            sender.sendMessage(ChatColor.GREEN + "Gave " + args[2] + " to " + target.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown glitch id.");
        }
        return true;
    }

    private boolean handleEquip(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /glitch equip <id>");
            return true;
        }
        Player player = (Player) sender;
        if (registry.getAbility(args[1]).isPresent()) {
            abilityHandler.equip(player, args[1]);
            player.sendMessage(ChatColor.GREEN + "Equipped glitch " + args[1]);
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown glitch id.");
        }
        return true;
    }

    private boolean handleUnequip(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player) sender;
        abilityHandler.unequip(player);
        player.sendMessage(ChatColor.YELLOW + "Glitch unequipped.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("give");
            subs.add("equip");
            subs.add("unequip");
            return subs;
        }
        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("equip")) {
                List<String> ids = new ArrayList<>();
                registry.getAllItems().forEach(item -> ids.add(item.getId()));
                return ids;
            }
        }
        return Collections.emptyList();
    }
}
