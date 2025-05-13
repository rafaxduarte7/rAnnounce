package com.rafaxplugins.announce;

import com.rafaxplugins.announce.commands.AnnounceCommand;
import com.rafaxplugins.announce.misc.command.CommandRegistry;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class AnnouncePlugin extends JavaPlugin {

    @Getter
    private static AnnouncePlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        CommandRegistry.registerCommand(
                new AnnounceCommand()
        );
    }
}
