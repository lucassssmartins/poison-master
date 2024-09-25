package br.com.poison.core.bukkit.listener;

import br.com.poison.core.bukkit.event.list.profile.clan.list.ProfileClanEnterEvent;
import br.com.poison.core.bukkit.event.list.profile.clan.list.ProfileClanLeaveEvent;
import br.com.poison.core.bukkit.api.user.tag.TagController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TagListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        TagController.removeTag(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerKickEvent event) {
        TagController.removeTag(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClanEnter(ProfileClanEnterEvent event) {
        TagController.updateTag(event.getProfile().player());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClanLeave(ProfileClanLeaveEvent event) {
        TagController.updateTag(event.getProfile().player());
    }
}
