package net.kingidk.commandMaker;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandCreation extends Command {
    private final List<String> actions;
    private final CommandMaker plugin;
    private final String permission;

    public CommandCreation(String name, List<String> aliases, List<String> actions, CommandMaker plugin, String permission) {
        super(name);
        this.plugin = plugin;
        setAliases(aliases);
        this.actions = actions;
        this.permission = permission;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        if (!(permission == null)) {
            if (!sender.hasPermission(permission)) {
                sender.sendMessage(Component.text("You do not have permission to run this command!", NamedTextColor.RED));
                return true;
            }
        }

        for (String string : actions) {
            if (!string.contains(":")) {
                plugin.getLogger().warning("Incorrectly formatted action! Failed to parse: " + string);
                return true;
            }
            int colonIndex = string.indexOf(":");
            String prefix = string.substring(0, colonIndex + 1);
            String action = string.substring(colonIndex + 1).trim();
            if (sender instanceof Player p) {
                action = action.replace("{player}", p.getName());
            }
            switch (prefix) {
                case "MESSAGE:" -> sendMessage(sender, action);
                case "CONSOLE:" -> runCommand(sender, action, true);
                case "PLAYER:" -> runCommand(sender, action, false);
                default -> plugin.getLogger().warning("Incorrectly formatted action! Failed to parse: " + string);
            }
        }


        return true;
    }

    public void runCommand(CommandSender sender, String command, boolean isConsole) {
        if (isConsole) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        } else {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(Component.text("You must be a player to run this command!", NamedTextColor.RED));
                return;
            }
            p.getScheduler().run(plugin, t -> Bukkit.dispatchCommand(p, command), null);
        }
    }

    public void sendMessage(CommandSender sender, String action) {
            Component component = MiniMessage.miniMessage().deserialize(convertLegacyToMiniMessage(action));
            sender.sendMessage(component);
    }



    private static String convertLegacyToMiniMessage(String input) {
        return input
                .replace("&0", "<black>").replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>").replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>").replace("&5", "<dark_purple>")
                .replace("&6", "<gold>").replace("&7", "<gray>")
                .replace("&8", "<dark_gray>").replace("&9", "<blue>")
                .replace("&a", "<green>").replace("&b", "<aqua>")
                .replace("&c", "<red>").replace("&d", "<light_purple>")
                .replace("&e", "<yellow>").replace("&f", "<white>")
                .replace("&l", "<bold>").replace("&o", "<italic>")
                .replace("&n", "<underlined>").replace("&m", "<strikethrough>")
                .replace("&k", "<obfuscated>").replace("&r", "<reset>");
    }




}
