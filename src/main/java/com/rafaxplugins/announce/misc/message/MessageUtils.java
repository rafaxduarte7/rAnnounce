package com.rafaxplugins.announce.misc.message;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.bukkit.ChatColor.COLOR_CHAR;

public class MessageUtils {

    public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-OR]");

    public static String translateFormat(String format, Object... args) {
        return MessageUtils.translateColorCodes(String.format(
                format, args
        ));
    }

    public static String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String[] translateColorCodes(String... messages) {
        List<String> list = new ArrayList<>();
        for (String message : messages) {
            String translateColorCodes = translateColorCodes(message);
            list.add(translateColorCodes);
        }
        return list.toArray(new String[0]);
    }

    public static String stripColor(final String input) {
        if (input == null) {
            return null;
        }
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static String stripColorDefault(String message, ChatColor... colors) {

        String COLOR_CHAR = "&";

        if (colors.length < 1) {
            colors = ChatColor.values();
        }

        StringBuilder codes = new StringBuilder("");

        for (ChatColor color : colors) {
            codes.append(color.toString().toCharArray()[1]);
        }

        return Pattern.compile("(?i)" + COLOR_CHAR + "[" + codes.toString().toUpperCase() + "]")
                .matcher(message)
                .replaceAll("");
    }

    public static String stripColor(String message, ChatColor... colors) {

        if (colors.length < 1) {
            colors = ChatColor.values();
        }

        StringBuilder codes = new StringBuilder();

        for (ChatColor color : colors) {
            codes.append(color.toString().toCharArray()[1]);
        }

        return Pattern.compile("(?i)" + COLOR_CHAR + "[" + codes.toString().toUpperCase() + "]")
                .matcher(message)
                .replaceAll("");
    }
}
