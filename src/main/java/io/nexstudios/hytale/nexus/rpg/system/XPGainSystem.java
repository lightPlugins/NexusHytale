package io.nexstudios.hytale.nexus.rpg.system;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.nexstudios.hytale.nexus.rpg.components.PlayerRPGComponent;
import io.nexstudios.hytale.nexus.rpg.events.GiveXPEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class XPGainSystem extends DeathSystems.OnDeathSystem {

    private static final long XP_PER_KILL = 50;

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.of(PlayerRef.getComponentType());
    }

    @Override
    public void onComponentAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull DeathComponent deathComponent,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
            ) {

        var deathInfo = deathComponent.getDeathInfo();
        if(deathInfo == null) return;

        if(!(deathInfo.getSource() instanceof Damage.EntitySource source)) return;
        // killerRef cant be null here ??
        var killerRef = source.getRef();
        if(!killerRef.isValid()) return;

        var killer = store.getComponent(killerRef, PlayerRef.getComponentType());
        if(killer == null) return;

        var playerRPGComponent = store.getComponent(killerRef, PlayerRPGComponent.getComponentType());
        if(playerRPGComponent == null) return;

        killer.sendMessage(Message.raw("+%d XP".formatted(XP_PER_KILL)));
        GiveXPEvent.dispatch(killerRef, XP_PER_KILL);
    }
}
