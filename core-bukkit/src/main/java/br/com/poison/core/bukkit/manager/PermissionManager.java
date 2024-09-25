package br.com.poison.core.bukkit.manager;

import br.com.poison.core.Core;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.profile.Profile;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionManager {

    public static void loadPermissions(Player player) {
        Profile profile = Core.getProfileManager().read(player.getUniqueId());

        if (profile == null) return;

        PermissionAttachment attachment = player.addAttachment(BukkitCore.getPlugin(BukkitCore.class));

        List<String> permissions = new ArrayList<>(profile.getRank().getCategory().getPermissions());

        profile.getRelation().getPermissions().forEach(entry -> permissions.add(entry.getKey()));

        permissions.forEach(permission -> attachment.setPermission(permission, true));
    }

    public static void unloadPermissions(Player player) {
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            PermissionAttachment attachment = permission.getAttachment();

            if (attachment == null) return;

            Map<String, Boolean> flags = attachment.getPermissions();

            flags.forEach((key, value) -> attachment.setPermission(key, false));
        }
    }
}