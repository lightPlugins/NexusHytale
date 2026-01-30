package io.nexstudios.hytale.nexus.rpg.handler;

import io.nexstudios.hytale.nexus.rpg.components.PlayerRPGComponent;
import io.nexstudios.hytale.nexus.rpg.events.GiveXPEvent;
import io.nexstudios.hytale.nexus.rpg.events.LevelUpEvent;

import java.util.function.Consumer;

public class GiveXPHandler implements Consumer<GiveXPEvent> {


    @Override
    public void accept(GiveXPEvent event) {
        if(!event.playerRef().isValid()) return;

        var store = event.playerRef().getStore();

        var rpg = store.getComponent(event.playerRef(), PlayerRPGComponent.getComponentType());
        if(rpg == null) return;

        var oldLevel = rpg.getLevel();
        var leveledUp = rpg.addExperience(event.amount());

        if(leveledUp) {
            LevelUpEvent.dispatch(event.playerRef(), oldLevel, rpg.getLevel());
        }
    }
}
