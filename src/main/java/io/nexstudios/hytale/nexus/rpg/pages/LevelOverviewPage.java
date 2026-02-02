package io.nexstudios.hytale.nexus.rpg.pages;

import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.BasicCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jetbrains.annotations.NotNull;

public class LevelOverviewPage extends BasicCustomUIPage {

    public LevelOverviewPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    @Override
    public void build(@NotNull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Pages/LevelOverviewPage.ui");
        //uiCommandBuilder.set("#Header.Text", "Level Overview");
        //uiCommandBuilder.set("#TitleMore.Text", "Mooooore Test");
        // test
    }
}
