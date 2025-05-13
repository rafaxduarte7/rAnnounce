package com.rafaxplugins.announce.misc.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rafaxplugins.announce.misc.command.impl.CommandRestriction;
import com.rafaxplugins.announce.misc.command.impl.Commandable;
import com.rafaxplugins.announce.misc.command.impl.arguments.Argument;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("all")
public abstract class CustomCommand extends Command implements Commandable<CommandSender> {

    @Getter
    protected final String name0;
    @Getter
    protected final Set<String> aliases0;
    @Getter
    protected final CommandRestriction commandRestriction;
    @Getter
    protected final Map<CustomCommand, String> subCommands = Maps.newConcurrentMap();
    @Getter
    protected final LinkedList<Argument> arguments = Lists.newLinkedList();
    @Getter
    protected Commandable<CommandSender> parent;

    public CustomCommand(String name, CommandRestriction commandRestriction, String... aliases) {
        super(name);

        this.name0 = name;
        this.aliases0 = Sets.newHashSet(aliases);
        this.commandRestriction = commandRestriction;

        if (aliases.length > 0) {
            setAliases(Lists.newArrayList(aliases));
        }
    }

    public CustomCommand(String name, String... aliases) {
        this(name, CommandRestriction.CONSOLE_AND_IN_GAME, aliases);
    }

    @Override
    public boolean isConsole(CommandSender sender) {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    public boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    public String getSenderNick(CommandSender sender) {
        return sender.getName();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        this.executeRaw(sender, label, args);
        return true;
    }

    @Override
    public void sendSubCommandUsage(CommandSender sender) {
        if (!this.getSubCommands().isEmpty()) {

            StringBuilder builder = new StringBuilder("\n")
                    .append(ChatColor.GREEN)
                    .append("Comandos disponíveis:")
                    .append("\n ")
                    .append(ChatColor.GRAY);

            for (Map.Entry<CustomCommand, String> entry : this.getSubCommands().entrySet()) {
                CustomCommand subCommand = entry.getKey();
                if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
                    continue;
                }

                String value = entry.getValue();

                builder.append("\n/")
                        .append(ChatColor.YELLOW);

                List<String> labels = Lists.newLinkedList();
                labels.add(subCommand.getName0());

                Commandable<CommandSender> curr = subCommand;

                while ((curr = curr.getParent()) != null) {
                    labels.add(curr.getName0());
                }

                for (int i = labels.size() - 1; i >= 0; i--) {
                    builder.append(labels.get(i));
                    builder.append(" ");
                }

                for (Argument argument : subCommand.getArguments()) {
                    builder.append(ChatColor.GRAY).append(argument.getName());
                    builder.append(" ");
                }

                if (value != null && !value.isEmpty()) {
                    builder.append(ChatColor.WHITE).append(" - ").append(value);
                }
            }

            builder.append("\n ");

            sender.sendMessage(builder.toString());
        }
    }

    @Override
    public void executeRaw(CommandSender sender, String label, String[] args) {
        if (this.getPermission() != null && !this.getPermission().isEmpty() && !sender.hasPermission(this.getPermission())) {
            sender.sendMessage(ChatColor.RED + "Você não possui permissão para executar este comando.");
            return;
        }

        if (getCommandRestriction() != null) {
            if (this.getCommandRestriction().equals(CommandRestriction.CONSOLE) && !isConsole(sender)) {
                sender.sendMessage(ChatColor.RED + "Este comando só pode ser usado pelo console.");
                return;
            }

            if (this.getCommandRestriction().equals(CommandRestriction.IN_GAME) && !isPlayer(sender)) {
                sender.sendMessage(ChatColor.RED + "Este comando só pode ser usado por jogadores.");
                return;
            }
        }

        try {
            if (args.length > 0) {
                CustomCommand found = null;
                for (CustomCommand sub : getSubCommands().keySet()) {
                    if (sub.getName0().equalsIgnoreCase(args[0]) || sub.getAliases0().stream().anyMatch(alias -> alias.equalsIgnoreCase(args[0]))) {
                        found = sub;
                        break;
                    }
                }

                Commandable subCommand = found;

                if (subCommand != null) {
                    subCommand.executeRaw(sender, subCommand.getName0(), Arrays.copyOfRange(args, 1, args.length));
                    return;
                }
            }

            Map<Argument, String> mapOfArgs = IntStream
                    .range(0, this.getArguments().size())
                    .boxed()
                    .collect(Collectors.toMap(
                            i -> getArguments().get(i),
                            i -> {
                                if (i < args.length) {
                                    return args[i];
                                }

                                return "";
                            }
                    ));

            for (Map.Entry<Argument, String> entry : mapOfArgs.entrySet()) {
                Argument argument = entry.getKey();
                String argumentValue = entry.getValue();

                if (argumentValue.isEmpty() && argument.isRequired()) {
                    if (!entry.getKey().isValid(entry.getValue())) {
                        String errorMessage = entry.getKey().getErrorMessage(argumentValue);

                        if (errorMessage != null && !errorMessage.isEmpty()) {
                            sender.sendMessage(ChatColor.RED + errorMessage);
                        }
                    }

                    sender.sendMessage(this.getUsage(label));
                    return;
                }
            }

            this.onCommand(sender, args);
        } catch (Exception exception) {
            exception.printStackTrace();

            sender.sendMessage(ChatColor.RED + "Algo de errado aconteceu, tente novamente.");
        }
    }

    public final void registerSubCommand(CustomCommand subCommand) {
        this.registerSubCommand(subCommand, "");
    }

    public final void registerSubCommand(CustomCommand subCommand, String description) {
        subCommand.parent = this;
        this.subCommands.put(subCommand, description);
    }

    public final void unregisterSubCommand(String label) {
        this.subCommands.keySet().removeIf(sub -> sub.name0.equals(label) || sub.aliases0.contains(label));
    }

    public final void registerArgument(Argument argument) {
        this.arguments.add(argument);
    }

    @Override
    public List<String> tabComplete0(String[] args) {
        if (args.length > 0 && this.getArguments().size() >= args.length) {
            Stream<String> possibilities = Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getName);

            return possibilities
                    .filter(name -> name.regionMatches(true, 0, args[0], 0, args[0].length()))
                    .collect(Collectors.toList());
        }

        return ImmutableList.of();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        CustomCommand subCommand = null;
        for (CustomCommand sub : getSubCommands().keySet()) {
            if (sub.getName0().equalsIgnoreCase(args[0]) || sub.getAliases0().stream().anyMatch(a -> a.equalsIgnoreCase(args[0]))) {
                subCommand = sub;
                break;
            }
        }

        if (subCommand != null) {
            return subCommand.tabComplete(sender, alias, Arrays.copyOfRange(args, 1, args.length));
        }

        if (!this.tabComplete0(args).isEmpty()) {
            return this.tabComplete0(args);
        }

        return super.tabComplete(sender, alias, args);
    }
}
