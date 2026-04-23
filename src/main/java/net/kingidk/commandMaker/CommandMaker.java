package net.kingidk.commandMaker;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CommandMaker extends JavaPlugin {
    private final List<CommandCreation> registeredCommands = new ArrayList<>();


    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        registerCommands();
        Objects.requireNonNull(getCommand("commandmaker")).setExecutor(new AdminCommand(this));
        }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        unregisterCommands();
    }
    public void reload() {
        unregisterCommands();
        reloadConfig();
        registerCommands();
    }

    private void registerCommands() {
        CommandMap commandMap = Bukkit.getServer().getCommandMap();
        for (String cmdName : getConfig().getStringList("config.enabled-commands")) {
            // Establish config settings for command
            List<String> aliases = getConfig().getStringList("commands." + cmdName + ".aliases");
            List<String> actions = getConfig().getStringList("commands." + cmdName + ".actions");
            String permission = getConfig().getString("commands." + cmdName + ".permission");

            ConfigurationSection argsSection = getConfig().getConfigurationSection("commands." + cmdName + ".args");
            List<ArgsDefinition> argDefs = new ArrayList<>();
            if (argsSection != null) {
                for (String argName : argsSection.getKeys(false)) {
                    String type = argsSection.getString(argName + ".type", "STRING");
                    boolean papi = argsSection.getBoolean(argName + ".placeholder", false);
                    argDefs.add(new ArgsDefinition(argName, type, papi));
                }
            }


            CommandCreation cmd = new CommandCreation(cmdName, aliases, actions, this, permission, argDefs);
            commandMap.register(getName(), cmd);
            registeredCommands.add(cmd);
    }
        getLogger().info("Successfully registered " + registeredCommands.size() + " commands to the server");

    }


    private void unregisterCommands() {
        CommandMap commandMap = Bukkit.getServer().getCommandMap();
        Map<String, Command> knownCommands = commandMap.getKnownCommands();
        for (CommandCreation cmd : registeredCommands) {
            cmd.unregister(commandMap);
            knownCommands.remove(cmd.getName());
            knownCommands.remove(getName() + ":" + cmd.getName());
            for (String alias : cmd.getAliases()) {
                knownCommands.remove(alias);
                knownCommands.remove(getName() + ":" + alias);
            }
        }

        registeredCommands.clear();
    }


}
