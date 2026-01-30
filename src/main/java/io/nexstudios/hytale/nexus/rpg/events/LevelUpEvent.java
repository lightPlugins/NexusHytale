package io.nexstudios.hytale.nexus.rpg.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;

public record LevelUpEvent(
        @NotNull Ref<EntityStore> playerRef,
        int oldLevel,
        int newLevel
) implements IEvent<Void> {

    public int levelsGained() {
        return newLevel - oldLevel;
    }

    public static void dispatch(Ref<EntityStore> playerRef, int oldLevel, int newLevel) {
        IEventDispatcher<LevelUpEvent, LevelUpEvent> dispatcher =
                HytaleServer.get().getEventBus().dispatchFor(LevelUpEvent.class);

        if(dispatcher.hasListener()) {
            dispatcher.dispatch(new LevelUpEvent(playerRef, oldLevel, newLevel));
        }

    }
}
