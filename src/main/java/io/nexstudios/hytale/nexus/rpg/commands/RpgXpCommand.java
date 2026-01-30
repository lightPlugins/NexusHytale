package io.nexstudios.hytale.nexus.rpg.commands;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.nexstudios.hytale.nexus.rpg.components.PlayerRPGComponent;
import io.nexstudios.hytale.nexus.rpg.events.GiveXPEvent;
import org.jetbrains.annotations.NotNull;

public class RpgXpCommand extends AbstractPlayerCommand {

    private static final int DEFAULT_AMOUNT = 50;
    private final OptionalArg<Integer> amountArg;

    public RpgXpCommand() {
        super("xp", "Give yourself XP");
        this.amountArg = withOptionalArg("amount", "XP amount (>0)", ArgTypes.INTEGER)
                .addValidator(Validators.greaterThan(0));
    }


    @Override
    protected void execute(
            @NotNull CommandContext commandContext,
            @NotNull Store<EntityStore> store,
            @NotNull Ref<EntityStore> ref,
            @NotNull PlayerRef playerRef,
            @NotNull World world) {


        var amount = amountArg.get(commandContext);
        if(amount == null) amount = DEFAULT_AMOUNT;

        // check if player has rpg component
        if(store.getComponent(ref, PlayerRPGComponent.getComponentType()) == null) {
            playerRef.sendMessage(Message.raw("No RPG data found!"));
            return;
        }

        playerRef.sendMessage(Message.raw("+%d XP".formatted(amount)));
        GiveXPEvent.dispatch(ref, amount);

    }
}
