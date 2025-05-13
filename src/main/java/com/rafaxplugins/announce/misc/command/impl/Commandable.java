package com.rafaxplugins.announce.misc.command.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.rafaxplugins.announce.misc.command.impl.arguments.Argument;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Commandable<T> {

    Commandable<T> getParent();

    String getName0();

    Set<String> getAliases0();

    CommandRestriction getCommandRestriction();

    <S extends Commandable<T>> Map<S, String> getSubCommands();

    LinkedList<Argument> getArguments();

    boolean isConsole(T sender);

    boolean isPlayer(T sender);

    String getSenderNick(T sender);

    default void onCommand(T sender, String[] args) {
        this.sendSubCommandUsage(sender);
    }

    void sendSubCommandUsage(T sender);

    default String getUsage(String label) {
        StringBuilder builder = new StringBuilder("§cUtilize: /");

        List<String> labels = Lists.newLinkedList();
        labels.add(this.getName0());

        Commandable<T> curr = this;
        while ((curr = curr.getParent()) != null) {
            labels.add(curr.getName0());
        }

        for (int i = labels.size() - 1; i >= 0; i--) {
            builder.append(labels.get(i));
            if (i != 0) {
                builder.append(" ");
            }
        }

        for (Argument argument : this.getArguments()) {
            builder.append(" ")
                    .append(argument.getName());
        }

        return builder.toString();
    }


    void executeRaw(T sender, String label, String[] args);

    default List<String> tabComplete0(String[] args) {
        return ImmutableList.of();
    }

}
