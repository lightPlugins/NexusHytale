package io.nexstudios.hytale.nexus.rpg.commands;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class RpgSpawnCommand extends AbstractPlayerCommand {


    private static final String DEFAULT_TYPE = "Skeleton";
    private static final int DEFAULT_COUNT = 1;
    private static final int MAX_COUNT = 100;

    private final OptionalArg<String> typeArg;
    private final OptionalArg<Integer> countArg;

    public RpgSpawnCommand() {
        super("spawn", "Spawns a defined type of mob at your location");
        this.typeArg = withOptionalArg("type", "NPC Type", ArgTypes.STRING);
        this.countArg = withOptionalArg("count", "Amount of mobs to spawn", ArgTypes.INTEGER)
                .addValidator(Validators.greaterThanOrEqual(1))
                .addValidator(Validators.lessThan(MAX_COUNT + 1)
                );
    }


    @Override
    protected void execute(
            @NotNull CommandContext commandContext,
            @NotNull Store<EntityStore> store,
            @NotNull Ref<EntityStore> ref,
            @NotNull PlayerRef playerRef,
            @NotNull World world) {

        var npcType = typeArg.get(commandContext);
        if(npcType == null) npcType = DEFAULT_TYPE;

        var count = countArg.get(commandContext);
        if(count == null) count = DEFAULT_COUNT;

        var transform = store.getComponent(ref, TransformComponent.getComponentType());

        if(transform == null) {
            playerRef.sendMessage(Message.raw("No transform component found! Could not get your location."));
            return;
        }

        var roleIndex = NPCPlugin.get().getIndex(npcType);

        if(roleIndex < 0) {
            playerRef.sendMessage(Message.raw("No NPC with type '%s' found!".formatted(npcType)));
            return;
        }

        var playerPos = transform.getPosition();
        var worldStore = world.getEntityStore().getStore();
        var random = ThreadLocalRandom.current();
        var spawned = 0;

        for(int i = 0; i < count; i++) {
            var spawnPos = new Vector3d(
                    playerPos.getX() + random.nextDouble() * 6 - 3,
                    playerPos.getY() + 0.5,
                    playerPos.getZ() + random.nextDouble() * 6 - 3
            );

            var result = NPCPlugin.get().spawnNPC(worldStore, npcType, null, spawnPos, new Vector3f());

            if(result != null && result.first() != null) spawned++;

            playerRef.sendMessage(Message.raw("Spawned %d %s(s)".formatted(spawned, npcType)));
        }
    }
}
