package com.rafaxplugins.announce.commands;

import com.rafaxplugins.announce.AnnouncePlugin;
import com.rafaxplugins.announce.commands.impl.AnnounceReloadSubCommand;
import com.rafaxplugins.announce.misc.command.CustomCommand;
import com.rafaxplugins.announce.misc.command.impl.CommandRestriction;
import com.rafaxplugins.announce.misc.command.impl.arguments.Argument;
import com.rafaxplugins.announce.misc.message.Message;
import com.rafaxplugins.announce.misc.utils.PlayerCooldowns;
import lombok.val;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AnnounceCommand extends CustomCommand {

    public static final String ANNOUNCE_DELAY_KEY = "ANNOUNCE_DELAY_KEY";

    public AnnounceCommand() {
        super("anunciar", CommandRestriction.CONSOLE_AND_IN_GAME, "megafone");

        registerArgument(new Argument("msg", "mensagem do anúncio"));

        registerSubCommand(new AnnounceReloadSubCommand());
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (isConsole(sender)) return;

        val player = (Player) sender;
        val config = AnnouncePlugin.getInstance().getConfig();

        if (!PlayerCooldowns.hasEnded(player, ANNOUNCE_DELAY_KEY)) {
            Message.ERROR.send(player, config.getString("player-must-wait")
                    .replace("<time>", PlayerCooldowns.getFormattedTimeLeft(player, ANNOUNCE_DELAY_KEY)));
            return;
        }

        if (config.getBoolean("player-need-permission") && !player.hasPermission(config.getString("player-permission"))) {
            Message.ERROR.send(player, config.getString("player-no-has-permission"));
            return;
        }

        val msg = String.join(" ", args);
        val replaced = config.getStringList("announce-message")
                .stream()
                .map(line -> line.replace("<player>", player.getName()).replace("<msg>", msg))
                .collect(Collectors.toList());

        Message.sendBroadcast(replaced);

        if (config.getBoolean("announce-bypass")) {
            if (!player.hasPermission(config.getString("announce-bypass-permission"))) {
                PlayerCooldowns.start(player, ANNOUNCE_DELAY_KEY, config.getLong("player-delay-to-announce"), TimeUnit.MINUTES);
            }
        }
    }

}
