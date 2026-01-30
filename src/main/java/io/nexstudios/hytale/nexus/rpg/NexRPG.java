package io.nexstudios.hytale.nexus.rpg;

import io.nexstudios.hytale.nexus.NexusCore;
import io.nexstudios.hytale.nexus.rpg.commands.RPGCommand;
import io.nexstudios.hytale.nexus.rpg.components.PlayerRPGComponent;
import io.nexstudios.hytale.nexus.rpg.events.GiveXPEvent;
import io.nexstudios.hytale.nexus.rpg.events.LevelUpEvent;
import io.nexstudios.hytale.nexus.rpg.handler.GiveXPHandler;
import io.nexstudios.hytale.nexus.rpg.handler.LevelUpHandler;
import io.nexstudios.hytale.nexus.rpg.system.PlayerJoinSystem;
import io.nexstudios.hytale.nexus.rpg.system.XPGainSystem;
import lombok.Getter;

@Getter
public class NexRPG {


    private static NexRPG instance;

    public void init() {
        instance = this;
    }

    public void setup() {

        NexusCore.getInstance().getLogger().atInfo().log("Register NexRPG ...");

        var registry = NexusCore.getInstance().getEntityStoreRegistry();

        var rpgType = registry.registerComponent(
                PlayerRPGComponent.class,
                "MiniRPG_Player_Data",
                PlayerRPGComponent.CODEC
        );

        PlayerRPGComponent.setComponentType(rpgType);

        // Order doesn't matter for independent systems. Hytale manages execution based on system types+
        // We NEED the Dependency from the systems in the manifest.json!!!
        registry.registerSystem(new XPGainSystem());
        registry.registerSystem(new PlayerJoinSystem());

        var eventRegistry = NexusCore.getInstance().getEventRegistry();
        eventRegistry.register(GiveXPEvent.class, new GiveXPHandler());
        eventRegistry.register(LevelUpEvent.class, new LevelUpHandler());

        var commandRegistry = NexusCore.getInstance().getCommandRegistry();
        commandRegistry.registerCommand(new RPGCommand());



    }

}
