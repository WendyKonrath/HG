package org.hg.gamemanager;

import org.hg.utils.GetSettings;

public enum GameStats {
    WAITING(GetSettings.messages.getConfig().getString("GameStats.Waiting")),
    STARTING(GetSettings.messages.getConfig().getString("GameStats.Starting")),
    INVINCIBLE(GetSettings.messages.getConfig().getString("GameStats.Invincible")),
    GAME(GetSettings.messages.getConfig().getString("GameStats.Game")),
    ENDED(GetSettings.messages.getConfig().getString("GameStats.Ended"));
    private final String description;

    // Constructor to initialize the description
    GameStats(String description) {
        this.description = description;
    }

    // Getter method to access the description
    public String getDescription() {
        return description;
    }
}
