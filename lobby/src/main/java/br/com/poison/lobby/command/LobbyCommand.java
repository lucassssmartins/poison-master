package br.com.poison.lobby.command;

import br.com.poison.lobby.Lobby;
import br.com.poison.lobby.user.User;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandInheritor {

    @Command(name = "lobby", aliases = {"l", "hub"})
    public void lobby(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = Lobby.getUserManager().read(player.getUniqueId());

        if (user.getGame().isCategory(ArcadeCategory.HUB)) {
            player.sendMessage("§cVocê já está conectado no Lobby Principal!");
            return;
        }

        Lobby.getGameManager().redirect(user, ArcadeCategory.HUB);
    }
}
