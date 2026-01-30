package io.nexstudios.hytale.nexus.rpg.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.nexstudios.hytale.nexus.NexusCore;
import io.nexstudios.hytale.nexus.rpg.components.PlayerRPGComponent;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class PlayerJoinSystem extends RefSystem<EntityStore> {


    @Override
    public void onEntityAdded(
            @NullableDecl Ref<EntityStore> ref,
            @NullableDecl AddReason addReason,
            @NullableDecl Store<EntityStore> store,
            @NullableDecl CommandBuffer<EntityStore> commandBuffer) {

        if(addReason != AddReason.LOAD) return;

        if(store == null) {
            NexusCore.getInstance().getLogger().atWarning().log("Entity store is null for player join system");
            return;
        }

        if(ref == null) {
            NexusCore.getInstance().getLogger().atWarning().log("Ref is null for player join system");
            return;
        }

        if(commandBuffer == null) {
            NexusCore.getInstance().getLogger().atWarning().log("CommandBuffer is null for player join system");
            return;
        }

        var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if(playerRef == null) return;

        var rpgType = PlayerRPGComponent.getComponentType();
        var rpg = store.getComponent(ref, rpgType);

        if(rpg != null) {
            playerRef.sendMessage(Message.raw("Welcome back! Level %d (%d XP)".formatted(rpg.getLevel(), rpg.getTotalExperience())));
        } else {
            commandBuffer.addComponent(ref, rpgType, new PlayerRPGComponent());
            playerRef.sendMessage(Message.raw("Welcome! Your adventure begins at Level 1."));
        }
    }

    @Override
    public void onEntityRemove(
            @NullableDecl Ref<EntityStore> ref,
            @NullableDecl RemoveReason removeReason,
            @NullableDecl Store<EntityStore> store,
            @NullableDecl CommandBuffer<EntityStore> commandBuffer) {

    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.of(PlayerRef.getComponentType());
    }
}
