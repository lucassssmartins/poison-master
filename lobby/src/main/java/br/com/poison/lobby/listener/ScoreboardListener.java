package br.com.poison.lobby.listener;

import br.com.poison.lobby.Lobby;
import br.com.poison.lobby.user.User;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.event.list.profile.clan.list.ProfileClanEnterEvent;
import br.com.poison.core.bukkit.event.list.profile.clan.list.ProfileClanLeaveEvent;
import br.com.poison.core.bukkit.event.list.profile.rank.ProfileRankEvent;
import br.com.poison.core.bukkit.event.list.update.type.list.AsyncUpdateEvent;
import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import br.com.poison.core.profile.resources.rank.Rank;
import br.com.poison.core.resources.clan.Clan;
import br.com.poison.core.util.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ScoreboardListener implements Listener {

    @EventHandler
    public void update(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (User user : Lobby.getUserManager().documents()) {
                if (user == null || user.getSidebar() == null) continue;

                user.getSidebar().updateRow("players", "§a" + Util.formatNumber(Core.getServerManager().getTotalPlayers()));
            }
        }
    }

    @EventHandler
    public void rank(ProfileRankEvent event) {
        User user = Lobby.getUserManager().read(event.getProfile().getId());

        Rank rank = event.getRank();

        if (user.getSidebar() != null)
            user.getSidebar().updateRow("rank", rank.getPrefix());
    }

    @EventHandler
    public void clanEnter(ProfileClanEnterEvent event) {
        User user = Lobby.getUserManager().read(event.getProfile().getId());

        Clan clan = event.getClan();

        if (user.getSidebar() != null)
            user.getSidebar().updateRow("clan", "§7" + clan.getTag());
    }

    @EventHandler
    public void clanLeave(ProfileClanLeaveEvent event) {
        User user = Lobby.getUserManager().read(event.getProfile().getId());

        if (user.getSidebar() != null)
            user.getSidebar().updateRow("clan", "§7-/-");
    }
}
