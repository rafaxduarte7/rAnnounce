package com.rafaxplugins.announce.commands.impl;

import com.rafaxplugins.announce.AnnouncePlugin;
import com.rafaxplugins.announce.misc.command.CustomCommand;
import com.rafaxplugins.announce.misc.command.impl.CommandRestriction;
import com.rafaxplugins.announce.misc.message.Message;
import org.bukkit.command.CommandSender;

public class AnnounceReloadSubCommand extends CustomCommand {

    public AnnounceReloadSubCommand() {
        super("reload", CommandRestriction.CONSOLE_AND_IN_GAME, "reiniciar");

        setPermission(AnnouncePlugin.getInstance().getConfig().getString("reload-permission"));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        AnnouncePlugin.getInstance().reloadConfig();

        Message.SUCCESS.send(sender, AnnouncePlugin.getInstance().getConfig().getString("reload-config"));
    }
}
