package com.rafaxplugins.announce.misc.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandRegistry {

    static {
        try {
            Field commandsMapField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            commandsMapField.setAccessible(true);
        } catch (Exception ignored) {
        }
    }

    public static void registerCommand(CustomCommand... commands) {
        SimpleCommandMap commandMap = ((CraftServer) Bukkit.getServer()).getCommandMap();

        Set<String> names = Sets.newHashSet(commands).stream().map(Command::getName).collect(Collectors.toSet());

        try {
            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");

            knownCommandsField.setAccessible(true);

            Map<String, Command> refKnownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            Lists.newArrayList(commandMap.getCommands())
                    .stream()
                    .filter(targetCommand -> names.contains(targetCommand.getName()))
                    .forEach(targetCommand -> {
                        targetCommand.unregister(commandMap);
                        refKnownCommands.remove(targetCommand.getName());
                        targetCommand.getAliases().forEach(refKnownCommands::remove);
                    });

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(CommandRegistry.class.getName()).log(Level.SEVERE, null, ex);
        }

        Stream.of(commands).forEach(command -> commandMap.register(command.getName(), command));
    }
}
