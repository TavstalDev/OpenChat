package io.github.tavstaldev.openChat.managers;

import io.github.tavstaldev.openChat.OpenChat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultPermissionManager implements IPermissionManager {
    private Permission perms = null;

    public VaultPermissionManager() {
        RegisteredServiceProvider<Permission> rsp = OpenChat.Instance.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return;
        }
        perms = rsp.getProvider();
    }

    @Override
    public boolean hasPermissions() {
        return true;
    }

    @Override
    public String getPrimaryGroup(Player player) {
        if (perms == null) {
            return "default";
        }

        return perms.getPrimaryGroup(player);
    }
}
