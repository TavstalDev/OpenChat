package io.github.tavstaldev.openChat.managers;

import org.bukkit.entity.Player;

public class FallbackPermissionManager implements IPermissionManager {
    @Override
    public boolean hasPermissions() {
        return false;
    }

    @Override
    public String getPrimaryGroup(Player player) {
        return "default";
    }
}
