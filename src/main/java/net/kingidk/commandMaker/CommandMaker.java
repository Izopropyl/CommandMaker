package net.kingidk.commandMaker;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CommandMaker extends JavaPlugin {
    private final List<CommandCreation> registeredCommands = new ArrayList<>();
    public boolean papi;
    private BukkitAudiences audiences;

    public BukkitAudiences getAudiences() {
        return audiences;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        audiences = BukkitAudiences.create(this);

         var placeholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");

         if (placeholderAPI == null) {
            getLogger().info("PlaceholderAPI not detected, PAPI-based placeholders will be in plain-text!");
            papi = false;
        } else papi = true;


        registerCommands();
        Objects.requireNonNull(getCommand("commandmaker")).setExecutor(new AdminCommand(this));
        }


    @Override
    public void onDisable() {
        unregisterCommands();
        if (audiences != null) audiences.close();
    }
    public void reload() {
        unregisterCommands();
        reloadConfig();
        registerCommands();
    }

    private void registerCommands() {
        SimpleCommandMap commandMap = getCommandMap();

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
        SimpleCommandMap commandMap = getCommandMap();
        if (commandMap == null) return;

        try {
            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            for (CommandCreation cmd : registeredCommands) {
                cmd.unregister(commandMap);
                knownCommands.remove(cmd.getName());
                knownCommands.remove(getName() + ":" + cmd.getName());
                for (String alias : cmd.getAliases()) {
                    knownCommands.remove(alias);
                    knownCommands.remove(getName() + ":" + alias);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            getLogger().severe("Failed to unregister commands: " + e.getMessage());
        }

        registeredCommands.clear();
    }

    private SimpleCommandMap getCommandMap() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (SimpleCommandMap) field.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            getLogger().severe("Failed to retrieve command map: " + e.getMessage());
            return null;
        }
    }


}
