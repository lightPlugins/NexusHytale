package io.nexstudios.hytale.nexus.rpg.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class RPGCommand extends AbstractCommandCollection {


    public RPGCommand() {
        super("rpg", "RPG Debug Commands");
        addSubCommand(new RpgSpawnCommand());
        addSubCommand(new RpgXpCommand());
        addSubCommand(new RpgStatsCommand());
    }
}
