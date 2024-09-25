package br.com.poison.core.manager;

import br.com.poison.core.command.sender.CommandSender;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.manager.base.Manager;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProfileManager extends Manager<UUID, Profile> {

    @Override
    public void save(Profile profile) {
        getCacheMap().put(profile.getId(), profile);
    }

    public Profile read(String name) {
        return documents(profile -> profile.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void log(CommandSender sender, String message) {
        documents(profile -> !profile.getName().equalsIgnoreCase(sender.getName()) && profile.isStaffer() && profile.getPreference().isAllowLogs())
                .forEach(profile -> profile.sendMessage("§7[" + message + "]"));
    }

    public void log(Player sender, String message) {
        documents(profile -> !profile.getName().equalsIgnoreCase(sender.getName()) && profile.isStaffer() && profile.getPreference().isAllowLogs())
                .forEach(profile -> profile.sendMessage("§7[" + message + "]"));
    }
}
