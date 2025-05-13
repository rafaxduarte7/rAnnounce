package com.rafaxplugins.announce.misc.message;

import com.rafaxplugins.announce.misc.message.formatter.MessageFormatter;
import lombok.val;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("all")
public class Message extends MessageFormatter<CommandSender> {

    public static Message INFO = new Message("&e");
    public static Message GOLDEN = new Message("&6");
    public static Message EMPTY = new Message("");

    public static Message SUCCESS = new Message("&a") {
        @Override
        public void send(CommandSender sender, String message) {
            super.send(sender, message);
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.playSound(player.getLocation(), Sound.VILLAGER_YES, 1.5F, 1.5F);
            }
        }
    };

    public static Message ERROR = new Message("&c") {
        @Override
        public void send(CommandSender sender, String message) {
            super.send(sender, message);
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.5F, 1.5F);
            }
        }
    };

    public Message(String prefix) {
        super(prefix);
    }

    @Override
    public void send(CommandSender sender, String message) {
        if (sender != null) {
            sender.sendMessage(color(getMessage(message)));
        }
    }

    public static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public void send(Player player, String message, Object... args) {
        if (player != null && player.isOnline()) {
            player.sendMessage(color(getMessage(String.format(message, args))));
        }
    }

    public void sendToAll(String message) {
        String msg = color(getMessage(message));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(msg);
        }
    }

    public static void sendBroadcast(String... array) {
        for (val player : Bukkit.getOnlinePlayers()) {
            for (String line : array) {
                player.sendMessage(color(line));
            }
        }
    }

    public static void sendBroadcast(List<String> array) {
        for (val player : Bukkit.getOnlinePlayers()) {
            for (String line : array) {
                player.sendMessage(color(line));
            }
        }
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        if (player != null && player.isOnline()) {
            player.sendTitle(color(title), color(subtitle));
        }
    }

    public static void sendTitleToAll(String title, String subtitle) {
        String coloredTitle = color(title);
        String coloredSubtitle = color(subtitle);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(coloredTitle, coloredSubtitle);
        }
    }

    public static void playSound(Player player, Sound sound) {
        if (player != null && player.isOnline() && sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }

    public static void playSoundToAll(Sound sound) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playSound(player, sound);
        }
    }

    public static void sendActionBar(Player player, String message) {
        sendPacket(player, new PacketPlayOutChat(new ChatComponentText(color(message)), (byte) 2));
    }

    public static void sendActionBarToAll(String message) {
        String colored = color(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBar(player, colored);
        }
    }

    public static void sendPacket(Player player, Packet packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
