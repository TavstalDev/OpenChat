package io.github.tavstaldev.openChat.managers;

import org.bukkit.entity.Player;

public interface IPermissionManager {
    boolean hasPermissions();

    String getPrimaryGroup(Player player);
}
