package com.petrol.GlitchSMP;

import com.petrol.GlitchSMP.utils.CommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GlitchCommand extends BukkitCommand {
    private final CommandHandler handler;

    public GlitchCommand(CommandHandler handler) {
        super("glitch", "Glitch SMP command hub", "/glitch help", List.of("glitches"));
        this.handler = handler;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (alias.equalsIgnoreCase("glitches")) {
            if (args.length == 0) {
                return handler.onCommand(sender, this, "glitch", new String[]{"glitches"});
            }
            // If args, perhaps prepend "glitches" or something, but for now, pass as is
        }
        return handler.onCommand(sender, this, alias, args);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = handler.onTabComplete(sender, this, alias, args);
        return completions == null ? List.of() : completions;
    }
}
