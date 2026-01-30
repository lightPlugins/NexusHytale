package io.nexstudios.hytale.nexus.rpg.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;

public record GiveXPEvent(
        @NotNull Ref<EntityStore> playerRef,
        long amount
) implements IEvent<Void> {

    public static void dispatch(Ref<EntityStore> playerRef, long amount) {
        IEventDispatcher<GiveXPEvent, GiveXPEvent> dispatcher =
                HytaleServer.get().getEventBus().dispatchFor(GiveXPEvent.class);

        if(dispatcher.hasListener()) {
            dispatcher.dispatch(new GiveXPEvent(playerRef, amount));
        }
    }

}
