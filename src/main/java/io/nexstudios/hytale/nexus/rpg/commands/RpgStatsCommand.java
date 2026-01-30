package io.nexstudios.hytale.nexus.rpg.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.nexstudios.hytale.nexus.rpg.components.PlayerRPGComponent;
import org.jetbrains.annotations.NotNull;

public class RpgStatsCommand extends AbstractPlayerCommand {

    public RpgStatsCommand() {
        super("stats", "Displays your RPG stats.");
    }

    @Override
    protected void execute(
            @NotNull CommandContext commandContext,
            @NotNull Store<EntityStore> store,
            @NotNull Ref<EntityStore> ref,
            @NotNull PlayerRef playerRef,
            @NotNull World world) {

        var rpg = store.getComponent(ref, PlayerRPGComponent.getComponentType());
        if(rpg == null) {
            playerRef.sendMessage(Message.raw("No RPG data found!"));
            return;
        }

        var level = rpg.getLevel();
        var totalXP = rpg.getTotalExperience();
        var currentXp = rpg.getCurrentLevelXP();
        var toNext = rpg.getXPToNextLevel();
        // get the percentage (* 100)
        var progress = (int) (rpg.getProgress() * 100);

        playerRef.sendMessage(Message.raw("========= RPG STATS ========="));
        playerRef.sendMessage(Message.raw("Level: %d%s".formatted(
                level,
                rpg.isMaxLevel() ? " (MAX)" : ""
        )));

        playerRef.sendMessage(Message.raw("Total XP: %d".formatted(totalXP)));

        if(!rpg.isMaxLevel()) {
            playerRef.sendMessage(Message.raw("Progress: %d/%d (%d%%)".formatted(
                    currentXp,
                    currentXp + toNext,
                    progress
            )));

            playerRef.sendMessage(Message.raw("XP to next level: %d".formatted(toNext)));
        }
    }
}
