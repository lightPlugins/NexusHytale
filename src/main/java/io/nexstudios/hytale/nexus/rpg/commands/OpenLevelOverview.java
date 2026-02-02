package io.nexstudios.hytale.nexus.rpg.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.nexstudios.hytale.nexus.NexusCore;
import io.nexstudios.hytale.nexus.rpg.pages.LevelOverviewPage;
import org.jetbrains.annotations.NotNull;

public class OpenLevelOverview extends AbstractPlayerCommand {

    public OpenLevelOverview() {
        super("overview", "Opens the level overview.");
    }

    @Override
    protected void execute(
            @NotNull CommandContext commandContext,
            @NotNull Store<EntityStore> store,
            @NotNull Ref<EntityStore> ref,
            @NotNull PlayerRef playerRef,
            @NotNull World world) {

        Player player = store.getComponent(ref, Player.getComponentType());
        LevelOverviewPage view = new LevelOverviewPage(playerRef);

        if(player == null) {
            NexusCore.getInstance().getLogger().atWarning().log("Player is null for level overview command");
            return;
        }

        player.getPageManager().openCustomPage(ref, store, view);
    }
}
